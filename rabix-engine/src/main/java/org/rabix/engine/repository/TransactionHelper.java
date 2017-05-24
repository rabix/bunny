package org.rabix.engine.repository;

public abstract class TransactionHelper {

  public <Result> Result doInTransaction(TransactionCallback<Result> callback) throws Exception {
    return callback.call();
  }

  @FunctionalInterface
  public static interface TransactionCallback<Result> {
    Result call() throws Exception;
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
