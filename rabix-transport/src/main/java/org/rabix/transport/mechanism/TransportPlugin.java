package org.rabix.transport.mechanism;

public interface TransportPlugin<Q extends TransportQueue> {

  <T> ResultPair<T> send(Q destinationQueue, T entity);

  <T> void startReceiver(Q sourceQueue, Class<T> clazz, ReceiveCallback<T> receiveCallback, ErrorCallback errorCallback);
  
  void stopReceiver(Q sourceQueue);
  
  TransportPluginType getType();
  
  @FunctionalInterface
  public static interface ReceiveCallback<T> {
    void handleReceive(T entity) throws TransportPluginException;
  }
  
  @FunctionalInterface
  public static interface ErrorCallback {
    void handleError(Exception error);
  }
  
  public static class ResultPair<T> {
    private boolean success;
    
    private T result;
    
    private String message;
    private Exception exception;
    
    public ResultPair() {
    }
    
    public boolean isSuccess() {
      return success;
    }
    
    public T getResult() {
      return result;
    }
    
    public String getMessage() {
      return message;
    }
    
    public Exception getException() {
      return exception;
    }
    
    public static <T> ResultPair<T> success() {
      ResultPair<T> resultPair = new ResultPair<T>();
      resultPair.success = true;
      return resultPair;
    }
    
    public static <T> ResultPair<T> fail(String message, Exception exception) {
      ResultPair<T> resultPair = new ResultPair<T>();
      resultPair.success = false;
      resultPair.message = message;
      resultPair.exception = exception;
      return resultPair;
    }
  }
}
