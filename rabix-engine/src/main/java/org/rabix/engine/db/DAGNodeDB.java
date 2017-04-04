package org.rabix.engine.db;

import java.util.UUID;

import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.engine.lru.dag.DAGCache;
import org.rabix.engine.repository.DAGRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * In-memory {@link DAGNode} repository
 */
public class DAGNodeDB {

  private final static Logger logger = LoggerFactory.getLogger(DAGNodeDB.class);
  
  private DAGCache dagCache;
  private DAGRepository dagRepository;

  @Inject
  public DAGNodeDB(DAGRepository dagRepository, DAGCache dagCache) {
    this.dagRepository = dagRepository;
    this.dagCache = dagCache;
  }
  
  /**
   * Gets node from the repository 
   */
  public DAGNode get(String id, UUID rootId, String dagHash) {
    DAGNode res = dagCache.get(id, rootId, dagHash);
    if(res == null) {
      res = dagRepository.get(id, rootId);
      logger.info("Get DAGNode {} from repository", id);
      dagCache.put(dagRepository.get(InternalSchemaHelper.ROOT_NAME, rootId), rootId);
    }
    return res;
  }
  
  /**
   * Loads node into the repository recursively
   */
  public String loadDB(DAGNode node, UUID rootId) {
    String dagHash = dagCache.put(node, rootId);
    dagRepository.insert(rootId, node);
    return dagHash;
  }
  
}
