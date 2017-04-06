package org.rabix.executor.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.model.Job;
import org.rabix.common.helper.ChecksumHelper;
import org.rabix.common.helper.ChecksumHelper.HashAlgorithm;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.json.BeanSerializer;
import org.rabix.executor.config.StorageConfiguration;
import org.rabix.executor.service.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class CacheServiceImpl implements CacheService {

  private final static Logger logger = LoggerFactory.getLogger(CacheService.class);

  public final static String JOB_FILE = "job.json";
  
  private File cacheDirectory;
  private Configuration configuration;
  private StorageConfiguration storageConfig;

  @Inject
  public CacheServiceImpl(StorageConfiguration storageConfig, Configuration configuration) {
    this.storageConfig = storageConfig;
    this.configuration = configuration;
    if(isCacheEnabled())
    this.cacheDirectory = new File(configuration.getString("cache.directory"));
  }

  @Override
  public boolean isCacheEnabled() {
    return configuration.getBoolean("cache.enabled", false);
  }
  
  @Override
  public void cache(Job job) {
    File workingDir = storageConfig.getWorkingDir(job);
    
    File cacheDir = new File(workingDir.getParentFile(), getCacheName(workingDir.getName()));
    if (!cacheDir.exists()) {
      cacheDir.mkdirs();
    }
    File jobFile = new File(cacheDir, JOB_FILE);
    try {
      FileUtils.writeStringToFile(jobFile, BeanSerializer.serializePartial(job), "UTF-8");
    } catch (IOException e) {
      logger.warn("Failed to cache Job " + job.getId(), e);
    }
  }
  
  private String getCacheName(String filename) {
    return "." + filename + ".meta";
  }
  
  @Override
  public Map<String, Object> find(Job job) {
    try {
      File cacheDir = storageConfig.getWorkingDirWithoutRoot(job);
      cacheDir = new File(cacheDirectory, cacheDir.getPath());
      cacheDir = new File(cacheDir.getParentFile(), getCacheName(cacheDir.getName()));
      
      logger.info("Trying to find cached results in the directory {}", cacheDir);
      if (!cacheDir.exists()) {
        logger.info("Cache directory doesn't exist. Directory {}", cacheDir);
        return null;
      }
      logger.info("Cache directory exists. Directory {}", cacheDir);

      Bindings bindings = BindingsFactory.create(job);
      
      File jobFile = new File(cacheDir, JOB_FILE);
      if (!jobFile.exists()) {
        logger.info("Cached Job file not found");
        return null;
      }
      
      String appText = BeanSerializer.serializePartial(bindings.loadAppObject(job.getApp()));
      String sortedAppText = JSONHelper.writeSortedWithoutIdentation(JSONHelper.readJsonNode(appText));
      String appHash = ChecksumHelper.checksum(sortedAppText, HashAlgorithm.SHA1);

      Job cachedJob = BeanSerializer.deserialize(FileUtils.readFileToString(jobFile, "UTF-8"), Job.class);
      String cachedAppText = BeanSerializer.serializePartial(bindings.loadAppObject(cachedJob.getApp()));
      String cachedSortedAppText = JSONHelper.writeSortedWithoutIdentation(JSONHelper.readJsonNode(cachedAppText));
      String cachedAppHash = ChecksumHelper.checksum(cachedSortedAppText, HashAlgorithm.SHA1);

      if (!cachedAppHash.equals(appHash)) {
        return null;
      }
      File workingDir = storageConfig.getWorkingDir(job);
      File destinationCacheDir = new File(workingDir.getParentFile(), cacheDir.getName());
      destinationCacheDir.mkdirs();
      FileUtils.copyDirectory(cacheDir, destinationCacheDir);
      return cachedJob.getOutputs();
    } catch (BindingException e) {
      logger.error("Failed to find Bindings", e);
    } catch (IOException e) {
      logger.error("Failed to read result", e);
    }
    return null;
  }

}
