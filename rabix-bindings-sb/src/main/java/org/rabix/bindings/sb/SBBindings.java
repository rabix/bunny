package org.rabix.bindings.sb;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.CommandLine;
import org.rabix.bindings.ProtocolAppProcessor;
import org.rabix.bindings.ProtocolCommandLineBuilder;
import org.rabix.bindings.ProtocolFileValueProcessor;
import org.rabix.bindings.ProtocolProcessor;
import org.rabix.bindings.ProtocolRequirementProvider;
import org.rabix.bindings.ProtocolTranslator;
import org.rabix.bindings.ProtocolType;
import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.bindings.model.Application;
import org.rabix.bindings.model.DataType;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.bindings.model.requirement.Requirement;
import org.rabix.bindings.model.requirement.ResourceRequirement;
import org.rabix.bindings.sb.helper.SBSchemaHelper;
import org.rabix.common.helper.ChecksumHelper.HashAlgorithm;

public class SBBindings implements Bindings {

  private final ProtocolType protocolType;
  
  private final ProtocolTranslator translator;
  private final ProtocolAppProcessor appProcessor;
  private final ProtocolFileValueProcessor fileValueProcessor;
  
  private final ProtocolProcessor processor;
  
  private final ProtocolCommandLineBuilder commandLineBuilder;
  private final ProtocolRequirementProvider requirementProvider;
  
  public SBBindings() throws BindingException {
    this.protocolType = ProtocolType.SB;
    this.processor = new SBProcessor();
    this.commandLineBuilder = new SBCommandLineBuilder();
    this.fileValueProcessor = new SBFileValueProcessor();
    this.translator = new SBTranslator();
    this.requirementProvider = new SBRequirementProvider();
    this.appProcessor = new SBAppProcessor();
  }
  
  @Override
  public String loadApp(String uri) throws BindingException {
    return appProcessor.loadApp(uri);
  }
  
  @Override
  public Application loadAppObject(String uri) throws BindingException {
    return appProcessor.loadAppObject(uri);
  }
  
  @Override
  public boolean canExecute(Job job) throws BindingException {
    return appProcessor.isSelfExecutable(job);
  }
  
  @Override
  public Job preprocess(Job job, File workingDir, FilePathMapper logFilePathMapper) throws BindingException {
    return processor.preprocess(job, workingDir, logFilePathMapper);
  }
  
  @Override
  public boolean isSuccessful(Job job, int statusCode) throws BindingException {
    return processor.isSuccessful(job, statusCode);
  }

  @Override
  public Job postprocess(Job job, File workingDir, HashAlgorithm hashAlgorithm, FilePathMapper logFilePathMapper) throws BindingException {
    return processor.postprocess(job, workingDir, hashAlgorithm, logFilePathMapper);
  }

  @Override
  public String buildCommandLine(Job job, File workingDir, FilePathMapper filePathMapper) throws BindingException {
    return commandLineBuilder.buildCommandLine(job, workingDir, filePathMapper);
  }

  @Override
  public List<String> buildCommandLineParts(Job job, File workingDir, FilePathMapper filePathMapper) throws BindingException {
    return commandLineBuilder.buildCommandLineParts(job, workingDir, filePathMapper);
  }
  
  @Override
  public CommandLine buildCommandLineObject(Job job, File workingDir, FilePathMapper filePathMapper) throws BindingException {
    return commandLineBuilder.buildCommandLineObject(job, workingDir, filePathMapper);
  }

  @Override
  public Set<FileValue> getInputFiles(Job job, FilePathMapper fileMapper) throws BindingException {
    return fileValueProcessor.getInputFiles(job, fileMapper);
  }
  
  @Override
  public Set<FileValue> getProtocolFiles(File workingDir) throws BindingException {
    Set<FileValue> files = new HashSet<>();
    
    File jobFile = new File(workingDir, SBProcessor.JOB_FILE);
    if (jobFile.exists()) {
      String jobFilePath = jobFile.getAbsolutePath();
      files.add(new FileValue(null, jobFilePath, null, null, null, null, jobFile.getName()));
    }
    
    File resultFile = new File(workingDir, SBProcessor.RESULT_FILENAME);
    if (resultFile.exists()) {
      String resultFilePath = resultFile.getAbsolutePath();
      files.add(new FileValue(null, resultFilePath, null, null, null, null, resultFile.getName()));
    }
    return files;
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
  
  @Override
  public Object transformInputs(Object value, Job job, Object transform) throws BindingException {
    return value;
  }

  @Override
  public String getStandardErrorLog(Job job) throws BindingException {
    return null;
  }

  @Override public DataType getDataTypeFromSchema(Object schema) {
    return SBSchemaHelper.readDataType(schema);
  }

  @Override public boolean isRequiredFromSchema(Object schema) {
    return SBSchemaHelper.isRequired(schema);
  }

  @Override
  public Object translateToSpecific(Object commonValue) throws BindingException {
    return SBValueTranslator.translateToSpecific(commonValue);
  }

  @Override
  public Object translateToCommon(Object nativeValue) throws BindingException {
    return SBValueTranslator.translateToCommon(nativeValue);
  }
}
