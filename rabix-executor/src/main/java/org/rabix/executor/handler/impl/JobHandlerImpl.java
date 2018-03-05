package org.rabix.executor.handler.impl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.rabix.backend.api.callback.WorkerStatusCallback;
import org.rabix.backend.api.callback.WorkerStatusCallbackException;
import org.rabix.backend.api.engine.EngineStub;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.helper.FileValueHelper;
import org.rabix.bindings.mapper.FileMappingException;
import org.rabix.bindings.mapper.FilePathMapper;
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
import org.rabix.common.helper.ChecksumHelper.HashAlgorithm;
import org.rabix.common.helper.CloneHelper;
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
import org.rabix.executor.handler.JobHandler;
import org.rabix.executor.model.JobData;
import org.rabix.executor.pathmapper.InputFileMapper;
import org.rabix.executor.pathmapper.OutputFileMapper;
import org.rabix.executor.service.CacheService;
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
  private final FilePathMapper inputFileMapper;
  private final FilePathMapper outputFileMapper;

  private Job job;
  private Job cachedJob;
  private EngineStub<?, ?, ?> engineStub;

  private DockerConfigation dockerConfig;
  private StorageConfiguration storageConfiguration;
  private ContainerHandler containerHandler;

  private final WorkerStatusCallback statusCallback;
 
  private final CacheService cacheService;

  private ContainerHandlerFactory containerHandlerFactory;

  @Inject
  public JobHandlerImpl(
      @Assisted Job job, @Assisted EngineStub<?, ?, ?> engineStub, JobDataService jobDataService,
      StorageConfiguration storageConfig, DockerConfigation dockerConfig, FileConfiguration fileConfiguration,
      WorkerStatusCallback statusCallback, CacheService cacheService, UploadService uploadService,
      @InputFileMapper FilePathMapper inputFileMapper, @OutputFileMapper FilePathMapper outputFileMapper, ContainerHandlerFactory containerHandlerFactory) {
    this.job = job;
    this.engineStub = engineStub;
    this.storageConfiguration = storageConfig;
    this.dockerConfig = dockerConfig;
    this.jobDataService = jobDataService;
    this.statusCallback = statusCallback;
    this.cacheService = cacheService;
    this.workingDir = storageConfig.getWorkingDir(job);
    this.uploadService = uploadService;
    this.inputFileMapper = inputFileMapper;
    this.outputFileMapper = outputFileMapper;
    this.enableHash = fileConfiguration.calculateFileChecksum();
    this.hashAlgorithm = fileConfiguration.checksumAlgorithm();
    this.containerHandlerFactory = containerHandlerFactory;
  }

  @Override
  public void start() throws ExecutorException {
    logger.info("Start command line tool for id={}", job.getId());
    try {
      job = statusCallback.onJobReady(job);
      /*
       * If cache service and mock worker are enabled,
       * executor will simply pass the results before binding
       * or checking on inputs.
       */
      if(cacheService.isCacheEnabled() && cacheService.isMockWorkerEnabled()) {
        cachedJob = (Job) CloneHelper.deepCopy(job);

        if (cacheService.isCacheEnabled()) {
          Map<String, Object> results = cacheService.find(job);
          if (results != null) {
            logger.info("Job {} is successfully copied from cache by mocking", job.getName());
            containerHandler = new CompletedContainerHandler(job);
            containerHandler.start();
            return;
          }
        }
      }

      Bindings bindings = BindingsFactory.create(job);

      job = bindings.preprocess(job, workingDir, null);

      job = FileValueHelper.mapInputFilePaths(job, inputFileMapper);

      /*
       * Cache service is enabled but mocking is not.
       * Inputs will be thoroughly scanned after binding.
       */
      if (cacheService.isCacheEnabled() && !cacheService.isMockWorkerEnabled()) {
        Map<String, Object> results = cacheService.find(job);
        if (results != null) {
          logger.info("Job {} is successfully copied from cache without mocking", job.getName());
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
        containerHandler = containerHandlerFactory.create(job, containerRequirement);
      }
      containerHandler.start();
    } catch (Exception e) {
      String message = String.format("Execution failed for %s. %s", job.getName(), e.getMessage());
      throw new ExecutorException(message, e);
    }
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
        File destinationFile = new File(workingDir, fileRequirement.getFilename());
        if (fileRequirement instanceof SingleTextFileRequirement) {
          FileUtils.writeStringToFile(destinationFile, ((SingleTextFileRequirement) fileRequirement).getContent().replaceAll(Matcher.quoteReplacement("\\$"), "\\$"));
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
          
          URI location = URI.create(content.getLocation());
          String path = location.getScheme()!=null ? Paths.get(location).toString() : content.getPath();
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
            String path = stagedFiles.get(fileValue.getPath());
            fileValue.setPath(path);
            fileValue.setLocation(Paths.get(path).toUri().toString());
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
      Path errFile = workingDir.toPath().resolve(standardErrorLog != null ? standardErrorLog : DEFAULT_ERROR_FILE);
      
      String processExitMessage = containerHandler.getProcessExitMessage();
      try {
        if (!StringUtils.isEmpty(processExitMessage) && !Files.exists(errFile))
          Files.write(errFile, processExitMessage.getBytes());
      } catch (IOException e) {
        throw new ExecutorException("Couldn't write error file", e);
      }
      
      job = bindings.postprocess(job, workingDir, enableHash? hashAlgorithm : null, null);  
      
      if (!isSuccessful()) {
        uploadOutputFiles(job, bindings);
        return job;
      }
      
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
    Set<FileValue> fileValues = FileValueHelper.getOutputFiles(job);
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
    try {
      return containerHandler.getProcessExitStatus();
    } catch (ContainerException e) {
      throw new ExecutorException("Couldn't get process exit value.", e);
    }
  }
  
  @Override
  public String getErrorLog() throws ExecutorException {
    try {
      return containerHandler.getProcessExitMessage();
    } catch (ContainerException e) {
      throw new ExecutorException("Couldn't get process exit value.", e);
    }
  }

  @Override
  public boolean isSuccessful() throws ExecutorException {
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
}
