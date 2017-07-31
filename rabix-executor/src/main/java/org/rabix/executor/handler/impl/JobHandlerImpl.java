package org.rabix.executor.handler.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import javax.inject.Inject;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.rabix.backend.api.callback.WorkerStatusCallback;
import org.rabix.backend.api.callback.WorkerStatusCallbackException;
import org.rabix.backend.api.engine.EngineStub;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.helper.FileValueHelper;
import org.rabix.bindings.mapper.FileMappingException;
import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.bindings.model.DirectoryValue;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.bindings.model.requirement.DockerContainerRequirement;
import org.rabix.bindings.model.requirement.FileRequirement;
import org.rabix.bindings.model.requirement.FileRequirement.SingleFileRequirement;
import org.rabix.bindings.model.requirement.FileRequirement.SingleInputDirectoryRequirement;
import org.rabix.bindings.model.requirement.FileRequirement.SingleInputFileRequirement;
import org.rabix.bindings.model.requirement.FileRequirement.SingleTextFileRequirement;
import org.rabix.bindings.model.requirement.LocalContainerRequirement;
import org.rabix.bindings.model.requirement.Requirement;
import org.rabix.bindings.transformer.FileTransformer;
import org.rabix.common.helper.ChecksumHelper.HashAlgorithm;
import org.rabix.common.helper.CloneHelper;
import org.rabix.common.service.download.DownloadService;
import org.rabix.common.service.download.DownloadService.DownloadResource;
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
import org.rabix.executor.handler.JobHandler;
import org.rabix.executor.model.JobData;
import org.rabix.executor.pathmapper.InputFileMapper;
import org.rabix.executor.pathmapper.OutputFileMapper;
import org.rabix.executor.service.CacheService;
import org.rabix.executor.service.FilePermissionService;
import org.rabix.executor.service.JobDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.assistedinject.Assisted;

public class JobHandlerImpl implements JobHandler {

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
  private Job cachedJob;
  private EngineStub<?, ?, ?> engineStub;

  private DockerConfigation dockerConfig;
  private StorageConfiguration storageConfiguration;
  private ContainerHandler containerHandler;
  private DockerClientLockDecorator dockerClient;

  private final WorkerStatusCallback statusCallback;
  
  private final FilePermissionService filePermissionService;
  private final CacheService cacheService;

  private boolean setPermissions;

