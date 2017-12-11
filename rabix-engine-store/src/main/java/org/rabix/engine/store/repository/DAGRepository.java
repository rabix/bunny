package org.rabix.engine.store.repository;

import org.rabix.bindings.model.dag.DAGNode;

import java.util.UUID;

public interface DAGRepository {

  void insert(UUID rootId, DAGNode dag);

  DAGNode get(String id, UUID rootId);

  void delete(UUID rootId);
}
