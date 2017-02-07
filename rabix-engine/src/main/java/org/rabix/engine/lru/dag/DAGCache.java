package org.rabix.engine.lru.dag;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.rabix.bindings.model.dag.DAGContainer;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.helper.ChecksumHelper;
import org.rabix.common.helper.ChecksumHelper.HashAlgorithm;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.json.BeanSerializer;
import org.rabix.engine.lru.LRUCache;
import org.rabix.engine.model.JobRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class DAGCache extends LRUCache<String, DAGNode> {
  
  Map<UUID, String> rootIdDag;
  private final Logger logger = LoggerFactory.getLogger(DAGCache.class);
  
  
  @Inject
  public DAGCache() {
    super("DAGCache");
    rootIdDag = new HashMap<UUID, String>();
  }
  
  public DAGCache(int cacheSize) {
    super("DAGCache", cacheSize);
    rootIdDag = new HashMap<UUID, String>();
  }
  
  public DAGNode get(String id, UUID rootId) {
    DAGNode res = null;
    if(rootIdDag.containsKey(rootId)) {
      res = get(rootIdDag.get(rootId));
      logger.debug(String.format("DAGNode rootId=%s, id=%s found in cache", rootId, id));
      logger.debug(String.format("Cache size=%d", size()));
    }
    return res != null ? getIdFromDAG(id, res) : null;
  }
  
  public DAGNode get(String id, UUID rootId, JobRecord job) {
    DAGNode res = null;
    if(job.getDagCache() != null) {
      res = get(job.getDagCache());
      logger.debug(String.format("DAGNode rootId=%s, id=%s found in cache", rootId, id));
      logger.debug(String.format("Cache size=%d", size()));
    }
    else if(rootIdDag.containsKey(rootId) && res == null) {
      job.setDagCache(rootIdDag.get(rootId));
      res = get(rootIdDag.get(rootId));
      logger.debug(String.format("DAGNode rootId=%s, id=%s found in cache", rootId, id));
      logger.debug(String.format("Cache size=%d", size()));
    }
    return res != null ? getIdFromDAG(id, res) : null;    
  }
  
  public void put(DAGNode dagNode, UUID rootId) {
    String cacheKey = cacheDagNode(dagNode);
    rootIdDag.put(rootId, cacheKey);
    put(cacheKey, dagNode);
  }
  
  public DAGNode getIdFromDAG(String id, DAGNode node) {
    Map<String, DAGNode> allNodes = new HashMap<String, DAGNode>();
    populateNodes(node, allNodes);
    return allNodes.get(id);
  }
  
  private void populateNodes(DAGNode node, Map<String, DAGNode> allNodes) {
    allNodes.put(node.getName(), node);
    if (node instanceof DAGContainer) {
      for (DAGNode child : ((DAGContainer) node).getChildren()) {
        populateNodes(child, allNodes);
      }
    }
  }
  
  public static String cacheDagNode(DAGNode dagNode) {
    String dagText = BeanSerializer.serializeFull(dagNode);
    String cachedSortedDAGText = JSONHelper.writeSortedWithoutIdentation(JSONHelper.readJsonNode(dagText));
    String cachedDAGHash = ChecksumHelper.checksum(cachedSortedDAGText, HashAlgorithm.SHA1);
    return cachedDAGHash;
  }
  
}
