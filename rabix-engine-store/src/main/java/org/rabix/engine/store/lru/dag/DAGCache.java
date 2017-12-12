package org.rabix.engine.store.lru.dag;

import com.google.inject.Inject;
import org.apache.commons.configuration.Configuration;
import org.rabix.bindings.model.dag.DAGContainer;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.helper.ChecksumHelper;
import org.rabix.common.helper.ChecksumHelper.HashAlgorithm;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.json.BeanSerializer;
import org.rabix.engine.store.lru.LRUCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DAGCache extends LRUCache<String, DAGNode> {

  private final Logger logger = LoggerFactory.getLogger(DAGCache.class);
  private final static String CACHE_NAME = "DAGCache";
  private static int DEFAULT_CACHE_SIZE = 16;

  @Inject
  public DAGCache(Configuration configuration) {
    super(CACHE_NAME, configuration.getInteger("cache.dag.size", DEFAULT_CACHE_SIZE));
    logger.debug("{} initialized with size={}", CACHE_NAME, getCacheSize());
  }

  public DAGCache(int cacheSize) {
    super(CACHE_NAME, cacheSize);
  }

  public DAGNode get(String id, UUID rootId, String dagHash) {
    DAGNode res = null;
    res = get(dagHash);
    return res != null ? getIdFromDAG(id, res) : null;
  }

  public String put(DAGNode dagNode, UUID rootId) {
    String dagHash = hashDagNode(dagNode);
    put(dagHash, dagNode);
    return dagHash;
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

  public static String hashDagNode(DAGNode dagNode) {
    String dagText = BeanSerializer.serializeFull(dagNode);
    String cachedSortedDAGText = JSONHelper.writeSortedWithoutIdentation(JSONHelper.readJsonNode(dagText));
    String cachedDAGHash = ChecksumHelper.checksum(cachedSortedDAGText, HashAlgorithm.SHA1);
    return cachedDAGHash;
  }

}
