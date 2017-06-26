package org.rabix.executor.execution;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.rabix.backend.api.engine.EngineStub;
import org.rabix.bindings.model.Job;
import org.rabix.executor.handler.JobHandlerFactory;
import org.rabix.executor.model.JobData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Job execution command dispatcher.
 */
public class JobHandlerCommandDispatcher {

  private static final Logger logger = LoggerFactory.getLogger(JobHandlerCommandDispatcher.class);

  private final JobHandlerFactory jobHandlerFactory;

  private final Map<UUID, Map<UUID, JobHandlerRunnable>> jobHandlerRunnables = new HashMap<>();

  private final ThreadFactory jobHandlerThreadFactory;
  private final ExecutorService jobHandlerThreadExecutor;
  private final ScheduledExecutorService jobHandlerThreadCleanExecutor;

  @Inject
  public JobHandlerCommandDispatcher(JobHandlerFactory jobHandlerFactory) {
    this.jobHandlerFactory = jobHandlerFactory;
    this.jobHandlerThreadFactory = buildJobHandlerThreadFactory();
    this.jobHandlerThreadExecutor = Executors.newCachedThreadPool(jobHandlerThreadFactory);
    this.jobHandlerThreadCleanExecutor = Executors.newScheduledThreadPool(1);
    init();
  }

  /**
   * Initializes dispatcher
   */
  private void init() {
    scheduleCleaner();
  }

  /**
   * Dispatch commands to appropriate runnable threads
   */
  public void dispatch(JobData jobData, JobHandlerCommand command, EngineStub<?,?,?> engineStub) {
    synchronized (jobHandlerRunnables) {
      UUID rootId = jobData.getJob().getRootId();
      JobHandlerRunnable jobHandlerRunnable = getJobs(rootId).get(jobData.getJob().getId());

      if (jobHandlerRunnable == null) {
        Job job = jobData.getJob();
        jobHandlerRunnable = new JobHandlerRunnable(job.getId(), job.getRootId(), jobHandlerFactory.createHandler(job, engineStub));
        getJobs(rootId).put(job.getId(), jobHandlerRunnable);
        jobHandlerThreadExecutor.execute(jobHandlerRunnable);
        logger.info("JobHandlerRunnable created for {}.", job.getId());
      }
      jobHandlerRunnable.addCommand(command);
    }
  }

  private Map<UUID, JobHandlerRunnable> getJobs(UUID rootId) {
    synchronized (jobHandlerRunnables) {
      Map<UUID, JobHandlerRunnable> jobList = jobHandlerRunnables.get(rootId);
      if (jobList == null) {
        jobList = new HashMap<>();
        jobHandlerRunnables.put(rootId, jobList);
      }
      return jobList;
    }
  }

  /**
   * Creates simple Job handler thread factory
   */
  private ThreadFactory buildJobHandlerThreadFactory() {
    return new JobHandlerThreadFactoryBuilder()
      .setNamePrefix("JobHandler-Thread")
      .setDaemon(false)
      .setPriority(Thread.MAX_PRIORITY)
      .setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
          logger.error(String.format("Thread %s threw exception - %s", t.getName(), e.getMessage()));
        }
      }).build();
  }

  /**
   * Schedule cleaner thread that will go through the list of Job threads
   */
  private void scheduleCleaner() {
    jobHandlerThreadCleanExecutor.scheduleWithFixedDelay(new Runnable() {
      @Override
      public void run() {
        synchronized (jobHandlerRunnables) {
          logger.debug("Cleaner thread is executing. There are {} runnable(s) in the pool.", jobHandlerRunnables.size());

          List<Pair> stoppedIds = new ArrayList<>();
          List<Pair> runningIds = new ArrayList<>();

          for (Entry<UUID, Map<UUID, JobHandlerRunnable>> runnableEntry : jobHandlerRunnables.entrySet()) {
            UUID contextId = runnableEntry.getKey();
            for (Entry<UUID, JobHandlerRunnable> runnable : runnableEntry.getValue().entrySet()) {
              UUID id = runnable.getKey();
              JobHandlerRunnable thread = runnable.getValue();

              if (thread.isStopped()) {
                stoppedIds.add(new Pair(id, contextId));
              } else {
                runningIds.add(new Pair(id, contextId));
              }
            }
          }

          for (Pair stopped : stoppedIds) {
            logger.debug("Cleaner thread removes JobHandlerRunnable for context {} and job {}.", stopped.rootId, stopped.jobId);
            jobHandlerRunnables.get(stopped.rootId).remove(stopped.jobId);
          }
        }
      }
      
      class Pair {
        private UUID jobId;
        private UUID rootId;
        
        public Pair(UUID jobId, UUID rootId) {
          this.jobId = jobId;
          this.rootId = rootId;
        }
      }

    }, 1, 1, TimeUnit.MINUTES);
  }
  
}
