package org.rabix.engine.db;

import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.engine.repository.DAGRepository;

import com.google.inject.Inject;

import java.util.UUID;

/**
 * In-memory {@link DAGNode} repository
 */
public class DAGNodeDB {

  private DAGRepository dagRepository;

  @Inject
  public DAGNodeDB(DAGRepository dagRepository) {
    this.dagRepository = dagRepository;
  }
  
  /**
   * Gets node from the repository 
   */
  public synchronized DAGNode get(String name, UUID rootId) {
    return dagRepository.get(name, rootId);
  }
  
  /**
   * Loads node into the repository recursively
   */
  public synchronized void loadDB(DAGNode node, UUID rootId) {
    dagRepository.insert(rootId, node);
  }
  
}
