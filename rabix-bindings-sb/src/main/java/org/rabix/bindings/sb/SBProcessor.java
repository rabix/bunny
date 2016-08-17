package org.rabix.bindings.sb;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolProcessor;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.sb.bean.SBCommandLineTool;
import org.rabix.bindings.sb.bean.SBExpressionTool;
import org.rabix.bindings.sb.bean.SBJob;
import org.rabix.bindings.sb.bean.SBJobApp;
import org.rabix.bindings.sb.bean.SBOutputPort;
import org.rabix.bindings.sb.expression.SBExpressionException;
import org.rabix.bindings.sb.expression.helper.SBExpressionBeanHelper;
import org.rabix.bindings.sb.helper.SBBindingHelper;
import org.rabix.bindings.sb.helper.SBFileValueHelper;
import org.rabix.bindings.sb.helper.SBJobHelper;
import org.rabix.bindings.sb.helper.SBSchemaHelper;
import org.rabix.bindings.sb.processor.SBPortProcessorException;
import org.rabix.bindings.sb.processor.callback.SBPortProcessorHelper;
import org.rabix.bindings.sb.service.SBGlobException;
import org.rabix.bindings.sb.service.SBGlobService;
import org.rabix.bindings.sb.service.SBMetadataService;
import org.rabix.bindings.sb.service.impl.SBGlobServiceImpl;
import org.rabix.bindings.sb.service.impl.SBMetadataServiceImpl;
import org.rabix.common.helper.ChecksumHelper.HashAlgorithm;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.json.BeanSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SBProcessor implements ProtocolProcessor {

  public final static int DEFAULT_SUCCESS_CODE = 0;
  
  private final static String JOB_FILE = "job.json";
  private final static String resultFilename = "cwl.output.json";
  
  private final static Logger logger = LoggerFactory.getLogger(SBProcessor.class);

  private final SBGlobService globService;
  private final SBMetadataService metadataService;

  public SBProcessor() {
    this.globService = new SBGlobServiceImpl();
    this.metadataService = new SBMetadataServiceImpl();
  }

  @Override
  public Job preprocess(final Job job, final File workingDir) throws BindingException {
    SBJob sbJob = SBJobHelper.getSBJob(job);
    SBPortProcessorHelper portProcessorHelper = new SBPortProcessorHelper(sbJob);
    try {
      File jobFile = new File(workingDir, JOB_FILE);
      String serializedJob = BeanSerializer.serializePartial(SBJobHelper.getSBJob(job));
      FileUtils.writeStringToFile(jobFile, serializedJob);
      
      Map<String, Object> inputs = job.getInputs();
      inputs = portProcessorHelper.setFileSize(inputs);
      inputs = portProcessorHelper.loadInputContents(inputs);
      inputs = portProcessorHelper.stageInputFiles(inputs, workingDir);
      return Job.cloneWithInputs(job, inputs);
    } catch (SBPortProcessorException | IOException e) {
      throw new BindingException(e);
    }
  }
  
  @Override
  public boolean isSuccessful(Job job, int statusCode) throws BindingException {
    SBJob sbJob = SBJobHelper.getSBJob(job);
    List<Integer> successCodes = sbJob.getApp().getSuccessCodes();

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
  public Job postprocess(Job job, File workingDir) throws BindingException {
    SBJob sbJob = SBJobHelper.getSBJob(job);
    try {
      Map<String, Object> outputs = null;

      if (sbJob.getApp().isExpressionTool()) {
        SBExpressionTool expressionTool = (SBExpressionTool) sbJob.getApp();
        try {
          outputs = SBExpressionBeanHelper.evaluate(sbJob, expressionTool.getScript());
        } catch (SBExpressionException e) {
          throw new BindingException("Failed to populate outputs", e);
        }
      } else {
        outputs = collectOutputs(sbJob, workingDir, null);
      }
      outputs = new SBPortProcessorHelper(sbJob).fixOutputMetadata(sbJob.getInputs(), outputs);
      writeResult(workingDir, outputs);
      return Job.cloneWithOutputs(job, outputs);
    } catch (SBGlobException | SBExpressionException | IOException | SBPortProcessorException e) {
      throw new BindingException(e);
    }
  }
  
  private Map<String, Object> collectOutputs(SBJob job, File workingDir, HashAlgorithm hashAlgorithm) throws SBGlobException, SBExpressionException, IOException, BindingException {
    File resultFile = new File(workingDir, resultFilename);
    
    if (resultFile.exists()) {
      String resultStr = FileUtils.readFileToString(resultFile);
      return JSONHelper.readMap(resultStr);
    }
    
    Map<String, Object> result = new HashMap<>();
    SBCommandLineTool commandLineTool = (SBCommandLineTool) job.getApp();
    for (SBOutputPort outputPort : commandLineTool.getOutputs()) {
      Object singleResult = collectOutput(job, workingDir, hashAlgorithm, outputPort.getSchema(), outputPort.getOutputBinding(), outputPort);
      if (singleResult != null) {
        result.put(SBSchemaHelper.normalizeId(outputPort.getId()), singleResult);
      }
    }
    writeResult(workingDir, result);
    return result;
  }
  
  public void writeResult(File workingDir, Map<String, Object> result) {
    BeanSerializer.serializePartial(new File(workingDir, resultFilename), result);
  }

  @SuppressWarnings("unchecked")
  private Object collectOutput(SBJob job, File workingDir, HashAlgorithm hashAlgorithm, Object schema, Object binding, SBOutputPort outputPort) throws SBGlobException, SBExpressionException, BindingException {
    if (binding == null) {
      binding = SBSchemaHelper.getOutputBinding(schema);
    }
    logger.debug("Collecting outputs for {}.", job.getId());

    if (schema == null) {
      schema = SBSchemaHelper.TYPE_JOB_FILE;
    }

    Object result = null;
    if (SBSchemaHelper.isArrayFromSchema(schema)) {
      SBJobApp app = job.getApp();
      Object itemSchema = SBSchemaHelper.getSchemaForArrayItem(app.getSchemaDefs(), schema);
      if (itemSchema == null) {
        return null;
      }

      if (itemSchema.equals(SBSchemaHelper.TYPE_JOB_FILE) || SBSchemaHelper.isFileFromSchema(itemSchema)) {
        Object itemBinding = SBSchemaHelper.getOutputBinding(itemSchema);
        if (itemBinding != null) {
          binding = itemBinding;
        }
        result = globFiles(job, workingDir, hashAlgorithm, outputPort, binding);
      } else {
        return collectOutput(job, workingDir, hashAlgorithm, itemSchema, binding, outputPort);
      }
    } else if (SBSchemaHelper.isRecordFromSchema(schema)) {
      Map<String, Object> record = new HashMap<>();
      Object fields = SBSchemaHelper.getFields(schema);

      if (fields instanceof List<?>) {
        List<Object> fieldList = (List<Object>) fields;
        for (Object field : fieldList) {
          Map<String, Object> fieldMap = (Map<String, Object>) field;

          String id = (String) fieldMap.get(SBSchemaHelper.KEY_SCHEMA_NAME);
          Object fieldSchema = fieldMap.get(SBSchemaHelper.KEY_SCHEMA_TYPE);
          Object fieldBinding = SBSchemaHelper.getOutputBinding(fieldMap);
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
    Object outputEval = SBBindingHelper.getOutputEval(binding);
    if (outputEval != null) {
      logger.debug("Evaluating OutputEval for type {} and result {}", schema, result);
      result = SBBindingHelper.evaluateOutputEval(job, result, binding);
      logger.info("OutputEval transformed result into {}.", result);
    }
    if (result instanceof List<?>) {
      if (SBSchemaHelper.isFileFromSchema(schema)) {
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
  private List<Map<String, Object>> globFiles(final SBJob job, final File workingDir, HashAlgorithm hashAlgorithm, final SBOutputPort outputPort, Object outputBinding) throws SBGlobException {
    if (outputPort.getOutputBinding() != null) {
      outputBinding = outputPort.getOutputBinding(); // override
    }
    if (outputBinding == null) {
      logger.debug("Output binding is empty. Skip output extraction.");
      return null;
    }

    Object glob = SBBindingHelper.getGlob(outputBinding);
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
        SBFileValueHelper.setFileType(fileData);
        if (hashAlgorithm != null) {
          SBFileValueHelper.setChecksum(file, fileData, hashAlgorithm);
        }
        SBFileValueHelper.setSize(file.length(), fileData);
        SBFileValueHelper.setName(file.getName(), fileData);
        SBFileValueHelper.setPath(file.getAbsolutePath(), fileData);

        List<?> secondaryFiles = getSecondaryFiles(job, hashAlgorithm, fileData, file.getAbsolutePath(), outputBinding);
        if (secondaryFiles != null) {
          SBFileValueHelper.setSecondaryFiles(secondaryFiles, fileData);
        }
        Object metadata = SBBindingHelper.getMetadata(outputBinding);
        metadata = metadataService.evaluateMetadataExpressions(job, fileData, metadata);
        logger.info("Metadata expressions evaluated. Metadata is {}.", metadata);
        if (metadata != null) {
          SBFileValueHelper.setMetadata(metadata, fileData);
        }
        metadata = metadataService.processMetadata(job, fileData, outputPort, outputBinding);
        if (metadata != null) {
          logger.info("Metadata for {} resolved. Metadata is {}", outputPort.getId(), metadata);
          SBFileValueHelper.setMetadata(metadata, fileData);
        } else {
          logger.info("Metadata for {} output is empty.", outputPort.getId());
        }
        result.add(fileData);

        boolean loadContents = SBBindingHelper.loadContents(outputBinding);
        if (loadContents) {
          SBFileValueHelper.setContents(fileData);
        }
      } catch (Exception e) {
        logger.error("Failed to extract outputs", e);
        throw new SBGlobException("Failed to extract outputs.", e);
      }
    }
    return result;
  }

  /**
   * Gets secondary files (absolute paths)
   */
  private List<Map<String, Object>> getSecondaryFiles(SBJob job, HashAlgorithm hashAlgorithm, Map<String, Object> fileValue, String fileName, Object binding) throws SBExpressionException {
    List<String> secondaryFileSufixes = SBBindingHelper.getSecondaryFiles(binding);

    if (secondaryFileSufixes == null) {
      return null;
    }

    List<Map<String, Object>> secondaryFileMaps = new ArrayList<>();
    for (String suffix : secondaryFileSufixes) {
      String secondaryFilePath = fileName.toString();

      if (SBExpressionBeanHelper.isExpression(suffix)) {
        secondaryFilePath = SBExpressionBeanHelper.evaluate(job, fileValue, suffix);
      } else {
        while (suffix.startsWith("^")) {
          int extensionIndex = secondaryFilePath.lastIndexOf(".");
          if (extensionIndex != -1) {
            secondaryFilePath = secondaryFilePath.substring(0, extensionIndex);
            suffix = suffix.substring(1);
          } else {
            break;
          }
        }
        secondaryFilePath += suffix.startsWith(".") ? suffix : "." + suffix;
      }
      File secondaryFile = new File(secondaryFilePath);
      if (secondaryFile.exists()) {
        Map<String, Object> secondaryFileMap = new HashMap<>();
        SBFileValueHelper.setFileType(secondaryFileMap);
        SBFileValueHelper.setPath(secondaryFile.getAbsolutePath(), secondaryFileMap);
        SBFileValueHelper.setSize(secondaryFile.length(), secondaryFileMap);
        SBFileValueHelper.setName(secondaryFile.getName(), secondaryFileMap);
        if (hashAlgorithm != null) {
          SBFileValueHelper.setChecksum(secondaryFile, secondaryFileMap, hashAlgorithm);
        }
        secondaryFileMaps.add(secondaryFileMap);
      }
    }
    return secondaryFileMaps;
  }

}