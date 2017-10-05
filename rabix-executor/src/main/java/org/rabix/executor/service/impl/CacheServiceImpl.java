package org.rabix.executor.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.helper.FileValueHelper;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.common.helper.ChecksumHelper;
import org.rabix.common.json.BeanSerializer;
import org.rabix.executor.config.FileConfiguration;
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
  private FileConfiguration fileConfiguration;
  private StorageConfiguration storageConfig;
  private boolean mockWorker;

  @Inject
  public CacheServiceImpl(StorageConfiguration storageConfig, Configuration configuration,
                          FileConfiguration fileConfiguration) {
    this.storageConfig = storageConfig;
    this.configuration = configuration;
    this.fileConfiguration = fileConfiguration;
    if(isCacheEnabled()) {
      this.cacheDirectory = new File(configuration.getString("cache.directory"));
    }
    this.mockWorker = configuration.getBoolean("executor.mock_worker", false);
  }

  @Override
  public boolean isCacheEnabled() {
    return configuration.getBoolean("cache.enabled", false);
  }

  @Override
  public boolean isMockWorkerEnabled() {
    return configuration.getBoolean("executor.mock_worker", false);
  }

  @Override
  public void cache(Job job) {
    File workingDir = storageConfig.getWorkingDir(job);

    File cacheDir = new File(workingDir.getParentFile(), getCacheName(workingDir.getName()));
    if (!cacheDir.exists()) {
      cacheDir.mkdirs();
    }
    File jobFile = new File(cacheDir, JOB_FILE);

    job = fillCacheProperties(job);
    try {
      FileUtils.writeStringToFile(jobFile, BeanSerializer.serializePartial(job), "UTF-8");
    } catch (IOException e) {
      logger.warn("Failed to cache Job " + job.getId(), e);
    }
  }

  private String getCacheName(String filename) {
    return "." + filename + ".meta";
  }

  /**
   * Checks whether a job is equal to another job that is cached
   * @param job
   * @param cachedJob
   * @return true if jobs are equal
   */
  private boolean jobsEqual(Job job, Job cachedJob, Bindings bindings) throws BindingException {
    if (!BindingsFactory.create(job).loadAppObject(job.getApp()).equals(BindingsFactory.create(cachedJob).loadAppObject(cachedJob.getApp()))) {
      return false;
    }

    if (!mockWorker) {
      // FileValue equality is different for caching.
      Map<String, Object> inputs = job.getInputs();
      Map<String, Object> cachedInputs = cachedJob.getInputs();

      for (String inputPortKey : inputs.keySet()) {
        if (!cachedInputs.containsKey(inputPortKey)) {
          return false;
        }

        Object value = inputs.get(inputPortKey);
        Object cachedValue = cachedInputs.get(inputPortKey);
        if (value == null && cachedValue == null) {
          continue;
        }

        if (!cacheValuesEqual(value, cachedValue)) {
          return false;
        }
      }

      /*
       * Integrity check is necessary for output files.
       * Cache directory might be corrupted
       */
      Map<String, Object> cachedOutputs = cachedJob.getOutputs();
      for (String outputPortKey : cachedOutputs.keySet()) {
        Object cachedValue = cachedOutputs.get(outputPortKey);
        if(cachedValue instanceof FileValue &&
                !checkFileIntegrity((FileValue) cachedValue)) {
          return false;
        }
      }

    }
    return true;
  }

  private boolean cacheValuesEqual(Object value, Object cachedValue) {
    try {
      if (value instanceof List && cachedValue instanceof List) {
        for (int i = 0; i < ((List) value).size(); i++) {
          if (!cacheValuesEqual(((List) value).get(i), ((List) cachedValue).get(i))) {
            return false;
          }
        }
        return true;
      } else if (value instanceof Map && cachedValue instanceof Map) {
        for (Object key : ((Map<?, ?>) value).keySet()) {
          if (!cacheValuesEqual(((Map) value).get(key), ((Map) cachedValue).get(key))) {
            return false;
          }
        }
        return true;
      } else if (value instanceof FileValue && cachedValue instanceof FileValue) {
        return checkFileEquals((FileValue) value, (FileValue) cachedValue);
      } else {
        return value.equals(cachedValue);
      }
    } catch (Exception e){
      logger.warn("Cache values are not equal. Exception thrown {}", e.getMessage());
      return false;
    }
  }

  /**
   * Check whether input files are equal. For backwards compatibility,
   * if {@code cachedValue} is missing some of the necessary fields, it does
   * favor equality of the files.
   * @param value
   * @param cachedValue
   * @return
   */
  private boolean checkFileEquals(FileValue value, FileValue cachedValue) {
    try {
      if (cachedValue.getSize() != 0 &&
              !value.getSize().equals(cachedValue.getSize()))
        return false;
      if (cachedValue.getChecksum() != null &&
              !value.getChecksum().equals(cachedValue.getChecksum()))
        return false;
      // This control does not apply to the opposite situation for the reason explained in the method's doc.
      if (value.getSecondaryFiles()!=null && cachedValue.getSecondaryFiles()==null)
        return false;
      else if (value.getSecondaryFiles()!=null && cachedValue.getSecondaryFiles()!=null) {
        for (int i = 0; i < value.getSecondaryFiles().size(); i++) {
          if (!checkFileEquals(value.getSecondaryFiles().get(i), cachedValue.getSecondaryFiles().get(i))) {
            return false;
          }
        }
      }
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  private boolean checkFileIntegrity(FileValue value) {
    try {
      File file = new File(value.getPath());
      if (file.exists() && file.isFile()) {
        if (value.getSize() != 0 &&
                !value.getSize().equals(file.length())) {
          return false;
        }
        if (value.getChecksum() != null) {
          String checksumValue = ChecksumHelper.checksum(file, fileConfiguration.checksumAlgorithm());
          if (!checksumValue.equals(value.getChecksum())) {
            return false;
          }
        }
        if (value.getSecondaryFiles() != null) {
          for (int i = 0; i < value.getSecondaryFiles().size(); i++) {
            if (!checkFileIntegrity(value.getSecondaryFiles().get(i))) {
              return false;
            }
          }
        }
      } else {
        return false;
      }
    } catch(Exception e) {
      return false;
    }
    return true;
  }

  /**
   * Cache properties are required parameters to compare essential inputs to ones
   * that are cached in an earlier execution. These include App hash, input files' checksum, etc.
   * @param job : Job that is being analyzed for cache results.
   * @return Job : a modified job instance.
   */
  private Job fillCacheProperties(Job job) {

    Map<String, Object> inputs = job.getInputs();
    for (String inputPortKey : inputs.keySet()){
      List<FileValue> inputFiles = FileValueHelper.getFilesFromValue(inputs.get(inputPortKey));

      if (inputFiles.isEmpty()) {
        continue;
      }

      FileValue fileValue;
      for (int i=0; i<inputFiles.size(); i++) {
        fileValue = inputFiles.get(i);
        if (fileValue.getChecksum() == null || fileValue.getChecksum().isEmpty()) {

          File inputFile = new File(fileValue.getPath());
          if (inputFile.exists() && inputFile.isFile()) {
            String value = ChecksumHelper.checksum(inputFile, fileConfiguration.checksumAlgorithm());
            fileValue.setChecksum(value);
            inputFiles.set(i, fileValue);
          }
        }
      }
      inputs.put(inputPortKey, inputFiles);
    }

    return Job.cloneWithInputs(job, inputs);
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

      Job cachedJob = BeanSerializer.deserialize(FileUtils.readFileToString(jobFile, "UTF-8"), Job.class);

      if (!cachedJob.getStatus().equals(JobStatus.COMPLETED)) {
        return null;
      }

      if (!mockWorker) {
        job = fillCacheProperties(job);
      }

      if (!jobsEqual(job, cachedJob, bindings)) {
        logger.warn("Cached job is different. Doing dry run");
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
