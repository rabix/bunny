package org.rabix.bindings.cwl.service;

import java.util.Map;

import org.rabix.bindings.cwl.bean.CWLJob;
import org.rabix.bindings.cwl.bean.CWLOutputPort;
import org.rabix.bindings.cwl.expression.CWLExpressionException;

public interface CWLMetadataService {

  Map<String, Object> processMetadata(CWLJob job, Object value, CWLOutputPort outputPort, Object outputBinding);
 
  Object evaluateMetadataExpressions(CWLJob job, Object self, Object metadata) throws CWLExpressionException;
  
}
