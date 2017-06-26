package org.rabix.bindings.sb.processor;

import org.rabix.bindings.model.ApplicationPort;

public interface SBPortProcessorCallback {

  SBPortProcessorResult process(Object value, String id, Object schema, Object binding, ApplicationPort parentPort) throws Exception;
  
}
