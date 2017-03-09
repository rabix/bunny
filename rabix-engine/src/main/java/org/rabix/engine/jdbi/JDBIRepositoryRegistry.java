package org.rabix.engine.jdbi;

import org.rabix.engine.jdbi.impl.*;
import org.rabix.engine.repository.TransactionHelper;
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
  
  @Transaction(TransactionIsolationLevel.READ_UNCOMMITTED)
  public <Result> Result doInTransaction(TransactionCallback<Result> callback) throws Exception {
    return callback.call();
  }
  
}
