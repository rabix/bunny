package org.rabix.bindings.cwl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.CommandLine;
import org.rabix.bindings.ProtocolCommandLineBuilder;
import org.rabix.bindings.cwl.bean.CWLCommandLineTool;
import org.rabix.bindings.cwl.bean.CWLInputPort;
import org.rabix.bindings.cwl.bean.CWLJob;
import org.rabix.bindings.cwl.bean.CWLRuntime;
import org.rabix.bindings.cwl.expression.CWLExpressionException;
import org.rabix.bindings.cwl.expression.CWLExpressionResolver;
import org.rabix.bindings.cwl.helper.CWLBeanHelper;
import org.rabix.bindings.cwl.helper.CWLBindingHelper;
import org.rabix.bindings.cwl.helper.CWLFileValueHelper;
import org.rabix.bindings.cwl.helper.CWLJobHelper;
import org.rabix.bindings.cwl.helper.CWLRuntimeHelper;
import org.rabix.bindings.cwl.helper.CWLSchemaHelper;
import org.rabix.bindings.mapper.FileMappingException;
import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.bindings.model.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class CWLCommandLineBuilder implements ProtocolCommandLineBuilder {

  private final static Logger logger = LoggerFactory.getLogger(CWLCommandLineBuilder.class);
  
  public final static String SHELL_QUOTE_KEY = "shellQuote";
  
  @Override
  public CommandLine buildCommandLineObject(Job job, File workingDir, FilePathMapper filePathMapper) throws BindingException {
    CWLJob cwlJob = CWLJobHelper.getCWLJob(job);
    
    CWLRuntime remapedRuntime = CWLRuntimeHelper.remapTmpAndOutDir(cwlJob.getRuntime(), filePathMapper, job.getConfig());
    cwlJob.setRuntime(remapedRuntime);
    
    if (cwlJob.getApp().isExpressionTool()) {
      return null;
    }
    CWLCommandLineTool commandLineTool = (CWLCommandLineTool) cwlJob.getApp();

    String stdin = null;
    try {
      stdin = commandLineTool.getStdin(cwlJob);
    } catch (CWLExpressionException e) {
      throw new BindingException("Failed to extract standard input.", e);
    }
    
    String stdout = null;
    try {
      stdout = commandLineTool.getStdout(cwlJob);
    } catch (CWLExpressionException e) {
      throw new BindingException("Failed to extract standard output.", e);
    }
    if (!StringUtils.isEmpty(stdout)) {
      if (!stdout.startsWith("/")) {
        try {
          String mappedWorkingDir = filePathMapper.map(workingDir.getAbsolutePath(), job.getConfig());
          stdout = new File(mappedWorkingDir, stdout).getAbsolutePath();
        } catch (FileMappingException e) {
          throw new BindingException(e);
        }
      }
    }
    String stderr = null;
    try {
      stderr = commandLineTool.getStderr(cwlJob);
    } catch (CWLExpressionException e) {
      throw new BindingException("Failed to extract standard error.", e);
    }

    boolean runInShell = cwlJob.isShellCommandEscapeEnabled();

    CommandLine commandLine = new CommandLine(buildCommandLineParts(cwlJob, workingDir, filePathMapper), stdin, stdout, stderr, runInShell);
    logger.info("Command line built. CommandLine = {}", commandLine);
    return commandLine;
  }
  
  @Override
  public String buildCommandLine(Job job, File workingDir, FilePathMapper filePathMapper) throws BindingException {
    CommandLine commandLine = buildCommandLineObject(job, workingDir, filePathMapper);
    return commandLine != null ? commandLine.build() : null;
  }
  
  @Override
  public List<String> buildCommandLineParts(Job job, File workingDir, FilePathMapper filePathMapper) throws BindingException {
    CommandLine commandLine = buildCommandLineObject(job, workingDir, filePathMapper);
    return commandLine != null ? commandLine.getParts() : null;
  }
  
  /**
   * Get shellQuote flag 
   */
  private boolean getShellQuote(Object input) {
    return CWLBeanHelper.getValue(SHELL_QUOTE_KEY, input, true);
  }

  private boolean isShellQuote(CWLJob job, Object input) {
    return !job.isShellCommandEscapeEnabled() || getShellQuote(input);
  }
  
  /**
   * Build command line arguments
   */
  @SuppressWarnings("rawtypes")
  public List<CommandLine.Part> buildCommandLineParts(CWLJob job, File workingDir, FilePathMapper filePathMapper) throws BindingException {
    logger.debug("Building command line parts...");

    CWLCommandLineTool commandLineTool = (CWLCommandLineTool) job.getApp();
    List<CWLInputPort> inputPorts = commandLineTool.getInputs();
    List<CommandLine.Part> result = new ArrayList<>();

    try {
      List<Object> baseCmds = commandLineTool.getBaseCmd(job);
      result.addAll(Lists.transform(baseCmds, (obj -> new CommandLine.Part(obj.toString(), true))));

      List<CWLCommandLinePart> commandLineParts = new ArrayList<>();

      if (commandLineTool.hasArguments()) {
        for (int i = 0; i < commandLineTool.getArguments().size(); i++) {

          Object argBinding = commandLineTool.getArguments().get(i);
          Object argValue;
          if (argBinding instanceof Map<?,?>) {
            argValue = CWLBeanHelper.getValue(CWLCommandLineTool.KEY_ARGUMENT_VALUE, argBinding);
          } else {
            argValue = argBinding;
            argBinding = Collections.singletonMap(CWLCommandLineTool.KEY_ARGUMENT_VALUE, argValue);
          }

          Map<String, Object> emptySchema = new HashMap<>();
          CWLCommandLinePart commandLinePart = buildCommandLinePart(job, null, argBinding, argValue, emptySchema, null, filePathMapper);
          if (commandLinePart != null) {
            commandLinePart.setArgsArrayOrder(i);
            commandLineParts.add(commandLinePart);
          }
        }
      }
      for (CWLInputPort inputPort : inputPorts) {
        String key = inputPort.getId();
        Object schema = inputPort.getSchema();
        if(CWLSchemaHelper.isRecordFromSchema(schema) && inputPort.getInputBinding() == null) {
          List<CWLCommandLinePart> parts = buildRecordCommandLinePart(job, job.getInputs().get(CWLSchemaHelper.normalizeId(key)), schema, filePathMapper);
          commandLineParts.addAll(parts);          
        }
        else {
          CWLCommandLinePart part = buildCommandLinePart(job, inputPort, inputPort.getInputBinding(), job.getInputs().get(CWLSchemaHelper.normalizeId(key)), schema, key, filePathMapper);
          if (part != null) {
            commandLineParts.add(part);
          }
        }
      }
      Collections.sort(commandLineParts, new CWLCommandLinePart.CommandLinePartComparator());

      for (CWLCommandLinePart part : commandLineParts) {
        List<Object> flattenedObjects = part.flatten();
        for (Object obj : flattenedObjects) {
          if (obj instanceof CommandLine.Part) {
            result.add((CommandLine.Part) obj);
          } else {
            result.add(new CommandLine.Part(obj.toString(), true));
          }
        }
      }
    } catch (CWLExpressionException e) {
      throw new BindingException("Failed to build command line.", e);
    }
    return result;
  }
  
  @SuppressWarnings("rawtypes")
  private List<CWLCommandLinePart> buildRecordCommandLinePart(CWLJob job, Object value, Object schema, FilePathMapper filePathMapper) throws BindingException {
    List<CWLCommandLinePart> result = new ArrayList<CWLCommandLinePart>();
    Object schemaCopy = !CWLSchemaHelper.isRequired(schema) ? CWLSchemaHelper.getSchemaFromNonRequired(schema) : schema;
    List<Object> fields = (List<Object>) CWLSchemaHelper.getFields(schemaCopy);
    for(Object sch: fields) {
      if(CWLSchemaHelper.isRecordFromSchema(sch) && CWLSchemaHelper.getInputBinding(sch) == null) {
        result.addAll(buildRecordCommandLinePart(job, value, sch, filePathMapper));
      }
      else {
        Object inputBinding = CWLSchemaHelper.getInputBinding(sch);
        Object key = CWLSchemaHelper.getName(sch);
        if (inputBinding == null) {
          continue;
        }
        result.add(buildCommandLinePart(job, null, inputBinding,((Map) value).get(key), sch, (String) key, filePathMapper));
      }
    }
    return result;
  }
  
  private boolean hasInputBinding(CWLInputPort port){
    return CWLSchemaHelper.getInputBinding(port.getSchema()) != null;
  }

  @SuppressWarnings("unchecked")
  private CWLCommandLinePart buildCommandLinePart(CWLJob job, CWLInputPort inputPort, Object inputBinding, Object value, Object schema, String key, FilePathMapper filePathMapper) throws BindingException {
    logger.debug("Building command line part for value {} and schema {}", value, schema);

    CWLCommandLineTool commandLineTool = (CWLCommandLineTool) job.getApp();
    
    if (inputBinding == null){
      if (hasInputBinding(inputPort)) {
        inputBinding = new HashMap<String, Object>();
      } else {
        return null;
      }
    }
    
    int position = CWLBindingHelper.getPosition(inputBinding);
    String separator = CWLBindingHelper.getSeparator(inputBinding);
    String prefix = CWLBindingHelper.getPrefix(inputBinding);
    String itemSeparator = CWLBindingHelper.getItemSeparator(inputBinding);
    String keyValue = inputPort != null ? inputPort.getId() : "";
    
    Object valueFrom = CWLBindingHelper.getValueFrom(inputBinding);
    if (valueFrom != null) {
      try {
        value = CWLExpressionResolver.resolve(valueFrom, job, value);
      } catch (CWLExpressionException e) {
        throw new BindingException(e);
      }
    }

    boolean isFile = CWLSchemaHelper.isFileFromValue(value) || CWLSchemaHelper.isDirectoryFromValue(value);
    if (isFile) {
      try {
        value = filePathMapper != null ? filePathMapper.map(CWLFileValueHelper.getPath(value), new HashMap<>()) : CWLFileValueHelper.getPath(value);
      } catch (FileMappingException e) {
        throw new BindingException(e);
      }
    }

    if (value == null) {
      return null;
    }

    if (value instanceof Boolean) {
      if (((Boolean) value)) {
        if (prefix == null) {
          throw new BindingException("Missing prefix for " + inputPort.getId() + " input.");
        }
        return new CWLCommandLinePart.Builder(position, isFile).part(prefix).keyValue(keyValue).build();
      } else {
        return null;
      }
    }

    if (value instanceof Map<?, ?>) {
      CWLCommandLinePart.Builder commandLinePartBuilder = new CWLCommandLinePart.Builder(position, isFile);
      commandLinePartBuilder.keyValue(keyValue);
      
      for (Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()) {
        String fieldKey = entry.getKey();
        Object fieldValue = entry.getValue();

        Object field = CWLSchemaHelper.getField(fieldKey, CWLSchemaHelper.getSchemaForRecordField(job.getApp().getSchemaDefs(), schema));
        if (field == null) {
          logger.info("Field {} not found in schema {}", fieldKey, schema);
          continue;
        }

        Object fieldBinding = CWLSchemaHelper.getInputBinding(field);
        Object fieldType = CWLSchemaHelper.getType(field);
        Object fieldSchema = CWLSchemaHelper.findSchema(commandLineTool.getSchemaDefs(), fieldType);

        CWLCommandLinePart fieldCommandLinePart = buildCommandLinePart(job, inputPort, fieldBinding, fieldValue, fieldSchema, fieldKey, filePathMapper);

        if (fieldCommandLinePart != null) {
          fieldCommandLinePart.setKeyValue(fieldKey);
          commandLinePartBuilder.part(fieldCommandLinePart);
        }
      }
      return commandLinePartBuilder.build().sort();
    }

    if (value instanceof List<?>) {
      CWLCommandLinePart.Builder commandLinePartBuilder = new CWLCommandLinePart.Builder(position, isFile);
      commandLinePartBuilder.keyValue(keyValue);
      
      for (Object item : ((List<?>) value)) {
        Object arrayItemSchema = CWLSchemaHelper.getSchemaForArrayItem(item, commandLineTool.getSchemaDefs(), schema);
        Object arrayItemInputBinding = new HashMap<>();
        if (schema != null && CWLSchemaHelper.getInputBinding(schema) != null) {
          arrayItemInputBinding = (Map<String, Object>) CWLSchemaHelper.getInputBinding(schema);
        }
        
        CWLCommandLinePart subpart = buildCommandLinePart(job, inputPort, arrayItemInputBinding, item, arrayItemSchema, key, filePathMapper);
        if (subpart != null) {
          commandLinePartBuilder.part(subpart);
        }
      }

      CWLCommandLinePart commandLinePart = commandLinePartBuilder.build();

      List<Object> flattenedValues = commandLinePart.flatten();

      if (itemSeparator != null) {
        String joinedItems = Joiner.on(itemSeparator).join(flattenedValues);
        if (prefix == null) {
          return new CWLCommandLinePart.Builder(position, isFile).part(joinedItems).build();
        }
        if (StringUtils.isWhitespace(separator) && separator.length() > 0) {
          return new CWLCommandLinePart.Builder(position, isFile).keyValue(keyValue).part(prefix).part(joinedItems).build();
        } else {
          return new CWLCommandLinePart.Builder(position, isFile).keyValue(keyValue).part(prefix + separator + joinedItems).build();
        }
      }
      if (prefix == null) {
        return new CWLCommandLinePart.Builder(position, isFile).keyValue(keyValue).parts(flattenedValues).build();
      }
      List<Object> prefixedValues = new ArrayList<>();
      prefixedValues.add(prefix);
      for (Object arrayItem : flattenedValues) {
        prefixedValues.add(arrayItem);
      }
      return new CWLCommandLinePart.Builder(position, isFile).keyValue(keyValue).parts(prefixedValues).build();
    }

    boolean shellQuote = isShellQuote(job, inputBinding);

    if (prefix == null) {
      return new CWLCommandLinePart.Builder(position, isFile).keyValue(keyValue).part(new CommandLine.Part(value.toString(), shellQuote)).build();
    }
    if (CWLBindingHelper.DEFAULT_SEPARATOR.equals(separator)) {
      return new CWLCommandLinePart.Builder(position, isFile).keyValue(keyValue).part(prefix).part(new CommandLine.Part(value.toString(), shellQuote)).build();
    }
    return new CWLCommandLinePart.Builder(position, isFile).keyValue(keyValue).part(new CommandLine.Part(prefix + separator + value, shellQuote)).build();
  }

}