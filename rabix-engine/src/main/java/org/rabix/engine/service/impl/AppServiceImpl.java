package org.rabix.engine.service.impl;

import org.rabix.bindings.model.Application;
import org.rabix.bindings.model.dag.DAGContainer;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.helper.ChecksumHelper;
import org.rabix.common.helper.ChecksumHelper.HashAlgorithm;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.json.BeanSerializer;
import org.rabix.engine.service.AppService;
import org.rabix.engine.store.lru.app.AppCache;
import org.rabix.engine.store.repository.AppRepository;

import com.google.inject.Inject;

public class AppServiceImpl implements AppService {
  
  private AppRepository appRepository;
  private AppCache appCache;
  
  @Inject
  public AppServiceImpl(AppRepository appRepository, AppCache appCache) {
    this.appRepository = appRepository;
    this.appCache = appCache;
  }
  
  @Override
  public Application get(String id) {
    Application app = appCache.get(id);
    if(app == null) {
      app = BeanSerializer.deserialize(appRepository.get(id), Application.class);
      appCache.put(id, app);
    }
    return app;
  }
  
  @Override
  public void loadDB(DAGNode node) {
    loadApp(node);
  }
  
  @Override
  public void loadApp(DAGNode node) {
    String id = hashDagNode(node.getApp());
    appRepository.insert(id, JSONHelper.writeSortedWithoutIdentation(JSONHelper.convertToJsonNode(node.getApp())));
    node.setAppHash(id);
    node.setApp(null);
    if(node instanceof DAGContainer) {
      for (DAGNode child : ((DAGContainer) node).getChildren()) {
        loadApp(child);
      }
    }
  }
  
  public static String hashDagNode(Application app) {
    String cachedSortedAppText = JSONHelper.writeSortedWithoutIdentation(JSONHelper.convertToJsonNode(app));
    String cachedAppHash = ChecksumHelper.checksum(cachedSortedAppText, HashAlgorithm.SHA1);
    return cachedAppHash;
  }

}
