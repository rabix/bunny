package org.rabix.storage.postgres.jdbi;

import org.rabix.storage.postgres.jdbi.impl.JDBIAppRepository;
import org.rabix.storage.postgres.jdbi.impl.JDBIBackendRepository;
import org.rabix.storage.postgres.jdbi.impl.JDBIContextRecordRepository;
import org.rabix.storage.postgres.jdbi.impl.JDBIDAGRepository;
import org.rabix.storage.postgres.jdbi.impl.JDBIEventRepository;
import org.rabix.storage.postgres.jdbi.impl.JDBIIntermediaryFilesRepository;
import org.rabix.storage.postgres.jdbi.impl.JDBIJobRecordRepository;
import org.rabix.storage.postgres.jdbi.impl.JDBIJobRepository;
import org.rabix.storage.postgres.jdbi.impl.JDBILinkRecordRepository;
import org.rabix.storage.postgres.jdbi.impl.JDBIVariableRecordRepository;
import org.rabix.storage.postgres.jdbi.impl.JDBIJobStatsRecordRepository;
import org.rabix.storage.repository.TransactionHelper;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import org.skife.jdbi.v2.sqlobject.CreateSqlObject;
import org.skife.jdbi.v2.sqlobject.Transaction;

public abstract class JDBIRepositoryRegistry extends TransactionHelper {

  @CreateSqlObject
  public abstract JDBIAppRepository applicationRepository();
  
  @CreateSqlObject
  public abstract JDBIBackendRepository backendRepository();
  
  @CreateSqlObject
  public abstract JDBIDAGRepository dagRepository();
  
  @CreateSqlObject
  public abstract JDBIJobRepository jobRepository();

  @CreateSqlObject
  public abstract JDBIJobRecordRepository jobRecordRepository();
  
  @CreateSqlObject
  public abstract JDBILinkRecordRepository linkRecordRepository();
  
  @CreateSqlObject
  public abstract JDBIVariableRecordRepository variableRecordRepository();
  
  @CreateSqlObject
  public abstract JDBIContextRecordRepository contextRecordRepository();

  @CreateSqlObject
  public abstract JDBIJobStatsRecordRepository jobStatsRecordRepository();

  @CreateSqlObject
  public abstract JDBIEventRepository eventRepository();
  
  @CreateSqlObject
  public abstract JDBIIntermediaryFilesRepository intermediaryFilesRepository();
  
  @Transaction(TransactionIsolationLevel.READ_UNCOMMITTED)
  public <Result> Result doInTransaction(TransactionCallback<Result> callback) throws Exception {
    return callback.call();
  }
  
}
