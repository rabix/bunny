package org.rabix.engine.service.impl;

import java.util.UUID;

import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.engine.service.DAGNodeService;
import org.rabix.engine.store.lru.dag.DAGCache;
import org.rabix.engine.store.repository.DAGRepository;

import com.google.inject.Inject;

public class DAGNodeServiceImpl implements DAGNodeService {

  private DAGCache dagCache;
  private DAGRepository dagRepository;

  @Inject
  public DAGNodeServiceImpl(DAGRepository dagRepository, DAGCache dagCache) {
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
