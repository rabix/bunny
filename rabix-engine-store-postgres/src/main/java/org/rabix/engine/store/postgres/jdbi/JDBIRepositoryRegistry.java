package org.rabix.engine.store.postgres.jdbi;

import org.rabix.engine.store.repository.TransactionHelper;
import org.rabix.engine.store.postgres.jdbi.impl.JDBIAppRepository;
import org.rabix.engine.store.postgres.jdbi.impl.JDBIBackendRepository;
import org.rabix.engine.store.postgres.jdbi.impl.JDBIContextRecordRepository;
import org.rabix.engine.store.postgres.jdbi.impl.JDBIDAGRepository;
import org.rabix.engine.store.postgres.jdbi.impl.JDBIEventRepository;
import org.rabix.engine.store.postgres.jdbi.impl.JDBIIntermediaryFilesRepository;
import org.rabix.engine.store.postgres.jdbi.impl.JDBIJobRecordRepository;
import org.rabix.engine.store.postgres.jdbi.impl.JDBIJobRepository;
import org.rabix.engine.store.postgres.jdbi.impl.JDBILinkRecordRepository;
import org.rabix.engine.store.postgres.jdbi.impl.JDBIVariableRecordRepository;
import org.rabix.engine.store.postgres.jdbi.impl.JDBIJobStatsRecordRepository;
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
