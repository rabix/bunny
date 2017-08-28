package org.rabix.engine.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.apache.commons.configuration.Configuration;
import org.rabix.bindings.model.Job;
import org.rabix.common.engine.control.EngineControlFreeMessage;
import org.rabix.common.engine.control.EngineControlStopMessage;
import org.rabix.engine.service.BackendService;
import org.rabix.engine.service.BackendServiceException;
import org.rabix.engine.service.JobService;
import org.rabix.engine.service.SchedulerService;
import org.rabix.engine.service.SchedulerService.SchedulerJobBackendAssigner;
import org.rabix.engine.service.SchedulerService.SchedulerMessageCreator;
import org.rabix.engine.service.SchedulerService.SchedulerMessageSender;
import org.rabix.engine.store.repository.BackendRepository;
import org.rabix.engine.store.repository.JobRepository.JobEntity;
import org.rabix.engine.store.repository.TransactionHelper;
import org.rabix.engine.stub.BackendStub;
import org.rabix.transport.backend.Backend;
import org.rabix.transport.backend.Backend.BackendStatus;
import org.rabix.transport.mechanism.TransportPlugin.ErrorCallback;
import org.rabix.transport.mechanism.TransportPlugin.ReceiveCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class SchedulerServiceImpl implements SchedulerService, SchedulerMessageCreator, SchedulerJobBackendAssigner, SchedulerMessageSender {

  private final static Logger logger = LoggerFactory.getLogger(SchedulerServiceImpl.class);

  private final static long SCHEDULE_PERIOD = TimeUnit.SECONDS.toMillis(1);
  private final static long DEFAULT_HEARTBEAT_PERIOD = TimeUnit.SECONDS.toMillis(5);

  private final List<BackendStub<?, ?, ?>> backendStubs = new ArrayList<>();

  private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
  private ScheduledExecutorService heartbeatService = Executors.newSingleThreadScheduledExecutor();

  private Lock dispatcherLock = new ReentrantLock(true);

  private int stubPosition = 0;
  private final long heartbeatPeriod;

  private final JobService jobService;
  private final BackendService backendService;

  private final TransactionHelper transactionHelper;

  private final AtomicReference<Set<SchedulerMessage>> messages = new AtomicReference<Set<SchedulerMessage>>(Collections.<SchedulerMessage>emptySet());

  private final ReceiveCallback<Job> jobReceiver;

  private final ErrorCallback errorCallback;

  private final SchedulerMessageCreator messageCreator;
  private final SchedulerJobBackendAssigner assigner;
  
  @Inject
  public SchedulerServiceImpl(Configuration configuration, JobService jobService, BackendService backendService,
      TransactionHelper repositoriesFactory, SchedulerMessageCreator messageCreator, ReceiveCallback<Job> jobReceiver,
      BackendRepository backendRepository, SchedulerJobBackendAssigner assigner) {
    this.jobService = jobService;
    this.backendService = backendService;
    this.transactionHelper = repositoriesFactory;
    this.messageCreator = messageCreator;
    this.assigner = assigner;
    this.heartbeatPeriod = configuration.getLong("cleaner.backend.period", DEFAULT_HEARTBEAT_PERIOD);

    this.jobReceiver = jobReceiver;
    this.errorCallback = error -> logger.error("Failed to receive message.", error);
  }

  @Override
  public void start() {
    executorService.execute(() -> {
      try {
        while (true) {
          schedule();
          Thread.sleep(SCHEDULE_PERIOD);
        }
      } catch (Exception e) {
        logger.error("Failed to schedule jobs", e);
      }
    });
    heartbeatService.scheduleAtFixedRate(new HeartbeatMonitor(), 0, heartbeatPeriod, TimeUnit.MILLISECONDS);
  }
  
  private void schedule() {
    try {
      dispatcherLock.lock();
      transactionHelper.doInTransaction(() -> {
        if (backendStubs.isEmpty()) {
          return null;
        }
        Set<JobEntity> entities = jobService.getReadyFree();
        if (entities.isEmpty()) {
          return null;
        }
        
        Set<JobEntity> assignments = assigner.assign(entities, getBackends());
        entities.stream().forEach(e -> {
          e.setBackendId(assignments.stream().filter(a -> e.getJob().getId().equals(a.getJob().getId())).findFirst().get().getBackendId());
        });
        jobService.updateBackends(entities);
        messages.set(messageCreator.create(assignments));
        return null;
      });
      send(messages.getAndSet(Collections.<SchedulerMessage>emptySet()));
    } catch (Exception e) {
      logger.error("Failed to schedule Jobs", e);
    } finally {
      dispatcherLock.unlock();
    }
  }
  
  @Override
  public Set<JobEntity> assign(Set<JobEntity> jobs, Set<Backend> backends) {
     jobs.stream().forEach(j->j.setBackendId(backends.iterator().next().getId()));
     return jobs;
  }
  
  @Override
  public Set<SchedulerMessage> create(final Set<JobEntity> assignments) {
    return assignments.stream().map(a -> new SchedulerMessage(a.getBackendId(), a.getJob())).collect(Collectors.toSet());
  }
  
  @Override
  public void send(Set<SchedulerMessage> messages) {
    messages.stream().forEach(m -> {
      getStub(m.getBackendId()).send(m.getPayload());
      logger.debug("Message sent to {}.", m.getBackendId());
    });
  }

  public boolean stop(Job... jobs) {
    try {
      dispatcherLock.lock();
      for (Job job : jobs) {
        Set<UUID> backendIds = jobService.getBackendsByRootId(job.getRootId());
        for (UUID backendId : backendIds) {
          if (backendId != null) {
            BackendStub<?, ?, ?> backendStub = getStub(backendId);
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

      backendStub.start(info -> {
        if (backendStub.getBackend().getStatus() == BackendStatus.INACTIVE) {
          Backend backend = backendStub.getBackend();
          info.setId(backend.getId());
          backendService.updateHeartbeatInfo(info);
          backendStubs.add(backendStub);
          backendStub.getBackend().setStatus(BackendStatus.ACTIVE);
          logger.debug("Awakening backend: " + backend.getId());
        }
        backendService.updateHeartbeatInfo(backendStub.getBackend().getId(), Instant.ofEpochMilli(info.getTimestamp()));
      }, this.jobReceiver, this.errorCallback);
      this.backendStubs.add(backendStub);
    } finally {
      dispatcherLock.unlock();
    }
  }
  
  public void freeBackend(Job rootJob) {
    try {
      dispatcherLock.lock();
      Set<UUID> backendIds = jobService.getBackendsByRootId(rootJob.getId());
      backendIds.stream().map(i -> getStub(i)).forEach(s -> s.send(new EngineControlFreeMessage(rootJob.getConfig(), rootJob.getRootId())));
    } finally {
      dispatcherLock.unlock();
    }
  }

  public void deallocate(Job job) {
    jobService.delete(job.getId());
  }

  private BackendStub<?, ?, ?> nextBackend() {
    BackendStub<?, ?, ?> backendStub = backendStubs.get(stubPosition % backendStubs.size());
    stubPosition = (stubPosition + 1) % backendStubs.size();
    return backendStub;
  }
  
  private Set<UUID> getBackendIds() {
    return backendStubs.stream().map(s -> s.getBackend().getId()).collect(Collectors.toSet());
  }
  
  private Set<Backend> getBackends() {
    return backendStubs.stream().map(s -> s.getBackend()).collect(Collectors.toSet());
  }
  
  private BackendStub<?, ?, ?> getStub(UUID id) {
    Optional<BackendStub<?, ?, ?>> stub = backendStubs.stream().filter(s -> s.getBackend().getId().equals(id)).findFirst();
    return stub.isPresent() ? stub.get() : null;
  }

  private class HeartbeatMonitor implements Runnable {
    @Override
    public void run() {
      try {
        transactionHelper.doInTransaction(() -> {
          try {
            dispatcherLock.lock();
            logger.trace("Checking Backend heartbeats...");

            long currentTime = System.currentTimeMillis();

            Iterator<BackendStub<?, ?, ?>> backendIterator = backendStubs.iterator();

            while (backendIterator.hasNext()) {
              BackendStub<?, ?, ?> backendStub = backendIterator.next();
              Backend backend = backendStub.getBackend();

              Long heartbeatInfo = backendService.getHeartbeatInfo(backend.getId());

              if ((heartbeatInfo == null || currentTime - heartbeatInfo > heartbeatPeriod)) {
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
        });
      } catch (Exception e) {
        logger.error("Failed to check heartbeats", e);
      }
    }
  }

}