  @Inject
  public JobHandlerImpl(
      @Assisted Job job, @Assisted EngineStub<?, ?, ?> engineStub, 
      JobDataService jobDataService, Configuration configuration, StorageConfiguration storageConfig, 
      DockerConfigation dockerConfig, FileConfiguration fileConfiguration, 
      DockerClientLockDecorator dockerClient, WorkerStatusCallback statusCallback,
      CacheService cacheService, FilePermissionService filePermissionService, 
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
    this.cacheService = cacheService;
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

      Bindings bindings = BindingsFactory.create(job);
      statusCallback.onInputFilesDownloadStarted(job);
      try {
        downloadInputFiles(job, bindings);
      } catch (Exception e) {
        statusCallback.onInputFilesDownloadFailed(job);
        throw e;
      }
      statusCallback.onInputFilesDownloadCompleted(job);

      job = FileValueHelper.mapInputFilePaths(job, inputFileMapper);
      job = bindings.preprocess(job, workingDir, null);

      if (cacheService.isCacheEnabled()) {
        Map<String, Object> results = cacheService.find(job);
        if (results != null) {
          logger.info("Job {} is successfully copied from cache", job.getName());
          containerHandler = new CompletedContainerHandler(job);
          containerHandler.start();
          return;
        }
      }

      cachedJob = (Job) CloneHelper.deepCopy(job);
      cacheService.cache(cachedJob);
      
      List<Requirement> combinedRequirements = new ArrayList<>();
      combinedRequirements.addAll(bindings.getHints(job));
      combinedRequirements.addAll(bindings.getRequirements(job));

      stageFileRequirements(combinedRequirements);

      if (bindings.isSelfExecutable(job)) {
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
    Set<FileValue> fileValues = flattenFiles(FileValueHelper.getInputFiles(job));
    
    final Set<DownloadResource> downloadRecources = new HashSet<>();
    for (FileValue fileValue : fileValues) {
      downloadRecources.add(new DownloadResource(fileValue.getLocation(), fileValue.getPath(), fileValue.getName(), fileValue instanceof DirectoryValue));
      if (fileValue.getSecondaryFiles() != null) {
        for (FileValue secondaryFileValue : fileValue.getSecondaryFiles()) {
          downloadRecources.add(new DownloadResource(secondaryFileValue.getLocation(), secondaryFileValue.getPath(), secondaryFileValue.getName(), secondaryFileValue instanceof DirectoryValue));
        }
      }
    }
    downloadService.download(workingDir, downloadRecources, job.getConfig());
    
    // TODO refactor ASAP
    FileValueHelper.updateInputFiles(job, new FileTransformer() {
      @Override
      public FileValue transform(FileValue fileValue) {
        FileValue newFileValue = fileValue;
        if (fileValue.getLocation() != null) {
          DownloadResource downloadResource = findByLocation(fileValue.getLocation(), downloadRecources);
          if (downloadResource != null) {
            newFileValue = cloneWithPath(downloadResource.getPath(), newFileValue);
          }
          if (fileValue.getSecondaryFiles() != null) {
            List<FileValue> secondaryFiles = new ArrayList<>();
            for (FileValue secondaryFile : fileValue.getSecondaryFiles()) {
              FileValue newSecondaryFile = transform(secondaryFile);
              secondaryFiles.add(newSecondaryFile);
            }
            newFileValue = cloneWithSecondaryFiles(secondaryFiles, newFileValue);
          }
        }
        return newFileValue;
      }
      
      private DownloadResource findByLocation(String location, Set<DownloadResource> downloadResources) {
        for (DownloadResource downloadResource : downloadRecources) {
          if (location.equals(downloadResource.getLocation())) {
            return downloadResource;
          }
        }
        return null;
      }

      private FileValue cloneWithPath(String path, FileValue fileValue) {
        if (fileValue instanceof DirectoryValue) {
          return DirectoryValue.cloneWithPath((DirectoryValue) fileValue, path);
        }
        return FileValue.cloneWithPath(fileValue, path);
      }

      private FileValue cloneWithSecondaryFiles(List<FileValue> secondaryFiles, FileValue fileValue) {
        if (fileValue instanceof DirectoryValue) {
          return DirectoryValue.cloneWithSecondaryFiles((DirectoryValue) fileValue, secondaryFiles);
        }
        return FileValue.cloneWithSecondaryFiles(fileValue, secondaryFiles);
      }
    });
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

      Map<String, String> stagedFiles = new HashMap<>();

      for (SingleFileRequirement fileRequirement : fileRequirements) {
        logger.info("Process file requirement {}", fileRequirement);

        File destinationFile = new File(workingDir, fileRequirement.getFilename());
        if (fileRequirement instanceof SingleTextFileRequirement) {
          FileUtils.writeStringToFile(destinationFile, ((SingleTextFileRequirement) fileRequirement).getContent());
          continue;
        }
        if (fileRequirement instanceof SingleInputFileRequirement || fileRequirement instanceof SingleInputDirectoryRequirement) {
          FileValue content = ((SingleInputFileRequirement) fileRequirement).getContent();
          if (FileValue.isLiteral(content)) {
            if (fileRequirement instanceof SingleInputDirectoryRequirement) {
              destinationFile.mkdirs();
            } else {
              destinationFile.createNewFile();              
            }
            return;
          }
          
          String path = ((SingleInputFileRequirement) fileRequirement).getContent().getPath();
          String mappedPath = inputFileMapper.map(path, job.getConfig());
          stagedFiles.put(path, destinationFile.getPath());
          File file = new File(mappedPath);

          if (!file.exists()) {
            continue;
          }
          boolean isLinkEnabled = ((SingleInputFileRequirement) fileRequirement).isLinkEnabled();
          if (file.isFile()) {
            if (isLinkEnabled) {
              Files.createLink(destinationFile.toPath(), file.toPath()); // use hard link
            } else {
              FileUtils.copyFile(file, destinationFile); // use copy
            }
          } else {
            FileUtils.copyDirectory(file, destinationFile); // use copy
          }
        }
      }

      try {
        job = FileValueHelper.updateInputFiles(job, fileValue -> {
          if (stagedFiles.containsKey(fileValue.getPath())) {
            fileValue.setPath(stagedFiles.get(fileValue.getPath()));
          }

          return fileValue;
        });
      } catch (BindingException e) {
        throw new FileMappingException(e);
      }

    } catch (IOException e) {
      throw new ExecutorException("Failed to process file requirements.", e);
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
  public Job postprocess(boolean isTerminal) throws ExecutorException {
    logger.debug("postprocess(id={})", job.getId());
    try {
      Bindings bindings = BindingsFactory.create(job);
      
      Map<String, Object> results = cacheService.find(job);
      if (results != null) {
        job = Job.cloneWithOutputs(job, results);
        job = Job.cloneWithStatus(job, JobStatus.COMPLETED);
        return job;
      }
      
      String standardErrorLog = bindings.getStandardErrorLog(job);
      if (standardErrorLog == null) {
        standardErrorLog = DEFAULT_ERROR_FILE;
      }
      containerHandler.dumpContainerLogs(new File(workingDir, standardErrorLog));

      if (!isSuccessful()) {
        uploadOutputFiles(job, bindings);
        return job;
      }
      if (setPermissions) {
        filePermissionService.execute(job);
      }
      job = bindings.postprocess(job, workingDir, enableHash? hashAlgorithm : null, null);
      containerHandler.dumpCommandLine();
      
      statusCallback.onOutputFilesUploadStarted(job);
      uploadOutputFiles(job, bindings);
      statusCallback.onOutputFilesUploadCompleted(job);

      job = FileValueHelper.mapOutputFilePaths(job, outputFileMapper);
      
      JobData jobData = jobDataService.find(job.getId(), job.getRootId());
      jobData = JobData.cloneWithResult(jobData, job.getOutputs());
      jobDataService.save(jobData);

      cachedJob = Job.cloneWithStatus(cachedJob, JobStatus.COMPLETED);
      cachedJob = Job.cloneWithOutputs(cachedJob, job.getOutputs());
      cacheService.cache(cachedJob);
      
      logger.debug("Command line tool {} returned result {}.", job.getId(), job.getOutputs());
      return job;
    } catch (ContainerException e) {
      throw new ExecutorException("Failed to query container.", e);
    } catch (BindingException e) {
      throw new ExecutorException("Could not collect outputs.", e);
    } catch (UploadServiceException e) {
      throw new ExecutorException("Could not upload outputs.", e);
    } catch (WorkerStatusCallbackException e) {
      throw new ExecutorException("Could not call executor callback.", e);
    }
  }
  
  public void removeContainer() {
    try {
      if(isSuccessful()) {
        containerHandler.removeContainer();
      }
    } catch (ExecutorException e) {
      logger.debug("Failed to remove container");
    }
    
  }

  private void uploadOutputFiles(final Job job, final Bindings bindings) throws BindingException, UploadServiceException {
    if (storageConfiguration.getBackendStore().equals(BackendStore.LOCAL)) {
      return;
    }
    Set<FileValue> fileValues = flattenFiles(FileValueHelper.getOutputFiles(job));
    fileValues.addAll(bindings.getProtocolFiles(workingDir));
    
    File cmdFile = new File(workingDir, COMMAND_LOG);
    if (cmdFile.exists()) {
      String cmdFilePath = cmdFile.getAbsolutePath();
      fileValues.add(new FileValue(null, cmdFilePath, null, null, null, null, cmdFile.getName()));
    }
    
    File jobErrFile = new File(workingDir, DEFAULT_ERROR_FILE);
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
      if(isSuccessful()) {
        containerHandler.removeContainer();
      }
    } catch (ContainerException e) {
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
      throw new ExecutorException("Couldn't check if container is running or not.", e);
    }
  }

  @Override
  public int getExitStatus() throws ExecutorException {
    logger.debug("getExitStatus()");
    try {
      return containerHandler.getProcessExitStatus();
    } catch (ContainerException e) {
      throw new ExecutorException("Couldn't get process exit value.", e);
    }
  }

  @Override
  public boolean isSuccessful() throws ExecutorException {
    logger.debug("isSuccessful()");
    int processExitStatus = getExitStatus();
    return isSuccessful(processExitStatus);
  }

  @Override
  public boolean isSuccessful(int processExitCode) throws ExecutorException {
    try {
      Bindings bindings = BindingsFactory.create(job);
      return bindings.isSuccessful(job, processExitCode);
    } catch (BindingException e) {
      throw new ExecutorException("Failed to create Bindings", e);
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
