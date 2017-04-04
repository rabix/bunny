package org.rabix.engine.db;

import org.rabix.bindings.model.Application;
import org.rabix.bindings.model.dag.DAGContainer;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.helper.ChecksumHelper;
import org.rabix.common.helper.ChecksumHelper.HashAlgorithm;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.json.BeanSerializer;
import org.rabix.engine.lru.app.AppCache;
import org.rabix.engine.repository.AppRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class AppDB {

  private AppCache appCache;
  private AppRepository appRepository;
  
  private final static Logger logger = LoggerFactory.getLogger(AppDB.class);
  
  @Inject
  public AppDB(AppRepository appRepository, AppCache appCache) {
    this.appRepository = appRepository;
    this.appCache = appCache;
  }
  
  public Application get(String id) {
    Application app = appCache.get(id);
    if(app == null) {
      logger.info("Get App {} from repository", id);
      app = BeanSerializer.deserialize(appRepository.get(id), Application.class);;
      appCache.put(id, app);
    }
    return app;
  }
  
  public void loadDB(DAGNode node) {
    loadApp(node);
  }
  
  public void loadApp(DAGNode node) {
    String id = hashDagNode(node.getApp());
    appRepository.insert(id, BeanSerializer.serializeFull(node.getApp()));
    node.setAppHash(id);
    node.setApp(null);
    if(node instanceof DAGContainer) {
      for (DAGNode child : ((DAGContainer) node).getChildren()) {
        loadApp(child);
      }
    }
  }
  
  public static String hashDagNode(Application app) {
    String appText = BeanSerializer.serializeFull(app);
    String cachedSortedAppText = JSONHelper.writeSortedWithoutIdentation(JSONHelper.readJsonNode(appText));
    String cachedAppHash = ChecksumHelper.checksum(cachedSortedAppText, HashAlgorithm.SHA1);
    return cachedAppHash;
  }

}
