package org.rabix.bindings.draft3.service;

import java.util.Map;

import org.rabix.bindings.draft3.bean.Draft3Job;
import org.rabix.bindings.draft3.bean.Draft3OutputPort;
import org.rabix.bindings.draft3.expression.Draft3ExpressionException;

public interface Draft3MetadataService {

  Map<String, Object> processMetadata(Draft3Job job, Object value, Draft3OutputPort outputPort, Object outputBinding);
 
  Object evaluateMetadataExpressions(Draft3Job job, Object self, Object metadata) throws Draft3ExpressionException;
  
}
