package org.rabix.engine.jdbi;

import org.rabix.engine.dao.AppRepository;
import org.rabix.engine.dao.BackendRepository;
import org.rabix.engine.dao.ContextRecordRepository;
import org.rabix.engine.dao.DAGRepository;
import org.rabix.engine.dao.JobBackendRepository;
import org.rabix.engine.dao.JobRecordRepository;
import org.rabix.engine.dao.JobRepository;
import org.rabix.engine.dao.LinkRecordRepository;
import org.rabix.engine.dao.VariableRecordRepository;
import org.skife.jdbi.v2.sqlobject.CreateSqlObject;
import org.skife.jdbi.v2.sqlobject.Transaction;

public abstract class JDBIRepositoryRegistry {

  @CreateSqlObject
  public abstract AppRepository applicationRepository();
  
  @CreateSqlObject
  public abstract BackendRepository backendRepository();
  
  @CreateSqlObject
  public abstract DAGRepository dagRepository();
  
  @CreateSqlObject
  public abstract JobRepository jobRepository();
  
  @CreateSqlObject
  public abstract JobBackendRepository jobBackendRepository();
  
  @CreateSqlObject
  public abstract JobRecordRepository jobRecordRepository();
  
  @CreateSqlObject
  public abstract LinkRecordRepository linkRecordRepository();
  
  @CreateSqlObject
  public abstract VariableRecordRepository variableRecordRepository();
  
  @CreateSqlObject
  public abstract ContextRecordRepository contextRecordRepository();
  
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
