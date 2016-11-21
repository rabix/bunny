package org.rabix.executor.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.model.Job;
import org.rabix.common.helper.ChecksumHelper;
import org.rabix.common.helper.ChecksumHelper.HashAlgorithm;
import org.rabix.common.helper.JSONHelper;
import org.rabix.executor.config.StorageConfiguration;
import org.rabix.executor.service.ResultCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;

public class ResultCacheServiceImpl implements ResultCacheService {

  private final static Logger logger = LoggerFactory.getLogger(ResultCacheService.class);

  private StorageConfiguration storageConfig;
  private Map<String, CachedResult> cachedResults;

  private File cacheDirectory;

  @Inject
  public ResultCacheServiceImpl(StorageConfiguration storageConfig, Configuration configuration) {
    this.storageConfig = storageConfig;
    this.cachedResults = new HashMap<>();
    
    this.cacheDirectory = new File(configuration.getString("cache.directory"));
    loadCachedResults(cacheDirectory, cacheDirectory, cachedResults);
  }

  private void loadCachedResults(File cacheDirectory, File directory, final Map<String, CachedResult> cachedResults) {
    try {
      if (!directory.isDirectory()) {
        return;
      }
      
      File[] files = directory.listFiles();
      for (File file : files) {
        loadCachedResults(cacheDirectory, file, cachedResults);
      }
      
      String id = directory.getAbsolutePath().substring(cacheDirectory.getAbsolutePath().length());
      if (StringUtils.isEmpty(id)) {
        return;
      }
      id = id.substring(1).replace("/", ".").trim();
      
      if (StringUtils.isEmpty(id)) {
        return;
      }
      File jobFile = new File(directory, "job.json");
      if (!jobFile.exists()) {
        return;
      }

      String oldJobJson = FileUtils.readFileToString(jobFile);
      JsonNode oldJobJsonNode = JSONHelper.readJsonNode(oldJobJson);
      JsonNode oldAppJsonNode = oldJobJsonNode.get("app");

      String oldSerializedApp = JSONHelper.writeSortedWithoutIdentation(oldAppJsonNode);
      String oldHash = ChecksumHelper.checksum(oldSerializedApp, HashAlgorithm.SHA1);

      File resultFile = new File(directory, "cwl.output.json");
      if (resultFile.exists()) {
        String resultJson = FileUtils.readFileToString(resultFile);
        cachedResults.put(id, new CachedResult(oldHash, JSONHelper.readMap(resultJson)));
      }
    } catch (IOException e) {
      logger.error("Failed to traverse cached directory {}", directory);
    }
  }
  
  @Override
  public Map<String, Object> findResultsFromCache(Job job) {
    CachedResult cachedResult = cachedResults.get(job.getName());
    return cachedResult != null ? cachedResult.getResult() : null;
  }
  
  @Override
  public Map<String, Object> findResultsFromCachingDir(Job job) {
    try {
      Bindings bindings = BindingsFactory.create(job);
      File workingDir = new File(cacheDirectory, storageConfig.getWorkingDirWithoutRoot(job).getPath());

      logger.info("Trying to find cached results in the directory {}", workingDir);

      if (!workingDir.exists()) {
        logger.info("Cache directory doesn't exist. Directory {}", workingDir);
        return null;
      }
      logger.info("Cache directory exists. Directory {}", workingDir);

      String serializedApp = JSONHelper.writeSortedWithoutIdentation(JSONHelper.readJsonNode(bindings.loadApp(job.getApp())));
      String newHash = ChecksumHelper.checksum(serializedApp, HashAlgorithm.SHA1);

      String oldJobJson = FileUtils.readFileToString(new File(workingDir, "job.json"));
      JsonNode oldJobJsonNode = JSONHelper.readJsonNode(oldJobJson);
      JsonNode oldAppJsonNode = oldJobJsonNode.get("app");

      String oldSerializedApp = JSONHelper.writeSortedWithoutIdentation(oldAppJsonNode);
      String oldHash = ChecksumHelper.checksum(oldSerializedApp, HashAlgorithm.SHA1);

      if (!oldHash.equals(newHash)) {
        return null;
      }

      switch (bindings.getProtocolType()) {
      case SB:
      case DRAFT2:
        File resultFile = new File(workingDir, "cwl.output.json");
        if (resultFile.exists()) {
          FileUtils.copyFile(resultFile, new File(storageConfig.getWorkingDir(job), "cwl.output.json"));

          Map<String, Object> inputs = JSONHelper.readMap(oldJobJsonNode.get("inputs"));
          @SuppressWarnings("unchecked")
          Map<String, Object> commonInputs = (Map<String, Object>) bindings.translateToCommon(inputs);
          Job newJob = Job.cloneWithInputs(job, commonInputs);
          return bindings.postprocess(newJob, workingDir, null, null).getOutputs();
        }
        break;
      default:
        break;
      }
    } catch (BindingException e) {
      logger.error("Failed to find Bindings", e);
    } catch (IOException e) {
      logger.error("Failed to read result", e);
    }
    return null;
  }

  public static class CachedResult {
    private String hash;
    private Map<String, Object> result;
    
    @JsonCreator
    public CachedResult(String hash, Map<String, Object> result) {
      this.hash = hash;
      this.result = result;
    }

    public String getHash() {
      return hash;
    }

    public void setHash(String hash) {
      this.hash = hash;
    }

    public Map<String, Object> getResult() {
      return result;
    }

    public void setResult(Map<String, Object> result) {
      this.result = result;
    }
    
  }
}
