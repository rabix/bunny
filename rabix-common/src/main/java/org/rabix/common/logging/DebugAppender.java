package org.rabix.common.logging;

import org.slf4j.Logger;

/**
 * Created by luka on 3/7/17.
 */
public class DebugAppender {

  private Logger logger;
  private StringBuilder buffer;

  public DebugAppender(Logger logger) {
    this.logger = logger;
    if (logger.isDebugEnabled()) {
      buffer = new StringBuilder();
    }
  }

  public DebugAppender append(Object... messages) {
    if (logger.isDebugEnabled()) {
      for (Object m: messages) {
        buffer.append(m);
      }
    }
    return this;
  }

  public String toString() {
    return logger.isDebugEnabled()? buffer.toString(): "";
  }


}
