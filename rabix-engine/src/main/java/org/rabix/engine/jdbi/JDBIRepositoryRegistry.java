package org.rabix.engine.jdbi;

import org.rabix.engine.jdbi.impl.JDBIAppRepository;
import org.rabix.engine.jdbi.impl.JDBIBackendRepository;
import org.rabix.engine.jdbi.impl.JDBIContextRecordRepository;
import org.rabix.engine.jdbi.impl.JDBIDAGRepository;
import org.rabix.engine.jdbi.impl.JDBIJobBackendRepository;
import org.rabix.engine.jdbi.impl.JDBIJobRecordRepository;
import org.rabix.engine.jdbi.impl.JDBIJobRepository;
import org.rabix.engine.jdbi.impl.JDBILinkRecordRepository;
import org.rabix.engine.jdbi.impl.JDBIVariableRecordRepository;
import org.skife.jdbi.v2.sqlobject.CreateSqlObject;
import org.skife.jdbi.v2.sqlobject.Transaction;

public abstract class JDBIRepositoryRegistry {

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
  public abstract JDBIContextRecordRepository contextRecordRepository();
  
  @Transaction
  public <Result> Result doInTransaction(TransactionCallback<Result> callback) throws TransactionException {
    return callback.call();
  }
  
  public static interface TransactionCallback<Result> {
    Result call() throws TransactionException;
  }
  
  public static class TransactionException extends Exception {
    
    /**
     * 
     */
    private static final long serialVersionUID = 4463416980242373365L;

    public TransactionException(Throwable t) {
      super(t);
    }
    
  }
  
  
}
