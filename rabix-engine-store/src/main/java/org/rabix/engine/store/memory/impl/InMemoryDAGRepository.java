package org.rabix.engine.store.memory.impl;

import com.google.inject.Inject;
import org.rabix.bindings.model.dag.DAGContainer;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.engine.store.repository.DAGRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryDAGRepository implements DAGRepository {

  private final Map<UUID, DAGNode> dagRepository;

  @Inject
  public InMemoryDAGRepository() {
    this.dagRepository = new ConcurrentHashMap<>();
  }

  @Override
  public void insert(UUID rootId, DAGNode dag) {
    dagRepository.put(rootId, dag);
  }

  @Override
  public DAGNode get(String id, UUID rootId) {
    return getIdFromDAG(id, dagRepository.get(rootId));
  }

  public DAGNode getIdFromDAG(String id, DAGNode node) {
    Map<String, DAGNode> allNodes = new HashMap<String, DAGNode>();
    populateNodes(node, allNodes);
    return allNodes.get(id);
  }

  private void populateNodes(DAGNode node, Map<String, DAGNode> allNodes) {
    allNodes.put(node.getId(), node);
    if (node instanceof DAGContainer) {
      for (DAGNode child : ((DAGContainer) node).getChildren()) {
        populateNodes(child, allNodes);
      }
    }
  }

}
