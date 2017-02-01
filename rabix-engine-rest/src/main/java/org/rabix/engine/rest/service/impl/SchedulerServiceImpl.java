package org.rabix.engine.rest.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.configuration.Configuration;
import org.rabix.bindings.model.Job;
import org.rabix.common.engine.control.EngineControlFreeMessage;
import org.rabix.common.engine.control.EngineControlStopMessage;
import org.rabix.engine.db.JobBackendService;
import org.rabix.engine.db.JobBackendService.BackendJob;
import org.rabix.engine.repository.TransactionHelper;
import org.rabix.engine.repository.TransactionHelper.TransactionException;
import org.rabix.engine.rest.backend.stub.BackendStub;
import org.rabix.engine.rest.service.JobService;
import org.rabix.engine.rest.service.SchedulerService;
import org.rabix.transport.backend.Backend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class SchedulerServiceImpl implements SchedulerService {

  private final static Logger logger = LoggerFactory.getLogger(SchedulerServiceImpl.class);

  private final static long DEFAULT_HEARTBEAT_PERIOD = TimeUnit.MINUTES.toMillis(2);

  private final List<BackendStub<?, ?, ?>> backendStubs = new ArrayList<>();
  private final Map<String, Long> heartbeatInfo = new HashMap<>();

  private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
  private ScheduledExecutorService heartbeatService = Executors.newSingleThreadScheduledExecutor();

  private Lock dispatcherLock = new ReentrantLock(true);

  private int position = 0;

  private final long heartbeatPeriod;

  private final JobService jobService;
  private final JobBackendService jobBackendService;

  private final TransactionHelper transactionHelper;

  @Inject
  public SchedulerServiceImpl(Configuration configuration, JobBackendService jobBackendService, JobService jobService,
      TransactionHelper repositoriesFactory) {
    this.jobService = jobService;
    this.jobBackendService = jobBackendService;
    this.transactionHelper = repositoriesFactory;
    this.heartbeatPeriod = configuration.getLong("backend.cleaner.heartbeatPeriodMills", DEFAULT_HEARTBEAT_PERIOD);
    start();
  }

  private void start() {
    executorService.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        try {
          transactionHelper.doInTransaction(new TransactionHelper.TransactionCallback<Void>() {
            @Override
            public Void call() throws TransactionException {
              schedule();
              return null;
            }
          });
        } catch (TransactionException e) {
          // TODO handle exception
          logger.error("Failed to schedule jobs", e);
        }
      }
    }, 0, 1, TimeUnit.SECONDS);

    heartbeatService.scheduleAtFixedRate(new HeartbeatMonitor(), 0, heartbeatPeriod, TimeUnit.MILLISECONDS);
  }

  public void send(Job... jobs) {
    for (Job job : jobs) {
      jobBackendService.insert(job.getId(), job.getRootId(), null);
    }
  }

  private void schedule() {
    if (backendStubs.isEmpty()) {
      return;
    }

    Set<BackendJob> freeJobs = jobBackendService.getFree();
    for (BackendJob freeJob : freeJobs) {
      BackendStub<?, ?, ?> backendStub = nextBackend();

      jobBackendService.update(freeJob.getJobId(), backendStub.getBackend().getId());
      backendStub.send(jobService.get(freeJob.getJobId()));
      logger.info("Job {} sent to {}.", freeJob.getJobId(), backendStub.getBackend().getId());
    }
  }

  public boolean stop(Job... jobs) {
    for (Job job : jobs) {
      String backendId = jobBackendService.getByJobId(job.getId()).getBackendId();
      if (backendId != null) {
        BackendStub<?, ?, ?> backendStub = getBackendStub(backendId);
        if (backendStub != null) {
          backendStub.send(new EngineControlStopMessage(job.getId(), job.getRootId()));
        }
      }
    }
    return true;
  }

  public void addBackendStub(BackendStub<?, ?, ?> backendStub) {
    try {
      dispatcherLock.lock();
      backendStub.start(heartbeatInfo);
      this.backendStubs.add(backendStub);
      this.heartbeatInfo.put(backendStub.getBackend().getId(), System.currentTimeMillis());
    } finally {
      dispatcherLock.unlock();
    }
  }

  public void freeBackend(Job rootJob) {
    Set<BackendStub<?, ?, ?>> backendStubs = new HashSet<>();

    Set<BackendJob> backendJobs = jobBackendService.getByRootId(rootJob.getId());
    for (BackendJob backendJob : backendJobs) {
      backendStubs.add(getBackendStub(backendJob.getBackendId()));
    }

    for (BackendStub<?, ?, ?> backendStub : backendStubs) {
      backendStub.send(new EngineControlFreeMessage(rootJob.getConfig(), rootJob.getRootId()));
    }
  }

  public void remove(Job job) {
    jobBackendService.delete(job.getId());
  }

  private BackendStub<?, ?, ?> nextBackend() {
    BackendStub<?, ?, ?> backendStub = backendStubs.get(position % backendStubs.size());
    position = (position + 1) % backendStubs.size();
    return backendStub;
  }

  private BackendStub<?, ?, ?> getBackendStub(String id) {
    for (BackendStub<?, ?, ?> backendStub : backendStubs) {
      if (backendStub.getBackend().getId().equals(id)) {
        return backendStub;
      }
    }
    return null;
  }

  private class HeartbeatMonitor implements Runnable {
    @Override
    public void run() {
      try {
        transactionHelper.doInTransaction(new TransactionHelper.TransactionCallback<Void>() {
          @Override
          public Void call() throws TransactionException {
            logger.info("Checking Backend heartbeats...");

            long currentTime = System.currentTimeMillis();
            
            Iterator<BackendStub<?, ?, ?>> backendIterator = backendStubs.iterator();
            
            while(backendIterator.hasNext()) {
              BackendStub<?, ?, ?> backendStub = backendIterator.next();
              Backend backend = backendStub.getBackend();

              if (currentTime - heartbeatInfo.get(backend.getId()) > heartbeatPeriod) {
                backendStub.stop();
                backendIterator.remove();
                logger.info("Removing Backend {}", backendStub.getBackend().getId());

                Set<BackendJob> backendJobs = jobBackendService.getByBackendId(backend.getId());

                for (BackendJob backendJob : backendJobs) {
                  jobBackendService.update(backendJob.getJobId(), null);
                  logger.info("Reassign Job {} to free Jobs", backendJob.getJobId());
                }
              }
            }
            logger.info("Heartbeats checked");
            return null;
          }
        });
      } catch (TransactionException e) {
        // TODO handle exception
        logger.error("Failed to check heartbeats", e);
      }
    }
  }

}
