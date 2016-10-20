package org.rabix.bindings;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.bindings.model.Application;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.bindings.model.requirement.Requirement;
import org.rabix.bindings.model.requirement.ResourceRequirement;
import org.rabix.bindings.transformer.FileTransformer;
import org.rabix.common.helper.ChecksumHelper.HashAlgorithm;

public interface Bindings {

  /**
   * Loads and resolves application from URL
   *
   * @param appURI         Application URL
   * @return            Application as string
   * @throws BindingException
   */
  String loadApp(String appURI) throws BindingException;
  
  /**
   * Loads application object from URL
   *
   * @param uri         Application URL
   * @return            Application as object
   * @throws BindingException
   */
  Application loadAppObject(String uri) throws BindingException;

  /**
   * Returns true if {@link Job} can be executed by itself (without a container)
   *
   * @param job         Job object
   * @return            true/false
   * @throws BindingException
   */
  boolean canExecute(Job job) throws BindingException;
  
  /**
   * Returns true if {@link Job} has been successfully executed
   *
   * @param job         Job object
   * @param statusCode  Command line tool status code
   * @return            true/false
   * @throws BindingException
   */
  boolean isSuccessful(Job job, int statusCode) throws BindingException;

  /**
   * Pre process the {@link Job}.
   * Note: Call pre process before Job execution
   *
   * @param job         Job object
   * @param workingDir  Working directory
   * @return            Pre processed Job object
   * @throws BindingException
   */
  Job preprocess(Job job, File workingDir) throws BindingException;
  
  /**
   * Post process the {@link Job}
   * Note: Call post process after successfull or failed Job execution
   *
   * @param job             Job object
   * @param workingDir      Working directory
   * @param hashAlgorithm   Checksum hash algorithm
   * @return                Post processed Job object
   * @throws BindingException
   */
  Job postprocess(Job job, File workingDir, HashAlgorithm hashAlgorithm) throws BindingException;

  /**
   * Builds command line as a string
   *
   * @param job             Job object
   * @param workingDir      Working directory
   * @param filePathMapper  FilePathMapper used to map workingDir when new files are being created
   * @return                Command line
   * @throws BindingException
   */
  String buildCommandLine(Job job, File workingDir, FilePathMapper filePathMapper) throws BindingException;

  /**
   * Builds command line as a list of command line parts
   *
   * @param job             Job object
   * @param workingDir      Working directory
   * @param filePathMapper  FilePathMapper used to map workingDir when new files are being created
   * @return                List of command line parts
   * @throws BindingException
   */
  List<String> buildCommandLineParts(Job job, File workingDir, FilePathMapper filePathMapper) throws BindingException;

  /**
   * Gets a set of input {@link FileValue} objects with their secondary files
   *
   * @param job         Job object
   * @return            FileValue objects
   * @throws BindingException
   */
  Set<FileValue> getInputFiles(Job job) throws BindingException;
  
  /**
   * Gets a set of input {@link FileValue} objects with their secondary files mapped with a {@link FilePathMapper}
   *
   * @param job         Job object
   * @param fileMapper  FileMapper object
   * @return            FileValue objects
   * @throws BindingException
   */
  @Deprecated
  Set<FileValue> getInputFiles(Job job, FilePathMapper fileMapper) throws BindingException;
  
  /**
   * Gets a set of output {@link FileValue} objects with their secondary files
   *
   * @param job                 Job object
   * @param onlyVisiblePorts    Returns only visible ports. Visible ports are global or terminal ports.
   * @return                    FileValue objects
   * @throws BindingException
   */
  Set<FileValue> getOutputFiles(Job job, boolean onlyVisiblePorts) throws BindingException;

  /**
   * Updates input files
   *
   * @param job             Job object
   * @param fileTransformer FileTransformer that transforms old file values into new ones
   * @return                Updated Job object
   * @throws BindingException
   */
  Job updateInputFiles(Job job, FileTransformer fileTransformer) throws BindingException;
  
  /**
   * Evaluates expression over the inputs
   * 
   * @param value           Input value
   * @param job             Job value
   * @param transform       Expression
   * @return                Transformed vallues
   * @throws BindingException
   */
  Object transformInputs(Object value, Job job, Object transform) throws BindingException;
  
  /**
   * Updates output files
   *
   * @param job             Job object
   * @param fileTransformer FileTransformer that transforms old file values into new ones
   * @return                Updated Job object
   * @throws BindingException
   */
  Job updateOutputFiles(Job job, FileTransformer fileTransformer) throws BindingException;
  
  /**
   * Returns files that are created by the protocol (CWL creates job.json, cwl.output.json)
   *
   * @param workingDir  Working directory
   * @return            FileValue objects
   * @throws BindingException
   */
  Set<FileValue> getProtocolFiles(File workingDir) throws BindingException;
  
  /**
   * Maps input file paths using the particular {@link FilePathMapper}
   *
   * @param job         Job object
   * @param fileMapper  FileMapper object
   * @return            Updated Job object
   * @throws BindingException
   */
  Job mapInputFilePaths(Job job, FilePathMapper fileMapper) throws BindingException;

  /**
   * Maps output file paths using the particular {@link FilePathMapper}
   *
   * @param job         Job object
   * @param fileMapper  FileMapper object
   * @return            Updated Job object
   * @throws BindingException
   */
  Job mapOutputFilePaths(Job job, FilePathMapper fileMapper) throws BindingException;

  /**
   * Gets list of requirements
   *
   * @param job         Job object
   * @return            List of Requirement objects
   * @throws BindingException
   */
  List<Requirement> getRequirements(Job job) throws BindingException;

  /**
   * Gets list of hints
   *
   * @param job         Job object
   * @return            List of Hint objects
   * @throws BindingException
   */
  List<Requirement> getHints(Job job) throws BindingException;
  
  /**
   * Gets standard error log file name
   * 
   * @param job         Job object
   * @return            Standard error log
   * @throws BindingException
   */
  String getStandardErrorLog(Job job) throws BindingException;
  
  /**
   * Gets {@link ResourceRequirement} object
   *
   * @param job         Job object
   * @return            ResourceRequirement object
   * @throws BindingException
   */
  ResourceRequirement getResourceRequirement(Job job) throws BindingException;
  
  /**
   * Translates {@link Job} object into common DAG format
   *
   * @param job         Job object
   * @return            Root DAG node
   * @throws BindingException
   */
  DAGNode translateToDAG(Job job) throws BindingException;

  /**
   * Validates {@link Job} object
   *
   * @param job         Job object
   * @throws BindingException
   */
  void validate(Job job) throws BindingException;
  
  /**
   * Gets protocol type
   *
   * @return ProtocolType value
   */
  ProtocolType getProtocolType();

  /**
   * Converts FileValue object into raw map format
   * @param fileValue
   * @return
   */
  Map<String, Object> translateFile(FileValue fileValue);
  
}
