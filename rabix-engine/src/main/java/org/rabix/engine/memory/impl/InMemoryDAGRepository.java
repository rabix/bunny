package org.rabix.engine.memory.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.rabix.bindings.model.dag.DAGContainer;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.engine.repository.DAGRepository;

import com.google.inject.Inject;

public class InMemoryDAGRepository implements DAGRepository {

  Map<UUID, DAGNode> dagRepository;
  
  @Inject
  public InMemoryDAGRepository() {
    this.dagRepository = new ConcurrentHashMap<UUID, DAGNode>();
  }

  @Override
  public synchronized void insert(UUID rootId, DAGNode dag) {
    dagRepository.put(rootId, dag);
  }

  @Override
  public synchronized DAGNode get(String id, UUID rootId) {
    return getIdFromDAG(id, dagRepository.get(rootId));
  }
  
  public synchronized DAGNode getIdFromDAG(String id, DAGNode node) {
    Map<String, DAGNode> allNodes = new HashMap<String, DAGNode>();
    populateNodes(node, allNodes);
    return allNodes.get(id);
  }
  
  private synchronized void populateNodes(DAGNode node, Map<String, DAGNode> allNodes) {
    allNodes.put(node.getId(), node);
    if (node instanceof DAGContainer) {
      for (DAGNode child : ((DAGContainer) node).getChildren()) {
        populateNodes(child, allNodes);
      }
    }
  }

}
