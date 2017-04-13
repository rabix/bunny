package org.rabix.engine.model.scatter;

import org.rabix.bindings.BindingException;

public class ScatterStrategyException extends BindingException {

  /**
   * 
   */
  private static final long serialVersionUID = 3520029211594616403L;
  
  public ScatterStrategyException(String message) {
    super(message);
  }

  public ScatterStrategyException(Throwable e) {
    super(e);
  }

}
