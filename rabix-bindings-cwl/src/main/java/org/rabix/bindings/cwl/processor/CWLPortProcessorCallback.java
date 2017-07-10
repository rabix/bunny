package org.rabix.bindings.cwl.processor;

import org.rabix.bindings.model.ApplicationPort;

public interface CWLPortProcessorCallback {

  CWLPortProcessorResult process(Object value, String id, Object schema, Object binding, ApplicationPort parentPort) throws Exception;
  
}
