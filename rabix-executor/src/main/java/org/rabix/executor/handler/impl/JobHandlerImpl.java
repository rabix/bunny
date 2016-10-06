package org.rabix.executor.handler.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.inject.Inject;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.mapper.FileMappingException;
import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.requirement.DockerContainerRequirement;
import org.rabix.bindings.model.requirement.FileRequirement;
import org.rabix.bindings.model.requirement.FileRequirement.SingleFileRequirement;
import org.rabix.bindings.model.requirement.FileRequirement.SingleInputDirectoryRequirement;
import org.rabix.bindings.model.requirement.FileRequirement.SingleInputFileRequirement;
import org.rabix.bindings.model.requirement.FileRequirement.SingleTextFileRequirement;
import org.rabix.bindings.model.requirement.LocalContainerRequirement;
import org.rabix.bindings.model.requirement.Requirement;
import org.rabix.common.helper.ChecksumHelper;
import org.rabix.common.helper.ChecksumHelper.HashAlgorithm;
import org.rabix.common.service.download.DownloadService;
import org.rabix.common.service.download.DownloadServiceException;
import org.rabix.common.service.upload.UploadService;
import org.rabix.common.service.upload.UploadServiceException;
import org.rabix.executor.ExecutorException;
import org.rabix.executor.config.DockerConfigation;
import org.rabix.executor.config.FileConfiguration;
import org.rabix.executor.config.StorageConfiguration;
import org.rabix.executor.config.StorageConfiguration.BackendStore;
import org.rabix.executor.container.ContainerException;
import org.rabix.executor.container.ContainerHandler;
import org.rabix.executor.container.ContainerHandlerFactory;
import org.rabix.executor.container.impl.CompletedContainerHandler;
import org.rabix.executor.container.impl.DockerContainerHandler.DockerClientLockDecorator;
import org.rabix.executor.engine.EngineStub;
import org.rabix.executor.handler.JobHandler;
import org.rabix.executor.model.JobData;
import org.rabix.executor.pathmapper.InputFileMapper;
import org.rabix.executor.pathmapper.OutputFileMapper;
import org.rabix.executor.service.FilePermissionService;
import org.rabix.executor.service.JobDataService;
import org.rabix.executor.service.ResultCacheService;
import org.rabix.executor.status.ExecutorStatusCallback;
import org.rabix.executor.status.ExecutorStatusCallbackException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.assistedinject.Assisted;

public class JobHandlerImpl implements JobHandler {

  private final String KEY_CHECKSUM = "checksum";

  private static final Logger logger = LoggerFactory.getLogger(JobHandlerImpl.class);

  private final File workingDir;

  private final JobDataService jobDataService;

  private final boolean enableHash;
  private final HashAlgorithm hashAlgorithm;
  
  private final UploadService uploadService;
  private final DownloadService downloadService;
  
  private final FilePathMapper inputFileMapper;
  private final FilePathMapper outputFileMapper;

  private Job job;
  private EngineStub<?, ?, ?> engineStub;

  private DockerConfigation dockerConfig;
  private StorageConfiguration storageConfiguration;
  private ContainerHandler containerHandler;
  private DockerClientLockDecorator dockerClient;

  private final ExecutorStatusCallback statusCallback;
  
  private final FilePermissionService filePermissionService;
  private final ResultCacheService localMemoizationService;

  private boolean setPermissions;

  @Inject
  public JobHandlerImpl(
      @Assisted Job job, @Assisted EngineStub<?, ?, ?> engineStub, 
      JobDataService jobDataService, Configuration configuration, StorageConfiguration storageConfig, 
      DockerConfigation dockerConfig, FileConfiguration fileConfiguration, 
      DockerClientLockDecorator dockerClient, ExecutorStatusCallback statusCallback,
      ResultCacheService localMemoizationService, FilePermissionService filePermissionService, 
      UploadService uploadService, DownloadService downloadService,
      @InputFileMapper FilePathMapper inputFileMapper, @OutputFileMapper FilePathMapper outputFileMapper) {
    this.job = job;
    this.engineStub = engineStub;
    this.storageConfiguration = storageConfig;
    this.dockerConfig = dockerConfig;
    this.jobDataService = jobDataService;
    this.dockerClient = dockerClient;
    this.statusCallback = statusCallback;
    this.filePermissionService = filePermissionService;
    this.localMemoizationService = localMemoizationService;
    this.workingDir = storageConfig.getWorkingDir(job);
    this.uploadService = uploadService;
    this.downloadService = downloadService;
    this.inputFileMapper = inputFileMapper;
    this.outputFileMapper = outputFileMapper;
    this.enableHash = fileConfiguration.calculateFileChecksum();
    this.hashAlgorithm = fileConfiguration.checksumAlgorithm();

    this.setPermissions = configuration.getBoolean("executor.set_permissions", false);
  }

