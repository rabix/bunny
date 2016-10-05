package org.rabix.bindings.cwl;

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
import org.rabix.bindings.cwl.bean.CWLCommandLineTool;
import org.rabix.bindings.cwl.bean.CWLExpressionTool;
import org.rabix.bindings.cwl.bean.CWLJob;
import org.rabix.bindings.cwl.bean.CWLJobApp;
import org.rabix.bindings.cwl.bean.CWLOutputPort;
import org.rabix.bindings.cwl.expression.CWLExpressionException;
import org.rabix.bindings.cwl.expression.CWLExpressionResolver;
import org.rabix.bindings.cwl.expression.javascript.CWLExpressionJavascriptResolver;
import org.rabix.bindings.cwl.helper.CWLBindingHelper;
import org.rabix.bindings.cwl.helper.CWLDirectoryValueHelper;
import org.rabix.bindings.cwl.helper.CWLFileValueHelper;
import org.rabix.bindings.cwl.helper.CWLJobHelper;
import org.rabix.bindings.cwl.helper.CWLSchemaHelper;
import org.rabix.bindings.cwl.processor.CWLPortProcessorException;
import org.rabix.bindings.cwl.processor.callback.CWLPortProcessorHelper;
import org.rabix.bindings.cwl.service.CWLGlobException;
import org.rabix.bindings.cwl.service.CWLGlobService;
import org.rabix.bindings.cwl.service.CWLMetadataService;
import org.rabix.bindings.cwl.service.impl.CWLGlobServiceImpl;
import org.rabix.bindings.cwl.service.impl.CWLMetadataServiceImpl;
import org.rabix.bindings.model.Job;
import org.rabix.common.helper.ChecksumHelper.HashAlgorithm;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.json.BeanSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CWLProcessor implements ProtocolProcessor {

public final static int DEFAULT_SUCCESS_CODE = 0;
  
  public final static String JOB_FILE = "job.json";
  public final static String RESULT_FILENAME = "cwl.output.json";
  
  private final static Logger logger = LoggerFactory.getLogger(CWLProcessor.class);

  private final CWLGlobService globService;
  private final CWLMetadataService metadataService;

  public CWLProcessor() {
    this.globService = new CWLGlobServiceImpl();
    this.metadataService = new CWLMetadataServiceImpl();
  }

  @Override
  public Job preprocess(final Job job, final File workingDir) throws BindingException {
    CWLJob cwlJob = CWLJobHelper.getCWLJob(job);

    CWLPortProcessorHelper portProcessorHelper = new CWLPortProcessorHelper(cwlJob);
    try {
      File jobFile = new File(workingDir, JOB_FILE);
      String serializedJob = BeanSerializer.serializePartial(CWLJobHelper.getCWLJob(job));
      FileUtils.writeStringToFile(jobFile, serializedJob);
      
      Map<String, Object> inputs = job.getInputs();
      inputs = portProcessorHelper.setFileSize(inputs);
      inputs = portProcessorHelper.loadInputContents(inputs);
      inputs = portProcessorHelper.stageInputFiles(inputs, workingDir);
      return Job.cloneWithInputs(job, inputs);
    } catch (CWLPortProcessorException | IOException e) {
      throw new BindingException(e);
    }
  }
  
  @Override
  public boolean isSuccessful(Job job, int statusCode) throws BindingException {
    CWLJob cwlJob = CWLJobHelper.getCWLJob(job);
    List<Integer> successCodes = cwlJob.getApp().getSuccessCodes();

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
    CWLJob cwlJob = CWLJobHelper.getCWLJob(job);
    try {
      Map<String, Object> outputs = null;

      if (cwlJob.getApp().isExpressionTool()) {
        CWLExpressionTool expressionTool = (CWLExpressionTool) cwlJob.getApp();
        try {
          outputs = (Map<String, Object>) CWLExpressionJavascriptResolver.evaluate(cwlJob.getInputs(), null, (String) expressionTool.getScript(), null);
        } catch (CWLExpressionException e) {
          throw new BindingException("Failed to populate outputs", e);
        }
      } else {
        outputs = collectOutputs(cwlJob, workingDir, null);
      }
      return Job.cloneWithOutputs(job, outputs);
    } catch (CWLGlobException | CWLExpressionException | IOException e) {
      throw new BindingException(e);
    }
  }
  
  private Map<String, Object> collectOutputs(CWLJob job, File workingDir, HashAlgorithm hashAlgorithm) throws CWLGlobException, CWLExpressionException, IOException, BindingException {
    File resultFile = new File(workingDir, RESULT_FILENAME);
    
    if (resultFile.exists()) {
      String resultStr = FileUtils.readFileToString(resultFile);
      return JSONHelper.readMap(resultStr);
    }
    
    Map<String, Object> result = new HashMap<>();
    CWLCommandLineTool commandLineTool = (CWLCommandLineTool) job.getApp();
    for (CWLOutputPort outputPort : commandLineTool.getOutputs()) {
      Object singleResult = collectOutput(job, workingDir, hashAlgorithm, outputPort.getSchema(), outputPort.getOutputBinding(), outputPort);
      result.put(CWLSchemaHelper.normalizeId(outputPort.getId()), singleResult);
    }
    BeanSerializer.serializePartial(resultFile, result);
    return result;
  }

  @SuppressWarnings("unchecked")
  private Object collectOutput(CWLJob job, File workingDir, HashAlgorithm hashAlgorithm, Object schema, Object binding, CWLOutputPort outputPort) throws CWLGlobException, CWLExpressionException, BindingException {
    if (binding == null) {
      binding = CWLSchemaHelper.getOutputBinding(schema);
    }
    logger.debug("Collecting outputs for {}.", job.getId());

    if (schema == null) {
      schema = CWLSchemaHelper.TYPE_JOB_FILE;
    }

    Object result = null;
    if (CWLSchemaHelper.isArrayFromSchema(schema)) {
      CWLJobApp app = job.getApp();
      Object itemSchema = CWLSchemaHelper.getSchemaForArrayItem(null, app.getSchemaDefs(), schema);
      if (itemSchema == null) {
        return null;
      }

      if (itemSchema.equals(CWLSchemaHelper.TYPE_JOB_FILE) || CWLSchemaHelper.isFileFromSchema(itemSchema)) {
        Object itemBinding = CWLSchemaHelper.getOutputBinding(itemSchema);
        if (itemBinding != null) {
          binding = itemBinding;
        }
        result = globFiles(job, workingDir, hashAlgorithm, outputPort, binding);
      } else {
        return collectOutput(job, workingDir, hashAlgorithm, itemSchema, binding, outputPort);
      }
    } else if (CWLSchemaHelper.isRecordFromSchema(schema)) {
      Map<String, Object> record = new HashMap<>();
      Object fields = CWLSchemaHelper.getFields(schema);

      if (fields instanceof List<?>) {
        List<Object> fieldList = (List<Object>) fields;
        for (Object field : fieldList) {
          Map<String, Object> fieldMap = (Map<String, Object>) field;

          String id = (String) fieldMap.get(CWLSchemaHelper.KEY_SCHEMA_NAME);
          Object fieldSchema = fieldMap.get(CWLSchemaHelper.KEY_SCHEMA_TYPE);
          Object fieldBinding = CWLSchemaHelper.getOutputBinding(fieldMap);
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
    Object outputEval = CWLBindingHelper.getOutputEval(binding);
    if (outputEval != null) {
      result = CWLBindingHelper.evaluateOutputEval(job, result, binding);
      logger.info("OutputEval transformed result into {}.", result);
    }
    if (CWLSchemaHelper.isFileFromSchema(schema)) {
	  if (result instanceof List<?>) {
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
  
  @SuppressWarnings("unchecked")
  private Object setFormat(Object result, Object format, CWLJob job) throws CWLExpressionException {
    Object resolved = CWLExpressionResolver.resolve(format, job, null);
    ((Map<String, Object>) result).put("format", resolved);
    return result;
  }
  
  

  /**
   * Extracts files from a directory based on GLOB expression
   */
  private List<Map<String, Object>> globFiles(final CWLJob job, final File workingDir, HashAlgorithm hashAlgorithm, final CWLOutputPort outputPort, Object outputBinding) throws CWLGlobException {
    if (outputPort.getOutputBinding() != null) {
      outputBinding = outputPort.getOutputBinding(); // override
    }
    if (outputBinding == null) {
      logger.debug("Output binding is empty. Skip output extraction.");
      return null;
    }

    Object glob = CWLBindingHelper.getGlob(outputBinding);
    if (glob == null) {
      logger.debug("GLOB does not exist. Skip output extraction.");
      return null;
    }

    Set<File> files = globService.glob(job, workingDir, glob);
    if (files == null) {
      logger.info("Glob service didn't find any files.");
      return null;
    }
    logger.debug("Glob service returned result {}", files);

    final List<Map<String, Object>> result = new ArrayList<>();
    for (File file : files) {
      try {
        result.add(formFileValue(file, job, outputBinding, outputPort, hashAlgorithm));
      } catch (Exception e) {
        logger.error("Failed to extract outputs", e);
        throw new CWLGlobException("Failed to extract outputs.", e);
      }
    }
    return result;
  }
  
  public Map<String, Object> formFileValue(File file, CWLJob job, Object outputBinding, CWLOutputPort outputPort, HashAlgorithm hashAlgorithm) throws CWLExpressionException, IOException {
    if (file.isDirectory()) {
      logger.info("Processing directory {}.", file);
      
      Map<String, Object> directory = new HashMap<>();
      CWLDirectoryValueHelper.setDirectoryType(directory);
      CWLDirectoryValueHelper.setSize(file.length(), directory);
      CWLDirectoryValueHelper.setName(file.getName(), directory);
      CWLDirectoryValueHelper.setPath(file.getAbsolutePath(), directory);
      
      File[] list = file.listFiles();
      
      List<Object> listing = new ArrayList<>();
      for (File subfile : list) {
        listing.add(formFileValue(subfile, job, outputBinding, outputPort, hashAlgorithm));
      }
      CWLDirectoryValueHelper.setListing(listing, directory);
      return directory;
    }

    Map<String, Object> fileData = new HashMap<>();
    CWLFileValueHelper.setFileType(fileData);
    if (hashAlgorithm != null) {
      CWLFileValueHelper.setChecksum(file, fileData, hashAlgorithm);
    }
    CWLFileValueHelper.setSize(file.length(), fileData);
    CWLFileValueHelper.setName(file.getName(), fileData);
    CWLFileValueHelper.setPath(file.getAbsolutePath(), fileData);

    List<?> secondaryFiles = getSecondaryFiles(job, hashAlgorithm, fileData, file.getAbsolutePath(), outputBinding);
    if (secondaryFiles != null) {
      CWLFileValueHelper.setSecondaryFiles(secondaryFiles, fileData);
    }
    Object metadata = CWLBindingHelper.getMetadata(outputBinding);
    metadata = metadataService.evaluateMetadataExpressions(job, fileData, metadata);
    logger.info("Metadata expressions evaluated. Metadata is {}.", metadata);
    if (metadata != null) {
      CWLFileValueHelper.setMetadata(metadata, fileData);
    }
    metadata = metadataService.processMetadata(job, fileData, outputPort, outputBinding);
    if (metadata != null) {
      logger.info("Metadata for {} resolved. Metadata is {}", outputPort.getId(), metadata);
      CWLFileValueHelper.setMetadata(metadata, fileData);
    } else {
      logger.info("Metadata for {} output is empty.", outputPort.getId());
    }
    boolean loadContents = CWLBindingHelper.loadContents(outputBinding);
    if (loadContents) {
      CWLFileValueHelper.setContents(fileData);
    }
    return fileData;
  }

  /**
   * Gets secondary files (absolute paths)
   */
  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> getSecondaryFiles(CWLJob job, HashAlgorithm hashAlgorithm, Map<String, Object> fileValue, String fileName, Object secondaryFilesObj) throws CWLExpressionException {
    if (secondaryFilesObj == null) {
      return null;
    }
    
    List<Object> secondaryFilesList = new ArrayList<>();
    if (secondaryFilesObj instanceof List<?>) {
      secondaryFilesList.addAll((Collection<? extends Object>) secondaryFilesObj);
    }
    
    List<Map<String, Object>> secondaryFileMaps = new ArrayList<>();
    for (Object suffixObj : secondaryFilesList) {
      Object expr = CWLExpressionResolver.resolve(suffixObj, job, fileValue);
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
              suffixObj = suffix.substring(1);
            } else {
              break;
            }
          }
          secondaryFilePath += ((String) suffixObj).startsWith(".") ? suffixObj : "." + suffixObj;
        }
        else {
          secondaryFilePath = suffix;
        }
        File secondaryFile = new File(secondaryFilePath);
        if (secondaryFile.exists()) {
          CWLFileValueHelper.setFileType(secondaryFileMap);
          CWLFileValueHelper.setPath(secondaryFile.getAbsolutePath(), secondaryFileMap);
          CWLFileValueHelper.setSize(secondaryFile.length(), secondaryFileMap);
          CWLFileValueHelper.setName(secondaryFile.getName(), secondaryFileMap);
          if (hashAlgorithm != null) {
            CWLFileValueHelper.setChecksum(secondaryFile, secondaryFileMap, hashAlgorithm);
          }
        }
      } else if (expr instanceof Map) {
        secondaryFileMap = (Map<String, Object>) expr;
      }
      if(!secondaryFileMap.isEmpty()) {
        secondaryFileMaps.add(secondaryFileMap);
      }
    }
    return secondaryFileMaps;
  }

  @Override
  public Object transformInputs(Object value, Job job, Object transform) throws BindingException {
    CWLJob cwlJob = CWLJobHelper.getCWLJob(job);
    Object result = null;
    try {
      result = CWLExpressionResolver.resolve(transform, cwlJob, value);
    } catch (CWLExpressionException e) {
      throw new BindingException(e);
    }
    return result;
  }

}
