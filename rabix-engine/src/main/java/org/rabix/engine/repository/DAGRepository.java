package org.rabix.engine.repository;

import org.rabix.bindings.model.dag.DAGNode;

import java.util.UUID;

public interface DAGRepository {

  void insert(UUID rootId, DAGNode dag);
  
  DAGNode get(String name, UUID rootId);
}
