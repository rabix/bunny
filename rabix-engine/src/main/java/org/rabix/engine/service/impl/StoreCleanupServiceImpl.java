package org.rabix.engine.service.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.Configuration;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.engine.repository.JobRecordRepository;
import org.rabix.engine.repository.JobRepository;
import org.rabix.engine.repository.TransactionHelper;
import org.rabix.engine.service.StoreCleanupService;
import org.rabix.engine.service.impl.JobRecordServiceImpl.JobState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class StoreCleanupServiceImpl implements StoreCleanupService {

  private final static Logger logger = LoggerFactory.getLogger(StoreCleanupServiceImpl.class);
  
  private final static long DEFAULT_SLEEP = TimeUnit.SECONDS.toMillis(4);

  private final long sleepPeriod;
  private final TransactionHelper transactionService;
  
  private final JobRepository jobRepository;
  private final JobRecordRepository jobRecordRepository;

  private final ExecutorService executorService = Executors.newFixedThreadPool(1);

  @Inject
  public StoreCleanupServiceImpl(JobRepository jobRepository, JobRecordRepository jobRecordRepository,
      TransactionHelper transactionService, Configuration configuration) {
    this.sleepPeriod = configuration.getLong("db.delete_period", DEFAULT_SLEEP);
    this.jobRepository = jobRepository;
    this.jobRecordRepository = jobRecordRepository;
    this.transactionService = transactionService;
  }

  public void start() {
    executorService.execute(new Runnable() {
      @Override
      public void run() {
        while (true) {
          try {
            transactionService.doInTransaction(new TransactionHelper.TransactionCallback<Void>() {
              @Override
              public Void call() throws Exception {
                Set<Job> completedRootJobs = jobRepository.getRootsByStatus(JobStatus.COMPLETED);
                
                Set<UUID> rootIds = new HashSet<>();
                for (Job rootJob : completedRootJobs) {
                  rootIds.add(rootJob.getRootId());
                }
                jobRepository.deleteByRootIds(rootIds);
                
                int deleted = jobRecordRepository.deleteByStatus(JobState.COMPLETED);
                logger.debug("Deleted {} completed Jobs", deleted);
                return null;
              }
            });
          } catch (Exception e) {
            // TODO handle exception
          }
          try {
            Thread.sleep(sleepPeriod);
          } catch (InterruptedException e) {
            // TODO handle restart
          }
        }
      }
    });
  }
  
}
