package org.rabix.engine.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.configuration.Configuration;
import org.rabix.bindings.model.Job;
import org.rabix.common.engine.control.EngineControlFreeMessage;
import org.rabix.common.engine.control.EngineControlStopMessage;
import org.rabix.engine.repository.BackendRepository;
import org.rabix.engine.repository.JobRepository.JobEntity;
import org.rabix.engine.repository.TransactionHelper;
import org.rabix.engine.service.BackendService;
import org.rabix.engine.service.BackendServiceException;
import org.rabix.engine.service.JobService;
import org.rabix.engine.service.SchedulerService;
import org.rabix.engine.service.SchedulerService.SchedulerCallback;
import org.rabix.engine.service.StoreCleanupService;
import org.rabix.engine.stub.BackendStub;
import org.rabix.engine.stub.BackendStub.HeartbeatCallback;
import org.rabix.transport.backend.Backend;
import org.rabix.transport.backend.Backend.BackendStatus;
import org.rabix.transport.backend.HeartbeatInfo;
import org.rabix.transport.mechanism.TransportPlugin.ErrorCallback;
import org.rabix.transport.mechanism.TransportPlugin.ReceiveCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class SchedulerServiceImpl implements SchedulerService, SchedulerCallback {

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

  private final boolean backendLocal;

  private final TransactionHelper transactionHelper;
  private final StoreCleanupService storeCleanupService;
  
  private final SchedulerCallback schedulerCallback;
  
  private final AtomicReference<Set<SchedulerMessage>> messages = new AtomicReference<Set<SchedulerMessage>>(Collections.<SchedulerMessage>emptySet());

  private ReceiveCallback<Job> jobReceiver;

  private ErrorCallback errorCallback;
  
  @Inject
  public SchedulerServiceImpl(Configuration configuration, JobService jobService, BackendService backendService,
      TransactionHelper repositoriesFactory, StoreCleanupService storeCleanupService, SchedulerCallback schedulerCallback, ReceiveCallback<Job> jobReceiver,
      BackendRepository backendRepository) {
    this.jobService = jobService;
    this.backendService = backendService;
    this.schedulerCallback = schedulerCallback;
    this.transactionHelper = repositoriesFactory;
    this.storeCleanupService = storeCleanupService;
    this.heartbeatPeriod = configuration.getLong("cleaner.backend.period", DEFAULT_HEARTBEAT_PERIOD);
    this.backendLocal = configuration.getBoolean("backend.local", false);

    this.jobReceiver = jobReceiver;
    this.errorCallback = new ErrorCallback() {
      @Override
      public void handleError(Exception error) {
        logger.error("Failed to receive message.", error);
      }
    };
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
    storeCleanupService.start();
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
          Set<JobEntity> entities = jobService.getReadyFree();
          if (!entities.isEmpty()) {
            messages.set(schedulerCallback.onSchedule(entities, getBackendIds()));
            jobService.updateBackends(entities);
          }
          return null;
        }
      });
      for (SchedulerMessage message : messages.get()) {
        getBackendStub(message.getBackendId()).send(message.getPayload());
        logger.debug("Message sent to {}.", message.getBackendId());
      }
      messages.set(Collections.<SchedulerMessage>emptySet());
    } catch (Exception e) {
      logger.error("Failed to schedule Jobs", e);
      // TODO handle exception
    } finally {
      dispatcherLock.unlock();
    }
  }
  
  @Override
  public Set<SchedulerMessage> onSchedule(Set<JobEntity> entities, Set<UUID> backendIDs) {
    Set<SchedulerMessage> messages = new HashSet<>();
    for (JobEntity entity : entities) {
      BackendStub<?, ?, ?> backendStub = nextBackend();
      UUID backendID = backendStub.getBackend().getId();
      entity.setBackendId(backendStub.getBackend().getId());
      messages.add(new SchedulerMessage(backendID, entity.getJob()));
    }
    return messages;
  }

  public boolean stop(Job... jobs) {
    try {
      dispatcherLock.lock();
      for (Job job : jobs) {
        Set<UUID> backendIds = jobService.getBackendsByRootId(job.getRootId());
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
          if (backendStub.getBackend().getStatus() == BackendStatus.INACTIVE) {
            Backend backend = backendStub.getBackend();
            info.setId(backend.getId());
            backendService.updateHeartbeatInfo(info);
            backendStubs.add(backendStub);
            backendStub.getBackend().setStatus(BackendStatus.ACTIVE);
            logger.debug("Awakening backend: " + backend.getId());
          }
          backendService.updateHeartbeatInfo(backendStub.getBackend().getId(), new Timestamp(info.getTimestamp()));
        }
      }, this.jobReceiver, this.errorCallback);
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
  
  private Set<UUID> getBackendIds() {
    Set<UUID> ids = new HashSet<>();
    for (BackendStub<?, ?, ?> backendStub : backendStubs) {
      ids.add(backendStub.getBackend().getId());
    }
    return ids;
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

                if ((heartbeatInfo == null || currentTime - heartbeatInfo > heartbeatPeriod) && !backendLocal) {
                  backendStub.stop();
                  backend.setStatus(BackendStatus.INACTIVE);
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
