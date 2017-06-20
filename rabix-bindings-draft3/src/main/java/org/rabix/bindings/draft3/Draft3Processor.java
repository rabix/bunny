package org.rabix.bindings.draft3;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolProcessor;
import org.rabix.bindings.draft3.bean.Draft3CommandLineTool;
import org.rabix.bindings.draft3.bean.Draft3ExpressionTool;
import org.rabix.bindings.draft3.bean.Draft3Job;
import org.rabix.bindings.draft3.bean.Draft3JobApp;
import org.rabix.bindings.draft3.bean.Draft3OutputPort;
import org.rabix.bindings.draft3.bean.Draft3Runtime;
import org.rabix.bindings.draft3.expression.Draft3ExpressionException;
import org.rabix.bindings.draft3.expression.Draft3ExpressionResolver;
import org.rabix.bindings.draft3.expression.javascript.Draft3ExpressionJavascriptResolver;
import org.rabix.bindings.draft3.helper.Draft3BindingHelper;
import org.rabix.bindings.draft3.helper.Draft3FileValueHelper;
import org.rabix.bindings.draft3.helper.Draft3JobHelper;
import org.rabix.bindings.draft3.helper.Draft3RuntimeHelper;
import org.rabix.bindings.draft3.helper.Draft3SchemaHelper;
import org.rabix.bindings.draft3.processor.Draft3PortProcessor;
import org.rabix.bindings.draft3.processor.Draft3PortProcessorException;
import org.rabix.bindings.draft3.processor.callback.Draft3FilePathMapProcessorCallback;
import org.rabix.bindings.draft3.processor.callback.Draft3PortProcessorHelper;
import org.rabix.bindings.draft3.service.Draft3GlobException;
import org.rabix.bindings.draft3.service.Draft3GlobService;
import org.rabix.bindings.draft3.service.Draft3MetadataService;
import org.rabix.bindings.draft3.service.impl.Draft3GlobServiceImpl;
import org.rabix.bindings.draft3.service.impl.Draft3MetadataServiceImpl;
import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.bindings.model.Job;
import org.rabix.common.helper.ChecksumHelper;
import org.rabix.common.helper.ChecksumHelper.HashAlgorithm;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.json.BeanSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Draft3Processor implements ProtocolProcessor {

  public final static int DEFAULT_SUCCESS_CODE = 0;
  
  public final static String JOB_FILE = "job.json";
  public final static String RESULT_FILENAME = "cwl.output.json";
  
  private final static Logger logger = LoggerFactory.getLogger(Draft3Processor.class);

  private final Draft3GlobService globService;
  private final Draft3MetadataService metadataService;

  public Draft3Processor() {
    this.globService = new Draft3GlobServiceImpl();
    this.metadataService = new Draft3MetadataServiceImpl();
  }

  @Override
  public Job preprocess(final Job job, final File workingDir, FilePathMapper logFilesPathMapper) throws BindingException {
    Draft3Job draft3Job = Draft3JobHelper.getDraft3Job(job);
    Draft3Runtime runtime;
    try {
      runtime = Draft3RuntimeHelper.createRuntime(draft3Job, job.getResources());
    } catch (Draft3ExpressionException e1) {
      throw new BindingException(e1);
    }
    runtime = Draft3RuntimeHelper.setOutdir(runtime, workingDir.getAbsolutePath());
    runtime = Draft3RuntimeHelper.setTmpdir(runtime, workingDir.getAbsolutePath());
    draft3Job.setRuntime(runtime);
    Draft3PortProcessorHelper portProcessorHelper = new Draft3PortProcessorHelper(draft3Job);
    try {
      Map<String, Object> inputs = draft3Job.getInputs();
      inputs = portProcessorHelper.setFileSize(inputs);
      inputs = portProcessorHelper.loadInputContents(inputs);
      inputs = portProcessorHelper.stageInputFiles(inputs, workingDir);
      Job newJob = Job.cloneWithResources(job, Draft3RuntimeHelper.convertToResources(runtime));
      
      Map<String, Object> mappedInputs = inputs;
      if (logFilesPathMapper != null) {
        Map<String, Object> config = job.getConfig();
        Draft3PortProcessor draft3PortProcessor = new Draft3PortProcessor(draft3Job);
        mappedInputs = draft3PortProcessor.processInputs(inputs, new Draft3FilePathMapProcessorCallback(logFilesPathMapper, config));
      }
      
      File jobFile = new File(workingDir, Draft3Processor.JOB_FILE);
      String serializedJob = BeanSerializer.serializePartial(new Draft3Job(draft3Job.getId(), draft3Job.getApp(), mappedInputs, draft3Job.getOutputs()));
      try {
        FileUtils.writeStringToFile(jobFile, serializedJob);
      } catch (IOException e) {
        throw new BindingException(e);
      }
      
      @SuppressWarnings("unchecked")
      Map<String, Object> commonInputs = (Map<String, Object>) Draft3ValueTranslator.translateToCommon(inputs);
      return Job.cloneWithInputs(newJob, commonInputs);
    } catch (Draft3PortProcessorException e) {
      throw new BindingException(e);
    }
  }
  
  @Override
  public boolean isSuccessful(Job job, int statusCode) throws BindingException {
    Draft3Job draft3Job = Draft3JobHelper.getDraft3Job(job);
    List<Integer> successCodes = draft3Job.getApp().getSuccessCodes();

    if (successCodes == null) {
      successCodes = new ArrayList<>();
    }
    if (successCodes.isEmpty()) {
      successCodes.add(DEFAULT_SUCCESS_CODE);
    }
    for (Integer successCode : successCodes) {
      if (successCode.intValue() == statusCode) {
        return true;
      }
    }
    return false;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Job postprocess(Job job, File workingDir, HashAlgorithm hashAlgorithm, FilePathMapper logFilePathMapper) throws BindingException {
    Draft3Job draft3Job = Draft3JobHelper.getDraft3Job(job);
    try {
      Map<String, Object> outputs = null;

      if (draft3Job.getApp().isExpressionTool()) {
        Draft3ExpressionTool expressionTool = (Draft3ExpressionTool) draft3Job.getApp();
        try {
          outputs = (Map<String, Object>) Draft3ExpressionJavascriptResolver.evaluate(draft3Job.getInputs(), null, (String) expressionTool.getScript(), null);
        } catch (Draft3ExpressionException e) {
          throw new BindingException("Failed to populate outputs", e);
        }
      } else {
        outputs = collectOutputs(draft3Job, workingDir, hashAlgorithm, logFilePathMapper, job.getConfig());
      }
      Map<String, Object> commonOutputs = (Map<String, Object>) Draft3ValueTranslator.translateToCommon(outputs);
      return Job.cloneWithOutputs(job, commonOutputs);
    } catch (Draft3GlobException | Draft3ExpressionException | IOException e) {
      throw new BindingException(e);
    }
  }
  
  private Map<String, Object> collectOutputs(Draft3Job job, File workingDir, HashAlgorithm hashAlgorithm, FilePathMapper logFilePathMapper, Map<String, Object> config) throws Draft3GlobException, Draft3ExpressionException, IOException, BindingException {
    File resultFile = new File(workingDir, RESULT_FILENAME);
    
    if (resultFile.exists()) {
      String resultStr = FileUtils.readFileToString(resultFile);
      Map<String, Object> result = JSONHelper.readMap(resultStr);
      postprocessCreatedResults(result, hashAlgorithm);
      BeanSerializer.serializePartial(resultFile, result);
      return result;
    }
    
    Map<String, Object> result = new TreeMap<>();
    Draft3CommandLineTool commandLineTool = (Draft3CommandLineTool) job.getApp();
    for (Draft3OutputPort outputPort : commandLineTool.getOutputs()) {
      Object singleResult = collectOutput(job, workingDir, hashAlgorithm, outputPort.getSchema(), outputPort.getOutputBinding(), outputPort);
      result.put(Draft3SchemaHelper.normalizeId(outputPort.getId()), singleResult);
    }
    
    if (logFilePathMapper != null) {
      try {
        Map<String, Object> mappedResult = new Draft3PortProcessor(job).processOutputs(result, new Draft3FilePathMapProcessorCallback(logFilePathMapper, config));
        BeanSerializer.serializePartial(resultFile, mappedResult);
      } catch (Draft3PortProcessorException e) {
        throw new Draft3GlobException("Failed to map outputs", e);
      }
    } else {
      BeanSerializer.serializePartial(resultFile, result);
    }
    return result;
  }
  
  private void postprocessCreatedResults(Object value, HashAlgorithm hashAlgorithm) {
    if (value == null) {
      return;
    }
    if ((Draft3SchemaHelper.isFileFromValue(value))) {
      String path = Draft3FileValueHelper.getPath(value);
      if (StringUtils.isEmpty(Draft3FileValueHelper.getLocation(value))) {
        Draft3FileValueHelper.setLocation(path, value);
      }
      
      File file = new File(path);
      if (!file.exists()) {
        return;
      }
      Draft3FileValueHelper.setSize(file.length(), value);
      
      if(hashAlgorithm != null) {
        String checksum = ChecksumHelper.checksum(file, hashAlgorithm);
        if (checksum != null) {
          Draft3FileValueHelper.setChecksum(checksum, value);
        }
      }
      
      List<Map<String, Object>> secondaryFiles = Draft3FileValueHelper.getSecondaryFiles(value);
      if (secondaryFiles != null) {
        for (Object secondaryFile : secondaryFiles) {
          postprocessCreatedResults(secondaryFile, hashAlgorithm);
        }
      }
    } else if (value instanceof List<?>) {
      for (Object subvalue : (List<?>) value) {
        postprocessCreatedResults(subvalue, hashAlgorithm);
      }
    } else if (value instanceof Map<?, ?>) {
      for (Object subvalue : ((Map<?, ?>) value).values()) {
        postprocessCreatedResults(subvalue, hashAlgorithm);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private Object collectOutput(Draft3Job job, File workingDir, HashAlgorithm hashAlgorithm, Object schema, Object binding, Draft3OutputPort outputPort) throws Draft3GlobException, Draft3ExpressionException, BindingException {
    if (binding == null) {
      binding = Draft3SchemaHelper.getOutputBinding(schema);
    }
    logger.debug("Collecting outputs for {}.", job.getId());

    if (schema == null) {
      schema = Draft3SchemaHelper.TYPE_JOB_FILE;
    }

    Object result = null;
    if (Draft3SchemaHelper.isArrayFromSchema(schema)) {
      Draft3JobApp app = job.getApp();
      Object itemSchema = Draft3SchemaHelper.getSchemaForArrayItem(null, app.getSchemaDefs(), schema);
      if (itemSchema == null) {
        return null;
      }
      
      if (itemSchema.equals(Draft3SchemaHelper.TYPE_JOB_FILE) || Draft3SchemaHelper.isFileFromSchema(itemSchema)) {
        Object itemBinding = Draft3SchemaHelper.getOutputBinding(itemSchema);
        if (itemBinding != null) {
          binding = itemBinding;
        }
        result = globFiles(job, workingDir, hashAlgorithm, outputPort, binding);
      } else {
        return collectOutput(job, workingDir, hashAlgorithm, itemSchema, binding, outputPort);
      }
    } else if (Draft3SchemaHelper.isRecordFromSchema(schema)) {
      Map<String, Object> record = new HashMap<>();
      Object fields = Draft3SchemaHelper.getFields(schema);

      if (fields instanceof List<?>) {
        List<Object> fieldList = (List<Object>) fields;
        for (Object field : fieldList) {
          Map<String, Object> fieldMap = (Map<String, Object>) field;

          String id = (String) fieldMap.get(Draft3SchemaHelper.KEY_SCHEMA_NAME);
          Object fieldSchema = fieldMap.get(Draft3SchemaHelper.KEY_SCHEMA_TYPE);
          Object fieldBinding = Draft3SchemaHelper.getOutputBinding(fieldMap);
          if (fieldBinding != null) {
            binding = fieldBinding;
          }
          Object singleResult = collectOutput(job, workingDir, hashAlgorithm, fieldSchema, binding, outputPort);
          if (singleResult != null) {
            record.put(id, singleResult);
          }
        }
      }
      result = record;
    } else {
      result = globFiles(job, workingDir, hashAlgorithm, outputPort, binding);
    }
    Object outputEval = Draft3BindingHelper.getOutputEval(binding);
    if (outputEval != null) {
      Object self = result != null ? result : Collections.emptyList();
      result = Draft3BindingHelper.evaluateOutputEval(job, self, binding);
      logger.info("OutputEval transformed result into {}.", result);
    }
    if (Draft3SchemaHelper.isFileFromSchema(schema)) {
      if (result instanceof List<?>) {
        switch (((List<?>) result).size()) {
        case 0:
          result = null;
          break;
        case 1:
          result = ((List<?>) result).get(0);
          break;
        }
      }
    }
    if(outputPort.getFormat() != null) {
      if(result instanceof List) {
        for(Object elem: (List<Object>) result) {
          setFormat(elem, outputPort.getFormat(), job);
        }
      }
      else if( result instanceof Map) {
        setFormat(result, outputPort.getFormat(), job);
      }
    }
    return result;
  }
  
  /**
   * Extracts files from a directory based on GLOB expression
   */
  private List<Map<String, Object>> globFiles(final Draft3Job job, final File workingDir, HashAlgorithm hashAlgorithm, final Draft3OutputPort outputPort, Object outputBinding) throws Draft3GlobException {
    if (outputPort.getOutputBinding() != null) {
      outputBinding = outputPort.getOutputBinding(); // override
    }
    if (outputBinding == null) {
      logger.debug("Output binding is empty. Skip output extraction.");
      return null;
    }

    Object glob = Draft3BindingHelper.getGlob(outputBinding);
    if (glob == null) {
      logger.debug("GLOB does not exist. Skip output extraction.");
      return null;
    }

    Set<File> paths = globService.glob(job, workingDir, glob);
    if (paths == null) {
      logger.info("Glob service didn't find any files.");
      return null;
    }
    logger.debug("Glob service returned result {}", paths);

    final List<Map<String, Object>> result = new ArrayList<>();
    for (File path : paths) {
      try {
        logger.info("Processing {}.", path);
        File file = path;
        Map<String, Object> fileData = new HashMap<>();
        Draft3FileValueHelper.setFileType(fileData);
        if (hashAlgorithm != null) {
          Draft3FileValueHelper.setChecksum(file, fileData, hashAlgorithm);
        }
        Draft3FileValueHelper.setSize(file.length(), fileData);
        Draft3FileValueHelper.setName(file.getName(), fileData);
        Draft3FileValueHelper.setPath(file.getAbsolutePath(), fileData);

        List<?> secondaryFiles = getSecondaryFiles(job, hashAlgorithm, fileData, file.getAbsolutePath(), outputPort.getSecondaryFiles());
        if (secondaryFiles != null && !secondaryFiles.isEmpty()) {
          Draft3FileValueHelper.setSecondaryFiles(secondaryFiles, fileData);
        }
        Object metadata = Draft3BindingHelper.getMetadata(outputBinding);
        metadata = metadataService.evaluateMetadataExpressions(job, fileData, metadata);
        logger.info("Metadata expressions evaluated. Metadata is {}.", metadata);
        if (metadata != null) {
          Draft3FileValueHelper.setMetadata(metadata, fileData);
        }
        metadata = metadataService.processMetadata(job, fileData, outputPort, outputBinding);
        if (metadata != null) {
          logger.info("Metadata for {} resolved. Metadata is {}", outputPort.getId(), metadata);
          Draft3FileValueHelper.setMetadata(metadata, fileData);
        } else {
          logger.info("Metadata for {} output is empty.", outputPort.getId());
        }
        result.add(fileData);

        boolean loadContents = Draft3BindingHelper.loadContents(outputBinding);
        if (loadContents) {
          Draft3FileValueHelper.setContents(fileData);
        }
      } catch (Exception e) {
        throw new Draft3GlobException("Failed to extract outputs.", e);
      }
    }
    return result;
  }

  /**
   * Gets secondary files (absolute paths)
   */
  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> getSecondaryFiles(Draft3Job job, HashAlgorithm hashAlgorithm, Map<String, Object> fileValue, String fileName, Object secondaryFilesObj) throws Draft3ExpressionException {

    if (secondaryFilesObj == null) {
      return null;
    }
    
    List<Object> secondaryFilesList = new ArrayList<>();
    if (secondaryFilesObj instanceof List<?>) {
      secondaryFilesList.addAll((Collection<? extends Object>) secondaryFilesObj);
    }
    
    List<Map<String, Object>> secondaryFileMaps = new ArrayList<>();
    for (Object suffixObj : secondaryFilesList) {
      Object expr = Draft3ExpressionResolver.resolve(suffixObj, job, fileValue);
      Map<String, Object> secondaryFileMap = new HashMap<>();
      if(expr instanceof String) {
        String secondaryFilePath;
        String suffix = (String) expr;
        if((suffix).startsWith("^") || suffix.startsWith(".")) {
          secondaryFilePath = fileName.toString();
          while (suffix.startsWith("^")) {
            int extensionIndex = secondaryFilePath.lastIndexOf(".");
            if (extensionIndex != -1) {
              secondaryFilePath = secondaryFilePath.substring(0, extensionIndex);
              suffix = suffix.substring(1);
            } else {
              break;
            }
          }
          secondaryFilePath += ((String) suffix).startsWith(".") ? suffix : "." + suffix;
        } else {
          secondaryFilePath = suffix;
        }
        File secondaryFile = new File(secondaryFilePath);
        if (secondaryFile.exists()) {
          Draft3FileValueHelper.setFileType(secondaryFileMap);
          Draft3FileValueHelper.setPath(secondaryFile.getAbsolutePath(), secondaryFileMap);
          Draft3FileValueHelper.setSize(secondaryFile.length(), secondaryFileMap);
          Draft3FileValueHelper.setName(secondaryFile.getName(), secondaryFileMap);
          if (hashAlgorithm != null) {
            Draft3FileValueHelper.setChecksum(secondaryFile, secondaryFileMap, hashAlgorithm);
          }
        }
      } else if (expr instanceof Map) {
        secondaryFileMap = (Map<String, Object>) expr;
        postprocessCreatedResults(secondaryFileMap, hashAlgorithm);
      }
      if(!secondaryFileMap.isEmpty()) {
        secondaryFileMaps.add(secondaryFileMap);
      }
    }
    return secondaryFileMaps;
  }
  
  @SuppressWarnings("unchecked")
  private Object setFormat(Object result, Object format, Draft3Job job) throws Draft3ExpressionException {
    Object resolved = Draft3ExpressionResolver.resolve(format, job, null);
    ((Map<String, Object>) result).put("format", resolved);
    return result;
  }

  @Override
  public Object transformInputs(Object value, Job job, Object transform) throws BindingException {
    Object specificValue = Draft3ValueTranslator.translateToSpecific(value);
    Draft3Job draft3Job = Draft3JobHelper.getDraft3Job(job);
    Object result = null;
    try {
      result = Draft3ExpressionResolver.resolve(transform, draft3Job, specificValue);
      return Draft3ValueTranslator.translateToCommon(result);
    } catch (Draft3ExpressionException e) {
      throw new BindingException(e);
    }
  }
  
}
