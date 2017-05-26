package org.rabix.storage.model.scatter;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.storage.model.scatter.impl.ScatterCartesianStrategy;
import org.rabix.storage.model.scatter.impl.ScatterZipStrategy;

import com.google.common.base.Preconditions;

public class ScatterStrategyFactory {

  public ScatterStrategy create(DAGNode dagNode) throws BindingException {
    Preconditions.checkNotNull(dagNode);
    
    switch (dagNode.getScatterMethod()) {
    case dotproduct:
      return new ScatterZipStrategy(dagNode);
    case flat_crossproduct:
    case nested_crossproduct:
      return new ScatterCartesianStrategy(dagNode);
    default:
      throw new BindingException("Scatter method " + dagNode.getScatterMethod() + " is not supported.");
    }
  }
  
}