  @Override
  public void start() throws ExecutorException {
    logger.info("Start command line tool for id={}", job.getId());
    try {
      job = statusCallback.onJobReady(job);
      
      Map<String, Object> results = localMemoizationService.findResultsFromCachingDir(job);
      if (results != null) {
        containerHandler = new CompletedContainerHandler(job);
        containerHandler.start();
        return;
      }

      Bindings bindings = BindingsFactory.create(job);
      
      statusCallback.onInputFilesDownloadStarted(job);
      try {
        downloadInputFiles(job, bindings);
      } catch (Exception e) {
        statusCallback.onInputFilesDownloadFailed(job);
        throw e;
      }
      statusCallback.onInputFilesDownloadCompleted(job);
      
      job = bindings.mapInputFilePaths(job, inputFileMapper);
      
      List<Requirement> combinedRequirements = new ArrayList<>();
      combinedRequirements.addAll(bindings.getHints(job));
      combinedRequirements.addAll(bindings.getRequirements(job));

      stageFileRequirements(combinedRequirements);

      job = bindings.preprocess(job, workingDir);

      if (bindings.canExecute(job)) {
        containerHandler = new CompletedContainerHandler(job);
      } else {
        Requirement containerRequirement = getRequirement(combinedRequirements, DockerContainerRequirement.class);
        if (containerRequirement == null || !dockerConfig.isDockerSupported()) {
          containerRequirement = new LocalContainerRequirement();
        }
        containerHandler = ContainerHandlerFactory.create(job, containerRequirement, dockerClient, statusCallback, storageConfiguration, dockerConfig);
      }
      containerHandler.start();
    } catch (Exception e) {
      String message = String.format("Execution failed for %s. %s", job.getId(), e.getMessage());
      throw new ExecutorException(message, e);
    }
  }

  private void downloadInputFiles(final Job job, final Bindings bindings) throws BindingException, DownloadServiceException {
    if (storageConfiguration.getBackendStore().equals(BackendStore.LOCAL)) {
      return;
    }
    Set<FileValue> fileValues = flattenFiles(bindings.getInputFiles(job));
    
    Set<String> paths = new HashSet<>();
    for (FileValue fileValue : fileValues) {
      paths.add(fileValue.getPath());
    }
    downloadService.download(workingDir, paths, job.getConfig());
  }
  
  private void stageFileRequirements(List<Requirement> requirements) throws ExecutorException, FileMappingException {
    try {
      FileRequirement fileRequirementResource = getRequirement(requirements, FileRequirement.class);
      if (fileRequirementResource == null) {
        return;
      }

      List<SingleFileRequirement> fileRequirements = fileRequirementResource.getFileRequirements();
      if (fileRequirements == null) {
        return;
      }
      for (SingleFileRequirement fileRequirement : fileRequirements) {
        logger.info("Process file requirement {}", fileRequirement);

        File destinationFile = new File(workingDir, fileRequirement.getFilename());
        if (fileRequirement instanceof SingleTextFileRequirement) {
          FileUtils.writeStringToFile(destinationFile, ((SingleTextFileRequirement) fileRequirement).getContent());
          continue;
        }
        if (fileRequirement instanceof SingleInputFileRequirement || fileRequirement instanceof SingleInputDirectoryRequirement) {
          String path = ((SingleInputFileRequirement) fileRequirement).getContent().getPath();
          String mappedPath = inputFileMapper.map(path, job.getConfig());
          File file = new File(mappedPath);
          if (!file.exists()) {
            continue;
          }
          if (file.isFile()) {
            FileUtils.copyFile(file, destinationFile);
          } else {
            FileUtils.copyDirectory(file, destinationFile);
          }
        }
      }
    } catch (IOException e) {
      logger.error("Failed to process file requirements.", e);
      throw new ExecutorException("Failed to process file requirements.");
    }
  }

