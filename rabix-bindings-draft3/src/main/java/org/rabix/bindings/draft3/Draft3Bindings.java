package org.rabix.bindings.draft3;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.ProtocolAppProcessor;
import org.rabix.bindings.ProtocolCommandLineBuilder;
import org.rabix.bindings.ProtocolFilePathMapper;
import org.rabix.bindings.ProtocolFileValueProcessor;
import org.rabix.bindings.ProtocolProcessor;
import org.rabix.bindings.ProtocolRequirementProvider;
import org.rabix.bindings.ProtocolTranslator;
import org.rabix.bindings.ProtocolType;
import org.rabix.bindings.draft3.bean.Draft3JobApp;
import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.bindings.model.Application;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.bindings.model.requirement.Requirement;
import org.rabix.bindings.model.requirement.ResourceRequirement;
import org.rabix.bindings.transformer.FileTransformer;

public class Draft3Bindings implements Bindings {

  private final ProtocolType protocolType;
  
  private final ProtocolTranslator translator;
  private final ProtocolAppProcessor appProcessor;
  private final ProtocolFileValueProcessor fileValueProcessor;
  
  private final ProtocolProcessor processor;
  private final ProtocolFilePathMapper filePathMapper;
  
  private final ProtocolCommandLineBuilder commandLineBuilder;
  private final ProtocolRequirementProvider requirementProvider;
  
  public Draft3Bindings() throws BindingException {
    this.protocolType = ProtocolType.DRAFT3;
    this.filePathMapper = new Draft3FilePathMapper();
    this.processor = new Draft3Processor();
    this.commandLineBuilder = new Draft3CommandLineBuilder();
    this.fileValueProcessor = new Draft3FileValueProcessor();
    this.translator = new Draft3Translator();
    this.requirementProvider = new Draft3RequirementProvider();
    this.appProcessor = new Draft3AppProcessor();
  }
  
  @Override
  public String loadApp(String uri) throws BindingException {
    return appProcessor.loadApp(uri);
  }
  
  @Override
  public Application loadAppObject(String uri) throws BindingException {
    Draft3JobApp application = (Draft3JobApp) appProcessor.loadAppObject(uri);
    if (!Draft3JobApp.DRAFT_3_VERSION.equals(application.getCwlVersion())) {
      throw new BindingException(uri + " is not an CWL Draft-3 application");
    }
    return application;
  }
  
  @Override
  public boolean canExecute(Job job) throws BindingException {
    return appProcessor.isSelfExecutable(job);
  }
  
  @Override
  public Job preprocess(Job job, File workingDir) throws BindingException {
    return processor.preprocess(job, workingDir);
  }
  
  @Override
  public boolean isSuccessful(Job job, int statusCode) throws BindingException {
    return processor.isSuccessful(job, statusCode);
  }

  @Override
  public Job postprocess(Job job, File workingDir) throws BindingException {
    return processor.postprocess(job, workingDir);
  }

  @Override
  public String buildCommandLine(Job job) throws BindingException {
    return commandLineBuilder.buildCommandLine(job);
  }

  @Override
  public List<String> buildCommandLineParts(Job job) throws BindingException {
    return commandLineBuilder.buildCommandLineParts(job);
  }

  @Override
  public Set<FileValue> getInputFiles(Job job) throws BindingException {
    return fileValueProcessor.getInputFiles(job);
  }
  
  @Override
  public Set<FileValue> getInputFiles(Job job, FilePathMapper fileMapper) throws BindingException {
    return fileValueProcessor.getInputFiles(job, fileMapper);
  }

  @Override
  public Set<FileValue> getOutputFiles(Job job, boolean visiblePorts) throws BindingException {
    return fileValueProcessor.getOutputFiles(job, visiblePorts);
  }
  
  @Override
  public Set<FileValue> getFlattenedInputFiles(Job job) throws BindingException {
    return fileValueProcessor.getFlattenedInputFiles(job);
  }

  @Override
  public Set<FileValue> getFlattenedOutputFiles(Job job, boolean onlyVisiblePorts) throws BindingException {
    return fileValueProcessor.getFlattenedOutputFiles(job, onlyVisiblePorts);
  }

  
  @Override
  public Job updateInputFiles(Job job, FileTransformer fileTransformer) throws BindingException {
    return fileValueProcessor.updateInputFiles(job, fileTransformer);
  }

  @Override
  public Job updateOutputFiles(Job job, FileTransformer fileTransformer) throws BindingException {
    return fileValueProcessor.updateOutputFiles(job, fileTransformer);
  }
  
  @Override
  public Set<FileValue> getProtocolFiles(File workingDir) throws BindingException {
    Set<FileValue> files = new HashSet<>();
    
    File jobFile = new File(workingDir, Draft3Processor.JOB_FILE);
    if (jobFile.exists()) {
      String jobFilePath = jobFile.getAbsolutePath();
      files.add(new FileValue(null, jobFilePath, null, null, null, null));
    }
    
    File resultFile = new File(workingDir, Draft3Processor.RESULT_FILENAME);
    if (resultFile.exists()) {
      String resultFilePath = resultFile.getAbsolutePath();
      files.add(new FileValue(null, resultFilePath, null, null, null, null));
    }
    return files;
  }
  
  @Override
  public Job mapInputFilePaths(Job job, FilePathMapper fileMapper) throws BindingException {
    return filePathMapper.mapInputFilePaths(job, fileMapper);
  }

  @Override
  public Job mapOutputFilePaths(Job job, FilePathMapper fileMapper) throws BindingException {
    return filePathMapper.mapOutputFilePaths(job, fileMapper);
  }

  @Override
  public List<Requirement> getRequirements(Job job) throws BindingException {
    return requirementProvider.getRequirements(job);
  }

  @Override
  public List<Requirement> getHints(Job job) throws BindingException {
    return requirementProvider.getHints(job);
  }
  
  @Override
  public ResourceRequirement getResourceRequirement(Job job) throws BindingException {
    return requirementProvider.getResourceRequirement(job);
  }
  
  @Override
  public DAGNode translateToDAG(Job job) throws BindingException {
    return translator.translateToDAG(job);
  }

  @Override
  public void validate(Job job) throws BindingException {
    appProcessor.validate(job);
  }
  
  @Override
  public ProtocolType getProtocolType() {
    return protocolType;
  }

}
