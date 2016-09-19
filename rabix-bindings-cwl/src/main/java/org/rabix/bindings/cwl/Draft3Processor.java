package org.rabix.bindings.draft3;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolProcessor;
import org.rabix.bindings.draft3.bean.Draft3CommandLineTool;
import org.rabix.bindings.draft3.bean.Draft3ExpressionTool;
import org.rabix.bindings.draft3.bean.Draft3Job;
import org.rabix.bindings.draft3.bean.Draft3JobApp;
import org.rabix.bindings.draft3.bean.Draft3OutputPort;
import org.rabix.bindings.draft3.expression.Draft3ExpressionException;
import org.rabix.bindings.draft3.expression.Draft3ExpressionResolver;
import org.rabix.bindings.draft3.expression.javascript.Draft3ExpressionJavascriptResolver;
import org.rabix.bindings.draft3.helper.Draft3BindingHelper;
import org.rabix.bindings.draft3.helper.Draft3FileValueHelper;
import org.rabix.bindings.draft3.helper.Draft3JobHelper;
import org.rabix.bindings.draft3.helper.Draft3SchemaHelper;
import org.rabix.bindings.draft3.processor.Draft3PortProcessorException;
import org.rabix.bindings.draft3.processor.callback.Draft3PortProcessorHelper;
import org.rabix.bindings.draft3.service.Draft3GlobException;
import org.rabix.bindings.draft3.service.Draft3GlobService;
import org.rabix.bindings.draft3.service.Draft3MetadataService;
import org.rabix.bindings.draft3.service.impl.Draft3GlobServiceImpl;
import org.rabix.bindings.draft3.service.impl.Draft3MetadataServiceImpl;
import org.rabix.bindings.model.Job;
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
  public Job preprocess(final Job job, final File workingDir) throws BindingException {
    Draft3Job draft2Job = Draft3JobHelper.getDraft3Job(job);
    Draft3PortProcessorHelper portProcessorHelper = new Draft3PortProcessorHelper(draft2Job);
    try {
      File jobFile = new File(workingDir, JOB_FILE);
      String serializedJob = BeanSerializer.serializePartial(Draft3JobHelper.getDraft3Job(job));
      FileUtils.writeStringToFile(jobFile, serializedJob);
      
      Map<String, Object> inputs = job.getInputs();
      inputs = portProcessorHelper.setFileSize(inputs);
      inputs = portProcessorHelper.loadInputContents(inputs);
      inputs = portProcessorHelper.stageInputFiles(inputs, workingDir);
      return Job.cloneWithInputs(job, inputs);
    } catch (Draft3PortProcessorException | IOException e) {
      throw new BindingException(e);
    }
  }
  
  @Override
  public boolean isSuccessful(Job job, int statusCode) throws BindingException {
    Draft3Job draft2Job = Draft3JobHelper.getDraft3Job(job);
    List<Integer> successCodes = draft2Job.getApp().getSuccessCodes();

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
  public Job postprocess(Job job, File workingDir) throws BindingException {
    Draft3Job draft2Job = Draft3JobHelper.getDraft3Job(job);
    try {
      Map<String, Object> outputs = null;

      if (draft2Job.getApp().isExpressionTool()) {
        Draft3ExpressionTool expressionTool = (Draft3ExpressionTool) draft2Job.getApp();
        try {
          outputs = (Map<String, Object>) Draft3ExpressionJavascriptResolver.evaluate(draft2Job.getInputs(), null, (String) expressionTool.getScript(), null);
        } catch (Draft3ExpressionException e) {
          throw new BindingException("Failed to populate outputs", e);
        }
      } else {
        outputs = collectOutputs(draft2Job, workingDir, null);
      }
      return Job.cloneWithOutputs(job, outputs);
    } catch (Draft3GlobException | Draft3ExpressionException | IOException e) {
      throw new BindingException(e);
    }
  }
  
  private Map<String, Object> collectOutputs(Draft3Job job, File workingDir, HashAlgorithm hashAlgorithm) throws Draft3GlobException, Draft3ExpressionException, IOException, BindingException {
    File resultFile = new File(workingDir, RESULT_FILENAME);
    
    if (resultFile.exists()) {
      String resultStr = FileUtils.readFileToString(resultFile);
      return JSONHelper.readMap(resultStr);
    }
    
    Map<String, Object> result = new HashMap<>();
    Draft3CommandLineTool commandLineTool = (Draft3CommandLineTool) job.getApp();
    for (Draft3OutputPort outputPort : commandLineTool.getOutputs()) {
      Object singleResult = collectOutput(job, workingDir, hashAlgorithm, outputPort.getSchema(), outputPort.getOutputBinding(), outputPort);
      if (singleResult != null) {
        result.put(Draft3SchemaHelper.normalizeId(outputPort.getId()), singleResult);
      }
    }
    BeanSerializer.serializePartial(resultFile, result);
    return result;
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
      Object itemSchema = Draft3SchemaHelper.getSchemaForArrayItem(app.getSchemaDefs(), schema);
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
      result = Draft3BindingHelper.evaluateOutputEval(job, result, binding);
      logger.info("OutputEval transformed result into {}.", result);
    }
    if (result instanceof List<?>) {
      if (Draft3SchemaHelper.isFileFromSchema(schema)) {
        switch (((List<?>) result).size()) {
        case 0:
          result = null;
          break;
        case 1:
          result = ((List<?>) result).get(0);
          break;
        default:
          throw new BindingException("Invalid file format " + result);
        }
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

        List<?> secondaryFiles = getSecondaryFiles(job, hashAlgorithm, fileData, file.getAbsolutePath(), outputBinding);
        if (secondaryFiles != null) {
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
        logger.error("Failed to extract outputs", e);
        throw new Draft3GlobException("Failed to extract outputs.", e);
      }
    }
    return result;
  }

  /**
   * Gets secondary files (absolute paths)
   */
  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> getSecondaryFiles(Draft3Job job, HashAlgorithm hashAlgorithm, Map<String, Object> fileValue, String fileName, Object binding) throws Draft3ExpressionException {
    Object secondaryFilesObj = Draft3BindingHelper.getSecondaryFiles(binding);

    if (secondaryFilesObj == null) {
      return null;
    }

    List<Object> secondaryFilesList = new ArrayList<>();
    if (secondaryFilesObj instanceof List<?>) {
      secondaryFilesList.addAll((Collection<? extends Object>) secondaryFilesObj);
    }
    
    List<Map<String, Object>> secondaryFileMaps = new ArrayList<>();
    for (Object suffixObj : secondaryFilesList) {
      String suffix = Draft3ExpressionResolver.resolve(suffixObj, job, fileValue);
      String secondaryFilePath = fileName.toString();

      while (suffix.startsWith("^")) {
        int extensionIndex = secondaryFilePath.lastIndexOf(".");
        if (extensionIndex != -1) {
          secondaryFilePath = secondaryFilePath.substring(0, extensionIndex);
          suffixObj = suffix.substring(1);
        } else {
          break;
        }
      }
      secondaryFilePath += suffix.startsWith(".") ? suffixObj : "." + suffixObj;
      File secondaryFile = new File(secondaryFilePath);
      if (secondaryFile.exists()) {
        Map<String, Object> secondaryFileMap = new HashMap<>();
        Draft3FileValueHelper.setFileType(secondaryFileMap);
        Draft3FileValueHelper.setPath(secondaryFile.getAbsolutePath(), secondaryFileMap);
        Draft3FileValueHelper.setSize(secondaryFile.length(), secondaryFileMap);
        Draft3FileValueHelper.setName(secondaryFile.getName(), secondaryFileMap);
        if (hashAlgorithm != null) {
          Draft3FileValueHelper.setChecksum(secondaryFile, secondaryFileMap, hashAlgorithm);
        }
        secondaryFileMaps.add(secondaryFileMap);
      }
    }
    return secondaryFileMaps;
  }

}
