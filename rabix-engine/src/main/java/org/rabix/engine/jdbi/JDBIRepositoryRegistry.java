package org.rabix.engine.jdbi;

import org.rabix.engine.jdbi.impl.*;
import org.rabix.engine.repository.TransactionHelper;
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
  public abstract JDBIJobBackendRepository jobBackendRepository();

  @CreateSqlObject
  public abstract JDBIJobRecordRepository jobRecordRepository();
  
  @CreateSqlObject
  public abstract JDBILinkRecordRepository linkRecordRepository();
  
  @CreateSqlObject
  public abstract JDBIVariableRecordRepository variableRecordRepository();
  
  @CreateSqlObject
  public abstract JDBIRootJobRepository rootJobRepository();

  @Transaction
  public <Result> Result doInTransaction(TransactionCallback<Result> callback) throws TransactionException {
    return callback.call();
  }
  
}
