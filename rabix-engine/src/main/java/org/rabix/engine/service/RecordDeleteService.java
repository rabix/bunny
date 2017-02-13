package org.rabix.engine.service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.rabix.engine.model.JobRecord.JobIdRootIdPair;
import org.rabix.engine.repository.JobRecordRepository;
import org.rabix.engine.repository.LinkRecordRepository;
import org.rabix.engine.repository.TransactionHelper;
import org.rabix.engine.repository.VariableRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class RecordDeleteService {

  private final Logger logger = LoggerFactory.getLogger(RecordDeleteService.class);
  
  private final static int DEFAULT_BATCH_SIZE = 100;
  private final static long DEFAULT_SLEEP = TimeUnit.SECONDS.toMillis(4);

  private final JobRecordRepository jobRecordRepository;
  private final LinkRecordRepository linkRecordRepository;
  private final VariableRecordRepository variableRecordRepository;
  
  private final TransactionHelper transactionService;

  private final BlockingQueue<JobIdRootIdPair> jobIDs = new LinkedBlockingQueue<>();

  private final ExecutorService executorService = Executors.newFixedThreadPool(1);

  @Inject
  public RecordDeleteService(JobRecordRepository jobRecordRepository, LinkRecordRepository linkRecordRepository, VariableRecordRepository variableRecordRepository, TransactionHelper transactionService) {
    this.jobRecordRepository = jobRecordRepository;
    this.linkRecordRepository = linkRecordRepository;
    this.variableRecordRepository = variableRecordRepository;
    this.transactionService = transactionService;
    start();
  }

  private void start() {
    final Set<JobIdRootIdPair> idsToRemove = new HashSet<>();
    executorService.execute(new Runnable() {
      @Override
      public void run() {
        while (true) {
          try {
            transactionService.doInTransaction(new TransactionHelper.TransactionCallback<Void>() {
              @Override
              public Void call() throws Exception {
                jobIDs.drainTo(idsToRemove, DEFAULT_BATCH_SIZE);
                if (idsToRemove.size() > 0) {
                  jobRecordRepository.delete(idsToRemove);
                  logger.debug("Removed {} Job ID - Root ID pairs.", idsToRemove.size());
                  idsToRemove.clear();
                }
                return null;
              }
            });
          } catch (Exception e) {
            // TODO handle exception
          }
          try {
            Thread.sleep(DEFAULT_SLEEP);
          } catch (InterruptedException e) {
            // TODO handle restart
          }
        }
      }
    });
  }
  
  public void addJobId(String jobId, UUID rootId) {
    this.jobIDs.add(new JobIdRootIdPair(jobId, rootId));
  }
  
}
