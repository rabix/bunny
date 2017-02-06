package org.rabix.engine.db;

import org.apache.commons.io.FileUtils;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.helper.ChecksumHelper;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.helper.ChecksumHelper.HashAlgorithm;
import org.rabix.common.json.BeanSerializer;
import org.rabix.engine.lru.dag.DAGCache;
import org.rabix.engine.model.JobRecord;
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
  
  public DAGNode get(String id, String contextId) {
    DAGNode res = dagCache.get(id, contextId);
    if(res == null) {
      res = dagRepository.get(id, contextId);
      dagCache.put(dagRepository.get(InternalSchemaHelper.ROOT_NAME, contextId), contextId);
    }
    return res;
  }
  
  public DAGNode get(String id, String contextId, JobRecord job) {
    DAGNode res = dagCache.get(id, contextId, job);
    if(res == null) {
      res = dagRepository.get(id, contextId);
      dagCache.put(dagRepository.get(InternalSchemaHelper.ROOT_NAME, contextId), contextId);
    }
    return res;
  }
  
  /**
   * Loads node into the repository recursively
   */
  public void loadDB(DAGNode node, String contextId) {
    dagCache.put(node, contextId);
    dagRepository.insert(contextId, node);
  }
  
}
