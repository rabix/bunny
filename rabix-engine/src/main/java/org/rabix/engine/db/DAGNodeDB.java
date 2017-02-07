package org.rabix.engine.db;

import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.engine.lru.dag.DAGCache;
import org.rabix.engine.repository.DAGRepository;

import com.google.inject.Inject;

/**
 * In-memory {@link DAGNode} repository
 */
public class DAGNodeDB {

  private DAGRepository dagRepository;
  private DAGCache dagCache;

  @Inject
  public DAGNodeDB(DAGRepository dagRepository, DAGCache dagCache) {
    this.dagRepository = dagRepository;
    this.dagCache = dagCache;
  }
  
  /**
   * Gets node from the repository 
   */
  
  public DAGNode get(String id, String contextId, String dagHash) {
    DAGNode res = dagCache.get(id, contextId, dagHash);
    if(res == null) {
      res = dagRepository.get(id, contextId);
      dagCache.put(dagRepository.get(InternalSchemaHelper.ROOT_NAME, contextId), contextId);
    }
    return res;
  }
  
  /**
   * Loads node into the repository recursively
   */
  public String loadDB(DAGNode node, String contextId) {
    String dagHash = dagCache.put(node, contextId);
    dagRepository.insert(contextId, node);
    return dagHash;
  }
  
}
