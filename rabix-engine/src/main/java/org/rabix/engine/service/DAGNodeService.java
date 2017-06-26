package org.rabix.engine.service;

import java.util.UUID;

import org.rabix.bindings.model.dag.DAGNode;

public interface DAGNodeService {

  DAGNode get(String id, UUID rootId, String dagHash);
  
  /**
   * Loads node into the repository recursively
   */
  String put(DAGNode node, UUID rootId);
  
}
