package org.rabix.engine.rest.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.configuration.Configuration;
import org.rabix.bindings.model.Job;
import org.rabix.common.engine.control.EngineControlFreeMessage;
import org.rabix.common.engine.control.EngineControlStopMessage;
import org.rabix.engine.repository.TransactionHelper;
import org.rabix.engine.rest.backend.stub.BackendStub;
import org.rabix.engine.rest.backend.stub.BackendStub.HeartbeatCallback;
import org.rabix.engine.rest.service.BackendService;
import org.rabix.engine.rest.service.BackendServiceException;
import org.rabix.engine.rest.service.JobService;
import org.rabix.engine.rest.service.SchedulerService;
import org.rabix.engine.service.RecordDeleteService;
import org.rabix.transport.backend.Backend;
import org.rabix.transport.backend.HeartbeatInfo;
import org.rabix.transport.mechanism.TransportPlugin.ErrorCallback;
import org.rabix.transport.mechanism.TransportPlugin.ReceiveCallback;
import org.rabix.transport.mechanism.TransportPluginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class SchedulerServiceImpl implements SchedulerService {

  private final static Logger logger = LoggerFactory.getLogger(SchedulerServiceImpl.class);

  private final static long SCHEDULE_PERIOD = TimeUnit.SECONDS.toMillis(1);
  private final static long DEFAULT_HEARTBEAT_PERIOD = TimeUnit.SECONDS.toMillis(5);

  private final List<BackendStub<?, ?, ?>> backendStubs = new ArrayList<>();

  private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
  private ScheduledExecutorService heartbeatService = Executors.newSingleThreadScheduledExecutor();

  private Lock dispatcherLock = new ReentrantLock(true);

  private int position = 0;

  private final long heartbeatPeriod;

  private final JobService jobService;
  private final BackendService backendService;

  private final TransactionHelper transactionHelper;
  private final RecordDeleteService recordDeleteService;
  
  private final Map<Job, BackendStub<?, ?, ?>> scheduledJobs = new HashMap<>();

  @Inject
  public SchedulerServiceImpl(Configuration configuration, JobService jobService, BackendService backendService, TransactionHelper repositoriesFactory, RecordDeleteService recordDeleteService) {
    this.jobService = jobService;
    this.backendService = backendService;
    this.transactionHelper = repositoriesFactory;
    this.recordDeleteService = recordDeleteService;
    this.heartbeatPeriod = configuration.getLong("backend.cleaner.heartbeatPeriodMills", DEFAULT_HEARTBEAT_PERIOD);
  }

  @Override
  public void start() {
    executorService.execute(new Runnable() {
      @Override
      public void run() {
        try {
          while (true) {
            schedule();
            Thread.sleep(SCHEDULE_PERIOD);
          }
        } catch (Exception e) {
          logger.error("Failed to schedule jobs", e);
        }
      }
    });

    heartbeatService.scheduleAtFixedRate(new HeartbeatMonitor(), 0, heartbeatPeriod, TimeUnit.MILLISECONDS);
    recordDeleteService.start();
  }

  private void schedule() {
    try {
      dispatcherLock.lock();
      transactionHelper.doInTransaction(new TransactionHelper.TransactionCallback<Void>() {
        @Override
        public Void call() throws Exception {
          if (backendStubs.isEmpty()) {
            return null;
          }

          Set<Job> freeJobs = jobService.getReadyFree();
          for (Job freeJob : freeJobs) {
            BackendStub<?, ?, ?> backendStub = nextBackend();

            jobService.updateBackend(freeJob.getId(), backendStub.getBackend().getId());
            scheduledJobs.put(freeJob, backendStub);
            logger.info("Job {} sent to {}.", freeJob.getId(), backendStub.getBackend().getId());
          }
          return null;
        }
      });
      for (Entry<Job, BackendStub<?, ?, ?>> mapping : scheduledJobs.entrySet()) {
        mapping.getValue().send(mapping.getKey());
      }
      scheduledJobs.clear();
    } catch (Exception e) {
      logger.error("Failed to schedule Jobs", e);
      // TODO handle exception
    } finally {
      dispatcherLock.unlock();
    }
  }

  public boolean stop(Job... jobs) {
    try {
      dispatcherLock.lock();
      for (Job job : jobs) {
        Set<UUID> backendIds = jobService.getBackendsByRootId(job.getId());
        for (UUID backendId : backendIds) {
          if (backendId != null) {
            BackendStub<?, ?, ?> backendStub = getBackendStub(backendId);
            if (backendStub != null) {
              backendStub.send(new EngineControlStopMessage(job.getId(), job.getRootId()));
            }
          }
        }
      }
      return true;
    } finally {
      dispatcherLock.unlock();
    }
  }

  public void addBackendStub(BackendStub<?, ?, ?> backendStub) throws BackendServiceException {
    try {
      dispatcherLock.lock();
      backendStub.start(new HeartbeatCallback() {
        @Override
        public void save(HeartbeatInfo info) throws Exception {
          backendService.updateHeartbeatInfo(info);
        }
      }, new ReceiveCallback<Job>() {
        @Override
        public void handleReceive(Job job) throws TransportPluginException {
          try {
            jobService.update(job);
          } catch (Exception e) {
            throw new TransportPluginException("Failed to update Job", e);
          }
        }
      }, new ErrorCallback() {
        @Override
        public void handleError(Exception error) {
          logger.error("Failed to receive message.", error);
        }
      });
      this.backendStubs.add(backendStub);
    } finally {
      dispatcherLock.unlock();
    }
  }

  public void freeBackend(Job rootJob) {
    try {
      dispatcherLock.lock();
      Set<BackendStub<?, ?, ?>> backendStubs = new HashSet<>();

      Set<UUID> backendIds = jobService.getBackendsByRootId(rootJob.getId());
      for (UUID backendId : backendIds) {
        backendStubs.add(getBackendStub(backendId));
      }

      for (BackendStub<?, ?, ?> backendStub : backendStubs) {
        backendStub.send(new EngineControlFreeMessage(rootJob.getConfig(), rootJob.getRootId()));
      }
    } finally {
      dispatcherLock.unlock();
    }
  }

  public void deallocate(Job job) {
    jobService.delete(job.getId());
  }

  private BackendStub<?, ?, ?> nextBackend() {
    BackendStub<?, ?, ?> backendStub = backendStubs.get(position % backendStubs.size());
    position = (position + 1) % backendStubs.size();
    return backendStub;
  }

  private BackendStub<?, ?, ?> getBackendStub(UUID id) {
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
          public Void call() throws Exception {
            try {
              dispatcherLock.lock();
              logger.trace("Checking Backend heartbeats...");

              long currentTime = System.currentTimeMillis();

              Iterator<BackendStub<?, ?, ?>> backendIterator = backendStubs.iterator();

              while (backendIterator.hasNext()) {
                BackendStub<?, ?, ?> backendStub = backendIterator.next();
                Backend backend = backendStub.getBackend();

                Long heartbeatInfo = backendService.getHeartbeatInfo(backend.getId());

                if (heartbeatInfo == null || currentTime - heartbeatInfo > heartbeatPeriod) {
                  backendStub.stop();
                  backendIterator.remove();
                  logger.info("Removing Backend {}", backendStub.getBackend().getId());

                  jobService.dealocateJobs(backend.getId());
                  backendService.stopBackend(backend);
                }
              }
              logger.trace("Heartbeats checked");
              return null;
            } finally {
              dispatcherLock.unlock();
            }
          }
        });
      } catch (Exception e) {
        logger.error("Failed to check heartbeats", e);
      }
    }
  }

}