  @SuppressWarnings("unchecked")
  private <T extends Requirement> T getRequirement(List<Requirement> requirements, Class<T> clazz) {
    for (Requirement requirement : requirements) {
      if (requirement.getClass().equals(clazz)) {
        return (T) requirement;
      }
    }
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Job postprocess(boolean isTerminal) throws ExecutorException {
    logger.debug("postprocess(id={})", job.getId());
    try {
      Bindings bindings = BindingsFactory.create(job);
      
      Map<String, Object> results = localMemoizationService.findResultsFromCachingDir(job);
      if (results != null) {
        job = Job.cloneWithOutputs(job, results);
        
        Set<FileValue> fileValues = bindings.getProtocolFiles(workingDir);
        Set<File> files = new HashSet<>();
        for (FileValue fileValue : fileValues) {
          files.add(new File(fileValue.getPath()));
        }
        uploadService.upload(files, storageConfiguration.getPhysicalExecutionBaseDir(), true, true, job.getConfig());
        job = bindings.mapOutputFilePaths(job, outputFileMapper);
        return job;
      }
      containerHandler.dumpContainerLogs(new File(workingDir, ERROR_LOG));

      if (!isSuccessful()) {
        uploadOutputFiles(job, bindings);
        return job;
      }
      if (setPermissions) {
        filePermissionService.execute(job);
      }
      job = bindings.postprocess(job, workingDir);

      if (enableHash) {
        Map<String, Object> outputs = job.getOutputs();
        Map<String, Object> outputsWithCheckSum = (Map<String, Object>) populateChecksum(outputs);
        job = Job.cloneWithOutputs(job, outputsWithCheckSum);
      }

      statusCallback.onOutputFilesUploadStarted(job);
      uploadOutputFiles(job, bindings);
      statusCallback.onOutputFilesUploadCompleted(job);

      job = bindings.mapOutputFilePaths(job, outputFileMapper);
      
      JobData jobData = jobDataService.find(job.getId(), job.getRootId());
      jobData = JobData.cloneWithResult(jobData, job.getOutputs());
      jobDataService.save(jobData);

      logger.debug("Command line tool {} returned result {}.", job.getId(), job.getOutputs());
      return job;
    } catch (ContainerException e) {
      logger.error("Failed to query container.", e);
      throw new ExecutorException("Failed to query container.", e);
    } catch (BindingException e) {
      logger.error("Could not collect outputs.", e);
      throw new ExecutorException("Could not collect outputs.", e);
    } catch (UploadServiceException e) {
      logger.error("Could not upload outputs.", e);
      throw new ExecutorException("Could not upload outputs.", e);
    } catch (ExecutorStatusCallbackException e) {
      logger.error("Could not call executor callback.", e);
      throw new ExecutorException("Could not call executor callback.", e);
    }
  }

  private void uploadOutputFiles(final Job job, final Bindings bindings) throws BindingException, UploadServiceException {
    if (storageConfiguration.getBackendStore().equals(BackendStore.LOCAL)) {
      return;
    }
    Set<FileValue> fileValues = flattenFiles(bindings.getOutputFiles(job, false));
    fileValues.addAll(bindings.getProtocolFiles(workingDir));
    
    File cmdFile = new File(workingDir, COMMAND_LOG);
    if (cmdFile.exists()) {
      String cmdFilePath = cmdFile.getAbsolutePath();
      fileValues.add(new FileValue(null, cmdFilePath, null, null, null, null, cmdFile.getName()));
    }
    
    File jobErrFile = new File(workingDir, ERROR_LOG);
    if (jobErrFile.exists()) {
      String jobErrFilePath = jobErrFile.getAbsolutePath();
      fileValues.add(new FileValue(null, jobErrFilePath, null, null, null, null, jobErrFile.getName()));
    }
    
    Set<File> files = new HashSet<>();
    for (FileValue fileValue : fileValues) {
      files.add(new File(fileValue.getPath()));
    }
    uploadService.upload(files, storageConfiguration.getPhysicalExecutionBaseDir(), true, true, job.getConfig());
  }

  public void stop() throws ExecutorException {
    logger.debug("stop(id={})", job.getId());
    if (containerHandler == null) {
      logger.debug("Container hasn't started yet.");
      return;
    }
    try {
      containerHandler.stop();
    } catch (ContainerException e) {
      logger.error("Failed to stop execution", e);
      throw new ExecutorException("Failed to stop execution.", e);
    }
  }

  public boolean isStarted() throws ExecutorException {
    logger.debug("isStarted()");
    if (containerHandler == null) {
      logger.debug("Container hasn't started yet.");
      return false;
    }
    try {
      return containerHandler.isStarted();
    } catch (ContainerException e) {
      logger.error("Failed to query container for status", e);
      throw new ExecutorException("Failed to query container for status.", e);
    }
  }

  public boolean isRunning() throws ExecutorException {
    if (containerHandler == null) {
      logger.debug("Container hasn't started yet.");
      return false;
    }
    try {
      return containerHandler.isRunning();
    } catch (ContainerException e) {
      logger.error("Couldn't check if container is running or not", e);
      throw new ExecutorException("Couldn't check if container is running or not.", e);
    }
  }

  @Override
  public int getExitStatus() throws ExecutorException {
    logger.debug("getExitStatus()");
    try {
      return containerHandler.getProcessExitStatus();
    } catch (ContainerException e) {
      logger.error("Couldn't get process exit value", e);
      throw new ExecutorException("Couldn't get process exit value.", e);
    }
  }

  @Override
  public boolean isSuccessful() throws ExecutorException {
    logger.debug("isSuccessful()");
    int processExitStatus = getExitStatus();
    return isSuccessful(processExitStatus);
  }

  @SuppressWarnings("unchecked")
  private void calculateChecksum(Object file) {
    Map<String, Object> fileMap = (Map<String, Object>) file;
    File f = new File((String) fileMap.get("path"));
    String checksum = ChecksumHelper.checksum(f, hashAlgorithm);
    if (checksum != null) {
      fileMap.put(KEY_CHECKSUM, checksum);
    }
  }

  @SuppressWarnings("unchecked")
  public Object populateChecksum(Object outputs) {
    if (outputs instanceof Map) {
      String mapClass = (String) ((Map<String, Object>) outputs).get("class");
      if (mapClass != null && mapClass.equals("File")) {
        calculateChecksum(outputs);
        return outputs;
      } else {
        Map<String, Object> outputsMap = (Map<String, Object>) outputs;
        Map<String, Object> result = new TreeMap<String, Object>();
        for (String output : outputsMap.keySet()) {
          Object value = outputsMap.get(output);
          result.put(output, populateChecksum(value));
        }
        return result;
      }
    } else if (outputs instanceof List) {
      List<Object> iter = (List<Object>) outputs;
      List<Object> resultList = new ArrayList<Object>();
      for (Object elem : iter) {
        resultList.add(populateChecksum(elem));
      }
      return resultList;
    } else {
      return outputs;
    }
  }

  @Override
  public boolean isSuccessful(int processExitCode) throws ExecutorException {
    try {
      Bindings bindings = BindingsFactory.create(job);
      return bindings.isSuccessful(job, processExitCode);
    } catch (BindingException e) {
      logger.error("Failed to create Bindings", e);
      throw new ExecutorException(e);
    }
  }

  public EngineStub<?, ?, ?> getEngineStub() {
    return engineStub;
  }
  
  private Set<FileValue> flattenFiles(Set<FileValue> fileValues) {
    Set<FileValue> flattenedFileValues = new HashSet<>();
    for (FileValue fileValue : fileValues) {
      flattenedFileValues.add(fileValue);
      if (fileValue.getSecondaryFiles() != null) {
        flattenedFileValues.addAll(fileValue.getSecondaryFiles());
      }
    }
    return flattenedFileValues;
  }


}
