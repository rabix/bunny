package org.rabix.bindings.cwl;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.rabix.bindings.cwl.bean.CWLCommandLineTool;
import org.rabix.bindings.cwl.bean.CWLJob;
import org.rabix.bindings.cwl.expression.CWLExpressionException;
import org.rabix.bindings.cwl.helper.CWLFileValueHelper;
import org.rabix.bindings.cwl.helper.CWLJobHelper;
import org.rabix.bindings.cwl.helper.CWLSchemaHelper;
import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.bindings.model.Application;
import org.rabix.bindings.model.DataType;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.bindings.model.requirement.Requirement;
import org.rabix.bindings.model.requirement.ResourceRequirement;
import org.rabix.bindings.transformer.FileTransformer;
import org.rabix.common.helper.ChecksumHelper.HashAlgorithm;

public class CWLBindings implements Bindings {

  private final ProtocolType protocolType;
  
  private final ProtocolTranslator translator;
  private final ProtocolAppProcessor appProcessor;
  private final ProtocolFileValueProcessor fileValueProcessor;
  
  private final ProtocolProcessor processor;
  private final ProtocolFilePathMapper filePathMapper;
  
  private final ProtocolCommandLineBuilder commandLineBuilder;
  private final ProtocolRequirementProvider requirementProvider;
  
  public CWLBindings() throws BindingException {
    this.protocolType = ProtocolType.CWL;
    this.filePathMapper = new CWLFilePathMapper();
    this.processor = new CWLProcessor();
    this.commandLineBuilder = new CWLCommandLineBuilder();
    this.fileValueProcessor = new CWLFileValueProcessor();
    this.translator = new CWLTranslator();
    this.requirementProvider = new CWLRequirementProvider();
    this.appProcessor = new CWLAppProcessor();
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
  public Job preprocess(Job job, File workingDir) throws BindingException {
    return processor.preprocess(job, workingDir);
  }
  
  @Override
  public boolean isSuccessful(Job job, int statusCode) throws BindingException {
    return processor.isSuccessful(job, statusCode);
  }

  @Override
  public Job postprocess(Job job, File workingDir, HashAlgorithm hashAlgorithm) throws BindingException {
    return processor.postprocess(job, workingDir, hashAlgorithm);
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
  public Job updateInputFiles(Job job, FileTransformer fileTransformer) throws BindingException {
    return fileValueProcessor.updateInputFiles(job, fileTransformer);
  }

  @Override
  public Job updateOutputFiles(Job job, FileTransformer fileTransformer) throws BindingException {
    return fileValueProcessor.updateOutputFiles(job, fileTransformer);
  }
  
  @Override
  public String getStandardErrorLog(Job job) throws BindingException {
    CWLJob cwlJob = CWLJobHelper.getCWLJob(job);
    try {
      if (cwlJob.getApp().isCommandLineTool()) {
        return ((CWLCommandLineTool) cwlJob.getApp()).getStderr(cwlJob);
      }
      return null;
    } catch (CWLExpressionException e) {
      throw new BindingException(e);
    }
  }
  
  @Override
  public Set<FileValue> getProtocolFiles(File workingDir) throws BindingException {
    Set<FileValue> files = new HashSet<>();
    
    File jobFile = new File(workingDir, CWLProcessor.JOB_FILE);
    if (jobFile.exists()) {
      String jobFilePath = jobFile.getAbsolutePath();
      files.add(new FileValue(null, jobFilePath, null, null, null, null, jobFile.getName()));
    }
    
    File resultFile = new File(workingDir, CWLProcessor.RESULT_FILENAME);
    if (resultFile.exists()) {
      String resultFilePath = resultFile.getAbsolutePath();
      files.add(new FileValue(null, resultFilePath, null, null, null, null, resultFile.getName()));
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
  
  @Override
  public Object transformInputs(Object value, Job job, Object transform) throws BindingException {
    return processor.transformInputs(value, job, transform);
  }

  @Override
  public Map<String, Object> translateFile(FileValue fileValue) {
    return CWLFileValueHelper.createFileRaw(fileValue);
  }

  @Override
  public DataType getDataTypeFromValue(Object input) {
    return CWLSchemaHelper.getDataTypeFromValue(input);
  }
}
