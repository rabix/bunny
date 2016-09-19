package org.rabix.bindings.cwl.service;

import java.util.Map;

import org.rabix.bindings.cwl.bean.Draft3Job;
import org.rabix.bindings.cwl.bean.Draft3OutputPort;
import org.rabix.bindings.cwl.expression.Draft3ExpressionException;

public interface Draft3MetadataService {

  Map<String, Object> processMetadata(Draft3Job job, Object value, Draft3OutputPort outputPort, Object outputBinding);
 
  Object evaluateMetadataExpressions(Draft3Job job, Object self, Object metadata) throws Draft3ExpressionException;
  
}
