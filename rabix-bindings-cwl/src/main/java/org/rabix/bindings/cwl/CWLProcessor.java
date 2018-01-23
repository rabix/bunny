package org.rabix.bindings.cwl;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolProcessor;
import org.rabix.bindings.cwl.bean.CWLCommandLineTool;
import org.rabix.bindings.cwl.bean.CWLExpressionTool;
import org.rabix.bindings.cwl.bean.CWLJob;
import org.rabix.bindings.cwl.bean.CWLJobApp;
import org.rabix.bindings.cwl.bean.CWLOutputPort;
import org.rabix.bindings.cwl.bean.CWLRuntime;
import org.rabix.bindings.cwl.expression.CWLExpressionException;
import org.rabix.bindings.cwl.expression.CWLExpressionResolver;
import org.rabix.bindings.cwl.expression.javascript.CWLExpressionJavascriptResolver;
import org.rabix.bindings.cwl.helper.CWLBindingHelper;
import org.rabix.bindings.cwl.helper.CWLDirectoryValueHelper;
import org.rabix.bindings.cwl.helper.CWLFileValueHelper;
import org.rabix.bindings.cwl.helper.CWLJobHelper;
import org.rabix.bindings.cwl.helper.CWLRuntimeHelper;
import org.rabix.bindings.cwl.helper.CWLSchemaHelper;
import org.rabix.bindings.cwl.processor.CWLPortProcessor;
import org.rabix.bindings.cwl.processor.CWLPortProcessorException;
import org.rabix.bindings.cwl.processor.callback.CWLFilePathMapProcessorCallback;
import org.rabix.bindings.cwl.processor.callback.CWLPortProcessorHelper;
import org.rabix.bindings.cwl.resolver.CWLDocumentResolver;
import org.rabix.bindings.cwl.service.CWLGlobException;
import org.rabix.bindings.cwl.service.CWLGlobService;
import org.rabix.bindings.cwl.service.CWLMetadataService;
import org.rabix.bindings.cwl.service.impl.CWLGlobServiceImpl;
import org.rabix.bindings.cwl.service.impl.CWLMetadataServiceImpl;
import org.rabix.bindings.mapper.FileMappingException;
import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.bindings.model.Job;
import org.rabix.common.helper.ChecksumHelper;
import org.rabix.common.helper.ChecksumHelper.HashAlgorithm;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.json.BeanSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CWLProcessor implements ProtocolProcessor {

  public final static int DEFAULT_SUCCESS_CODE = 0;

  public final static String JOB_FILE = "job.json";
  public final static String RESULT_FILENAME = "cwl.output.json";

  public final static String RESERVED_EXECUTOR_CMD_LOG_FILE_NAME = "cmd.log";
  public final static String RESERVED_EXECUTOR_ERROR_LOG_FILE_NAME = "job.err.log";

  private final static Logger logger = LoggerFactory.getLogger(CWLProcessor.class);

  private final CWLGlobService globService;
  private final CWLMetadataService metadataService;

  public CWLProcessor() {
    this.globService = new CWLGlobServiceImpl();
    this.metadataService = new CWLMetadataServiceImpl();
  }

  @Override
  public Job preprocess(final Job job, final File workingDir, FilePathMapper filesPathMapper) throws BindingException {
    return this.preprocess(job, workingDir.toPath(), filesPathMapper);
  }

  @Override
  public Job preprocess(final Job job, final Path workingDir, FilePathMapper filesPathMapper) throws BindingException {
    CWLJob cwlJob = CWLJobHelper.getCWLJob(job);
    CWLRuntime runtime = cwlJob.getRuntime();
    String path = workingDir.toAbsolutePath().toString();
    try {
      path = filesPathMapper == null ? path : filesPathMapper.map(path, Collections.emptyMap());
    } catch (FileMappingException e1) {
      logger.error(e1.getMessage());
    }
    runtime = CWLRuntimeHelper.setOutdir(runtime, path);
    runtime = CWLRuntimeHelper.setTmpdir(runtime, path);
    cwlJob.setRuntime(runtime);

    CWLPortProcessorHelper portProcessorHelper = new CWLPortProcessorHelper(cwlJob);
    try {
      Map<String, Object> inputs = cwlJob.getInputs();
      inputs = portProcessorHelper.createFileLiteralFiles(inputs, workingDir);
      inputs = portProcessorHelper.setFileProperties(inputs);
      inputs = portProcessorHelper.loadInputContents(inputs);
      inputs = portProcessorHelper.stageInputFiles(inputs, workingDir);
      inputs = portProcessorHelper.setInputSecondaryFiles(inputs, workingDir, null);
      Job newJob = Job.cloneWithResources(job, CWLRuntimeHelper.convertToResources(runtime));

      @SuppressWarnings("unchecked")
      Map<String, Object> commonInputs = (Map<String, Object>) CWLValueTranslator.translateToCommon(inputs);
      return Job.cloneWithInputs(newJob, commonInputs);
    } catch (CWLPortProcessorException e) {
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

  @SuppressWarnings("unchecked")
  public Job postprocess(Job job, Path workingDir, HashAlgorithm hashAlgorithm, FilePathMapper logFilePathMapper) throws BindingException {
    CWLJob cwlJob = CWLJobHelper.getCWLJob(job);
    try {
      Map<String, Object> outputs = null;

      if (cwlJob.getApp().isExpressionTool()) {
        CWLExpressionTool expressionTool = (CWLExpressionTool) cwlJob.getApp();
        try {
          outputs = (Map<String, Object>) CWLExpressionJavascriptResolver.evaluate(cwlJob.getInputs(), null, (String) expressionTool.getScript(),
              cwlJob.getRuntime(), null);
          postprocessCreatedResults(outputs, hashAlgorithm, workingDir);
        } catch (CWLExpressionException e) {
          throw new BindingException("Failed to populate outputs", e);
        }
      } else {
        outputs = collectOutputs(cwlJob, workingDir, hashAlgorithm, logFilePathMapper, job.getConfig());
      }
      return Job.cloneWithOutputs(job, (Map<String, Object>) CWLValueTranslator.translateToCommon(outputs));
    } catch (CWLGlobException | CWLExpressionException | IOException | URISyntaxException e) {
      throw new BindingException(e);
    }
  }

  @Override
  public Job postprocess(Job job, File workingDir, HashAlgorithm hashAlgorithm, FilePathMapper logFilePathMapper) throws BindingException {
    return this.postprocess(job, workingDir.toPath(), hashAlgorithm, logFilePathMapper);
  }

  private Map<String, Object> collectOutputs(CWLJob job, Path workingDir, HashAlgorithm hashAlgorithm, FilePathMapper logFilePathMapper,
      Map<String, Object> config) throws CWLGlobException, CWLExpressionException, IOException, BindingException, URISyntaxException {
    Path resultFile = workingDir.resolve(RESULT_FILENAME);

    if (Files.exists(resultFile)) {
      String resultStr = new String(Files.readAllBytes(resultFile));
      Map<String, Object> result = JSONHelper.readMap(resultStr);
      postprocessCreatedResults(result, hashAlgorithm, workingDir);
      BeanSerializer.serializePartial(resultFile, result);
      return result;
    }

    Map<String, Object> result = new HashMap<>();
    CWLCommandLineTool commandLineTool = (CWLCommandLineTool) job.getApp();
    for (CWLOutputPort outputPort : commandLineTool.getOutputs()) {
      Object singleResult = collectOutput(job, workingDir, hashAlgorithm, outputPort.getSchema(), outputPort.getOutputBinding(), outputPort);
      result.put(CWLSchemaHelper.normalizeId(outputPort.getId()), singleResult);
    }

    if (logFilePathMapper != null) {
      try {
        Map<String, Object> mappedResult = new CWLPortProcessor(job).processOutputs(result, new CWLFilePathMapProcessorCallback(logFilePathMapper, config));
        BeanSerializer.serializePartial(resultFile, mappedResult);
      } catch (CWLPortProcessorException e) {
        throw new CWLGlobException("Failed to map outputs", e);
      }
    } else {
      BeanSerializer.serializePartial(resultFile, result);
    }
    return result;
  }

  public static void postprocessCreatedResults(Object value, HashAlgorithm hashAlgorithm, File workingDir) throws IOException, URISyntaxException {
    postprocessCreatedResults(value, hashAlgorithm, workingDir.toPath());
  }

  public static void postprocessCreatedResults(Object value, HashAlgorithm hashAlgorithm, Path workingDir) throws IOException, URISyntaxException {
    if (value == null) {
      return;
    }
    if ((CWLSchemaHelper.isFileFromValue(value)) || CWLSchemaHelper.isDirectoryFromValue(value)) {
      // TODO discuss File literal processing
      if (CWLFileValueHelper.isFileLiteral(value)) {
        String contents = CWLFileValueHelper.getContents(value);
        CWLFileValueHelper.setSize(new Long(contents.length()), value);

        Path file = workingDir.resolve(CWLFileValueHelper.getName(value));
        Files.createDirectories(file.getParent());
        Files.write(file, contents.getBytes());
        String checksum = ChecksumHelper.checksum(file, hashAlgorithm);
        CWLFileValueHelper.setChecksum(checksum, value);
        CWLFileValueHelper.setLocation(file.toUri().toString(), value);
        CWLFileValueHelper.setPath(file.toString(), value);
        return;
      }

      // TODO discuss Directory literal processing
      if (CWLDirectoryValueHelper.isDirectoryLiteral(value)) {
        Path directory = workingDir.resolve(CWLDirectoryValueHelper.getName(value));
        Files.createDirectories(directory);
        CWLDirectoryValueHelper.setLocation(directory.toUri().toString(), value);
        CWLFileValueHelper.setDirType(value);

        List<Object> listing = CWLDirectoryValueHelper.getListing(value);
        if (listing != null) {
          for (Object listingObj : listing) {
            Path destinationFile = directory.resolve(CWLFileValueHelper.getName(listingObj));
            Path source = Paths.get(CWLFileValueHelper.getPath(listingObj));
            Files.copy(source, destinationFile);
            if (CWLSchemaHelper.isFileFromValue(listingObj)) {
              String checksum = ChecksumHelper.checksum(destinationFile, hashAlgorithm);
              CWLFileValueHelper.setChecksum(checksum, listingObj);
              CWLFileValueHelper.setLocation(destinationFile.toUri().toString(), listingObj);
            } else {
            }
          }
        }
        CWLFileValueHelper.setPath(directory.toString(), value);

        return;
      }
      
      CWLFileValueHelper.buildMissingInfo(value, hashAlgorithm, workingDir);

      List<Map<String, Object>> secondaryFiles = CWLFileValueHelper.getSecondaryFiles(value);
      if (secondaryFiles != null) {
        for (Object secondaryFile : secondaryFiles) {
          postprocessCreatedResults(secondaryFile, hashAlgorithm, workingDir);
        }
      }
    } else if (value instanceof List<?>) {
      for (Object subvalue : (List<?>) value) {
        postprocessCreatedResults(subvalue, hashAlgorithm, workingDir);
      }
    } else if (value instanceof Map<?, ?>) {
      for (Object subvalue : ((Map<?, ?>) value).values()) {
        postprocessCreatedResults(subvalue, hashAlgorithm, workingDir);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private Object collectOutput(CWLJob job, Path workingDir, HashAlgorithm hashAlgorithm, Object schema, Object binding, CWLOutputPort outputPort)
      throws CWLGlobException, CWLExpressionException, BindingException {
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

      if (binding != null || itemSchema.equals(CWLSchemaHelper.TYPE_JOB_FILE) || CWLSchemaHelper.isFileFromSchema(itemSchema)) {
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
      Object self = result != null ? result : Collections.emptyList();
      result = CWLBindingHelper.evaluateOutputEval(job, self, binding);
      logger.debug("OutputEval transformed result into {}.", result);
    }
    if (CWLSchemaHelper.isFileFromSchema(schema) || CWLSchemaHelper.isDirectoryFromSchema(schema)) {
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
    if (outputPort.getFormat() != null) {
      if (result instanceof List) {
        for (Object elem : (List<Object>) result) {
          setFormat(elem, outputPort.getFormat(), job);
        }
      } else if (result instanceof Map) {
        setFormat(result, outputPort.getFormat(), job);
      }
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  private Object setFormat(Object result, Object format, CWLJob job) throws CWLExpressionException {
    String resolved = CWLExpressionResolver.resolve(format, job, null);
    Object namespaces = job.getApp().getRaw().get(CWLDocumentResolver.NAMESPACES_KEY);
    if (namespaces instanceof Map) {
      for (Entry<String, String> entry : ((Map<String, String>) namespaces).entrySet()) {
        resolved = resolved.replace(entry.getKey() + ":", entry.getValue());
      }
    }
    ((Map<String, Object>) result).put("format", resolved);
    return result;
  }

  /**
   * Extracts files from a directory based on GLOB expression
   */
  private List<Map<String, Object>> globFiles(final CWLJob job, final Path workingDir, HashAlgorithm hashAlgorithm, final CWLOutputPort outputPort,
      Object outputBinding) throws CWLGlobException {
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

    List<Path> files = globService.glob(job, workingDir, glob);
    if (files == null) {
      logger.info("Glob service didn't find any files.");
      return null;
    }
    logger.debug("Glob service returned result {}", files);

    final List<Map<String, Object>> result = new ArrayList<>();
    for (Path file : files) {
      try {
        result.add(formFileValue(file, job, outputBinding, outputPort, hashAlgorithm, workingDir));
      } catch (Exception e) {
        throw new CWLGlobException("Failed to extract outputs.", e);
      }
    }
    return result;
  }

  public Map<String, Object> formFileValue(Path file, CWLJob job, Object outputBinding, CWLOutputPort outputPort, HashAlgorithm hashAlgorithm, Path workingDir)
      throws CWLExpressionException, IOException, URISyntaxException {
    Map<String, Object> fileData = CWLFileValueHelper.pathToRawFile(file, hashAlgorithm, workingDir);
    if (Files.isDirectory(file)) {
      return fileData;
    }
    List<?> secondaryFiles = getSecondaryFiles(job, hashAlgorithm, fileData, file, outputPort.getSecondaryFiles(), workingDir, true);
    if (secondaryFiles != null) {
      CWLFileValueHelper.setSecondaryFiles(secondaryFiles, fileData);
    }
    Object metadata = CWLBindingHelper.getMetadata(outputBinding);
    metadata = metadataService.evaluateMetadataExpressions(job, fileData, metadata);
    if (metadata != null) {
      logger.info("Metadata expressions evaluated. Metadata is {}.", metadata);
      CWLFileValueHelper.setMetadata(metadata, fileData);
    }
    metadata = metadataService.processMetadata(job, fileData, outputPort, outputBinding);
    if (metadata != null) {
      logger.info("Metadata for {} resolved. Metadata is {}", outputPort.getId(), metadata);
      CWLFileValueHelper.setMetadata(metadata, fileData);
    }
    
    boolean loadContents = CWLBindingHelper.loadContents(outputBinding);
    if (loadContents) {
      CWLFileValueHelper.setContents(fileData);
    }
    return fileData;
  }

  /**
   * Gets secondary files (absolute paths)
   * @throws URISyntaxException 
   */
  public static List<Map<String, Object>> getSecondaryFiles(CWLJob job, HashAlgorithm hashAlgorithm, Map<String, Object> fileValue, String filePath,
      Object secs, File workingDir, boolean onlyExisting) throws CWLExpressionException, IOException, URISyntaxException {
    return getSecondaryFiles(job, hashAlgorithm, fileValue, Paths.get(filePath), secs, workingDir.toPath(), onlyExisting);
  }


  /**
   * Gets secondary files (absolute paths)
   * @throws URISyntaxException 
   */
  @SuppressWarnings("unchecked")
  public static List<Map<String, Object>> getSecondaryFiles(CWLJob job, HashAlgorithm hashAlgorithm, Map<String, Object> fileValue, Path filePath, Object secs,
      Path workingDir, boolean onlyExisting) throws CWLExpressionException, IOException, URISyntaxException {
    Object secondaryFilesObj = secs;
    if (secondaryFilesObj == null) {
      return null;
    }
    if (secondaryFilesObj instanceof String || CWLExpressionResolver.isExpressionObject(secondaryFilesObj)) {
      secondaryFilesObj = CWLExpressionResolver.resolve(secondaryFilesObj, job, fileValue);
    }

    List<Object> secondaryFilesList = new ArrayList<>();
    if (secondaryFilesObj instanceof List<?>) {
      secondaryFilesList.addAll((Collection<? extends Object>) secondaryFilesObj);
    } else if (secondaryFilesObj instanceof String) {
      secondaryFilesList.add(secondaryFilesObj);
    }

    List<Map<String, Object>> secondaryFileMaps = new ArrayList<>();
    for (Object suffixObj : secondaryFilesList) {
      Object expr = suffixObj;
      boolean append = true;
      if (CWLExpressionResolver.isExpressionObject(suffixObj) || ((String) suffixObj).contains("$")) {
        expr = CWLExpressionResolver.resolve(suffixObj, job, fileValue);
        append = false;
      }
      if (expr instanceof List) {
        for (Object e2 : ((List) expr)) {
          addSecondaryFile(hashAlgorithm, filePath, workingDir, onlyExisting, secondaryFileMaps, e2, append);
        }
      } else {
        addSecondaryFile(hashAlgorithm, filePath, workingDir, onlyExisting, secondaryFileMaps, expr, append);
      }
    }
    return secondaryFileMaps.isEmpty() ? null : secondaryFileMaps;
  }

  private static void addSecondaryFile(HashAlgorithm hashAlgorithm, Path filePath, Path workingDir, boolean onlyExisting,
      List<Map<String, Object>> secondaryFileMaps, Object expr, boolean append) throws IOException, URISyntaxException {
    Map<String, Object> secondaryFile = getSecondaryFile(hashAlgorithm, filePath, workingDir, expr, append, onlyExisting);
    if (secondaryFile != null && !secondaryFile.isEmpty())
      secondaryFileMaps.add(secondaryFile);
  }

  private static Map<String, Object> getSecondaryFile(HashAlgorithm hashAlgorithm, Path filePath, Path workingDir, Object expr, boolean append,
      boolean onlyExisting) throws IOException, URISyntaxException {
    Map<String, Object> secondaryFileMap = new HashMap<>();
    Path path;
    if (expr instanceof String) {
      String secondaryFilePath = "";
      String suffix = (String) expr;
      if (append) {
        secondaryFilePath = filePath.getFileName().toString();
        while (suffix.startsWith("^")) {
          int extensionIndex = secondaryFilePath.lastIndexOf(".");
          if (extensionIndex != -1) {
            secondaryFilePath = secondaryFilePath.substring(0, extensionIndex);
            suffix = suffix.substring(1);
          } else {
            break;
          }
        }
      }
      secondaryFilePath += suffix;
      path = filePath.getParent().resolve(secondaryFilePath);
      if (Files.exists(path) || !onlyExisting) {
        secondaryFileMap = CWLFileValueHelper.pathToRawFile(path, hashAlgorithm, workingDir);
      } else {
        path = path.resolve(".").normalize();
        if (Files.exists(path) || !onlyExisting) {
          secondaryFileMap = CWLFileValueHelper.pathToRawFile(path, hashAlgorithm, workingDir);
        }
      }
    } else if (expr instanceof Map) {// also string
      secondaryFileMap = (Map<String, Object>) expr;
      postprocessCreatedResults(secondaryFileMap, hashAlgorithm, workingDir);
    }
    return secondaryFileMap;
  }

  @Override
  public Object transformInputs(Object value, Job job, Object transform) throws BindingException {
    Object specificValue = CWLValueTranslator.translateToSpecific(value);
    CWLJob cwlJob = CWLJobHelper.getCWLJob(job);
    Object result = null;
    try {
      result = CWLExpressionResolver.resolve(transform, cwlJob, specificValue);
      return CWLValueTranslator.translateToCommon(result);
    } catch (CWLExpressionException e) {
      throw new BindingException(e);
    }
  }
}
