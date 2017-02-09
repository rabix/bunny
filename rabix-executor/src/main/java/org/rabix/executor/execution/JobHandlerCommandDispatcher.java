package org.rabix.executor.execution;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.rabix.bindings.model.Job;
import org.rabix.executor.engine.EngineStub;
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

  private final Map<String, Map<String, JobHandlerRunnable>> jobHandlerRunnables = new HashMap<>();

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
      String contextId = jobData.getJob().getRootId();
      JobHandlerRunnable jobHandlerRunnable = getJobs(contextId).get(jobData.getJob().getId());

      if (jobHandlerRunnable == null) {
        Job job = jobData.getJob();
        jobHandlerRunnable = new JobHandlerRunnable(job.getId(), job.getRootId(), jobHandlerFactory.createHandler(job, engineStub));
        getJobs(contextId).put(job.getId(), jobHandlerRunnable);
        jobHandlerThreadExecutor.execute(jobHandlerRunnable);
        logger.info("JobHandlerRunnable created for {}.", job.getId());
      }
      jobHandlerRunnable.addCommand(command);
    }
  }

  private Map<String, JobHandlerRunnable> getJobs(String contextId) {
    synchronized (jobHandlerRunnables) {
      Map<String, JobHandlerRunnable> jobList = jobHandlerRunnables.get(contextId);
      if (jobList == null) {
        jobList = new HashMap<>();
        jobHandlerRunnables.put(contextId, jobList);
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

          for (Entry<String, Map<String, JobHandlerRunnable>> runnableEntry : jobHandlerRunnables.entrySet()) {
            String contextId = runnableEntry.getKey();
            for (Entry<String, JobHandlerRunnable> runnable : runnableEntry.getValue().entrySet()) {
              String id = runnable.getKey();
              JobHandlerRunnable thread = runnable.getValue();

              if (thread.isStopped()) {
                stoppedIds.add(new Pair(id, contextId));
              } else {
                runningIds.add(new Pair(id, contextId));
              }
            }
          }

          for (Pair stopped : stoppedIds) {
            logger.debug("Cleaner thread removes JobHandlerRunnable for context {} and job {}.", stopped.contextId, stopped.jobId);
            jobHandlerRunnables.get(stopped.contextId).remove(stopped.jobId);
          }
        }
      }
      
      class Pair {
        private String jobId;
        private String contextId;
        
        public Pair(String jobId, String contextId) {
          this.jobId = jobId;
          this.contextId = contextId;
        }
      }

    }, 1, 1, TimeUnit.MINUTES);
  }
  
}
