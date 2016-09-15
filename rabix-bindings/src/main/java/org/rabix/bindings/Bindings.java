package org.rabix.bindings;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.bindings.model.Application;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.bindings.model.requirement.Requirement;
import org.rabix.bindings.model.requirement.ResourceRequirement;
import org.rabix.bindings.transformer.FileTransformer;

public interface Bindings {

  String loadApp(String appURI) throws BindingException;
  
  Application loadAppObject(String uri) throws BindingException;

  boolean canExecute(Job job) throws BindingException;
  
  boolean isSuccessful(Job job, int statusCode) throws BindingException;

  Job preprocess(Job job, File workingDir) throws BindingException;
  
  Job postprocess(Job job, File workingDir) throws BindingException;

  String buildCommandLine(Job job) throws BindingException;

  List<String> buildCommandLineParts(Job job) throws BindingException;

  Set<FileValue> getInputFiles(Job job) throws BindingException;
  
  Set<FileValue> getInputFiles(Job job, FilePathMapper fileMapper) throws BindingException;
  
  Set<FileValue> getOutputFiles(Job job, boolean onlyVisiblePorts) throws BindingException;

  Set<FileValue> getFlattenedInputFiles(Job job) throws BindingException;
  
  Set<FileValue> getFlattenedOutputFiles(Job job, boolean onlyVisiblePorts) throws BindingException;
  
  Job updateInputFiles(Job job, FileTransformer fileTransformer) throws BindingException;
  
  Job updateOutputFiles(Job job, FileTransformer fileTransformer) throws BindingException;
  
  Set<FileValue> getProtocolFiles(File workingDir) throws BindingException;
  
  Job mapInputFilePaths(Job job, FilePathMapper fileMapper) throws BindingException;

  Job mapOutputFilePaths(Job job, FilePathMapper fileMapper) throws BindingException;

  List<Requirement> getRequirements(Job job) throws BindingException;

  List<Requirement> getHints(Job job) throws BindingException;
  
  ResourceRequirement getResourceRequirement(Job job) throws BindingException;
  
  DAGNode translateToDAG(Job job) throws BindingException;

  void validate(Job job) throws BindingException;
  
  ProtocolType getProtocolType();

  
}
