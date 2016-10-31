package org.rabix.bindings.cwl.processor;

import org.rabix.bindings.model.ApplicationPort;

public interface CWLPortProcessorCallback {

  CWLPortProcessorResult process(Object value, ApplicationPort port) throws Exception;
  
}
