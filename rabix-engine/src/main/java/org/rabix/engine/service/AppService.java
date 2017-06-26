package org.rabix.engine.service;

import org.rabix.bindings.model.Application;
import org.rabix.bindings.model.dag.DAGNode;

public interface AppService {

  Application get(String id);
  
  void loadDB(DAGNode node);
  
  void loadApp(DAGNode node);
  
}
