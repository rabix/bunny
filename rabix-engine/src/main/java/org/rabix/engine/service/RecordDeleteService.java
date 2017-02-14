package org.rabix.engine.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.Configuration;
import org.rabix.engine.repository.JobRecordRepository;
import org.rabix.engine.repository.TransactionHelper;
import org.rabix.engine.service.JobRecordService.JobState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class RecordDeleteService {

  private final static Logger logger = LoggerFactory.getLogger(RecordDeleteService.class);
  
  private final static long DEFAULT_SLEEP = TimeUnit.SECONDS.toMillis(4);

  private final long sleepPeriod;
  private final TransactionHelper transactionService;
  private final JobRecordRepository jobRecordRepository;

  private final ExecutorService executorService = Executors.newFixedThreadPool(1);

  @Inject
  public RecordDeleteService(JobRecordRepository jobRecordRepository, TransactionHelper transactionService, Configuration configuration) {
    this.sleepPeriod = configuration.getLong("db.delete_period", DEFAULT_SLEEP);
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
