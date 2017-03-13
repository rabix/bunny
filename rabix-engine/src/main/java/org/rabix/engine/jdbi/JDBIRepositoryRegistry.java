package org.rabix.engine.jdbi;

import org.rabix.engine.jdbi.impl.JDBIAppRepository;
import org.rabix.engine.jdbi.impl.JDBIBackendRepository;
import org.rabix.engine.jdbi.impl.JDBIContextRecordRepository;
import org.rabix.engine.jdbi.impl.JDBIDAGRepository;
import org.rabix.engine.jdbi.impl.JDBIEventRepository;
import org.rabix.engine.jdbi.impl.JDBIJobRecordRepository;
import org.rabix.engine.jdbi.impl.JDBIJobRepository;
import org.rabix.engine.jdbi.impl.JDBILinkRecordRepository;
import org.rabix.engine.jdbi.impl.JDBIVariableRecordRepository;
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
  public abstract JDBIEventRepository eventRepository();
  
  @Transaction(TransactionIsolationLevel.READ_UNCOMMITTED)
  public <Result> Result doInTransaction(TransactionCallback<Result> callback) throws Exception {
    return callback.call();
  }
  
}
