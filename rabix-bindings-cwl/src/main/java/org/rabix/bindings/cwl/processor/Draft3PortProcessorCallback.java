package org.rabix.bindings.cwl.processor;

import org.rabix.bindings.model.ApplicationPort;

public interface Draft3PortProcessorCallback {

  Draft3PortProcessorResult process(Object value, ApplicationPort port) throws Exception;
  
}
