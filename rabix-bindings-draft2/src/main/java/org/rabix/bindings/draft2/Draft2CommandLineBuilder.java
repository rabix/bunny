package org.rabix.bindings.draft2;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.CommandLine;
import org.rabix.bindings.ProtocolCommandLineBuilder;
import org.rabix.bindings.draft2.bean.Draft2CommandLineTool;
import org.rabix.bindings.draft2.bean.Draft2InputPort;
import org.rabix.bindings.draft2.bean.Draft2Job;
import org.rabix.bindings.draft2.expression.Draft2ExpressionException;
import org.rabix.bindings.draft2.expression.helper.Draft2ExpressionBeanHelper;
import org.rabix.bindings.draft2.helper.Draft2BindingHelper;
import org.rabix.bindings.draft2.helper.Draft2FileValueHelper;
import org.rabix.bindings.draft2.helper.Draft2JobHelper;
import org.rabix.bindings.draft2.helper.Draft2SchemaHelper;
import org.rabix.bindings.mapper.FileMappingException;
import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.bindings.model.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class Draft2CommandLineBuilder implements ProtocolCommandLineBuilder {

  private final static Logger logger = LoggerFactory.getLogger(Draft2CommandLineBuilder.class);

  @Override
  public CommandLine buildCommandLineObject(Job job, File workingDir, FilePathMapper filePathMapper) throws BindingException {
    Draft2Job draft2Job = Draft2JobHelper.getDraft2Job(job);
    if (draft2Job.getApp().isExpressionTool()) {
      return null;
    }
    
    Draft2CommandLineTool commandLineTool = (Draft2CommandLineTool) draft2Job.getApp();
    List<CommandLine.Part> commandLineParts = Lists.transform(buildCommandLineParts(draft2Job, workingDir, filePathMapper), (obj ->
        new CommandLine.Part(obj.toString())));

    String stdin = null;
    try {
      stdin = commandLineTool.getStdin(draft2Job);
    } catch (Draft2ExpressionException e) {
      throw new BindingException("Failed to extract standard input.", e);
    }

    String stdout = null;
    try {
      stdout = commandLineTool.getStdout(draft2Job);
    } catch (Draft2ExpressionException e) {
      throw new BindingException("Failed to extract standard outputs.", e);
    }

    CommandLine commandLine = new CommandLine(commandLineParts, stdin, stdout, null, false);
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
   * Build command line arguments
   */
  public List<Object> buildCommandLineParts(Draft2Job job, File workingDir, FilePathMapper filePathMapper) throws BindingException {
    logger.info("Building command line parts...");

    Draft2CommandLineTool commandLineTool = (Draft2CommandLineTool) job.getApp();
    List<Draft2InputPort> inputPorts = commandLineTool.getInputs();
    List<Object> result = new LinkedList<>();

    try {
      List<Object> baseCmds = commandLineTool.getBaseCmd(job);
      result.addAll(baseCmds);

      List<Draft2CommandLinePart> commandLineParts = new LinkedList<>();

      if (commandLineTool.hasArguments()) {
        for (int i = 0; i < commandLineTool.getArguments().size(); i++) {
          Object argBinding = commandLineTool.getArguments().get(i);
          if (argBinding instanceof String) {
            Draft2CommandLinePart commandLinePart = new Draft2CommandLinePart.Builder(0, false).part(argBinding).keyValue("").build();
            commandLinePart.setArgsArrayOrder(i);
            commandLineParts.add(commandLinePart);
            continue;
          }
          Object argValue = commandLineTool.getArgument(job, argBinding);
          Map<String, Object> emptySchema = new HashMap<>();
          Draft2CommandLinePart commandLinePart = buildCommandLinePart(job, null, argBinding, argValue, emptySchema, null, filePathMapper);
          if (commandLinePart != null) {
            commandLinePart.setArgsArrayOrder(i);
            commandLineParts.add(commandLinePart);
          }
        }
      }

      for (Draft2InputPort inputPort : inputPorts) {
        String key = inputPort.getId();
        Object schema = inputPort.getSchema();

        Draft2CommandLinePart part = buildCommandLinePart(job, inputPort, inputPort.getInputBinding(), job.getInputs().get(Draft2SchemaHelper.normalizeId(key)), schema, key, filePathMapper);
        if (part != null) {
          commandLineParts.add(part);
        }
      }
      Collections.sort(commandLineParts, new Draft2CommandLinePart.CommandLinePartComparator());

      for (Draft2CommandLinePart part : commandLineParts) {
        List<Object> flattenedObjects = part.flatten();
        for (Object obj : flattenedObjects) {
          result.add(obj);
        }
      }
    } catch (Draft2ExpressionException e) {
      throw new BindingException("Failed to build command line.", e);
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  private Draft2CommandLinePart buildCommandLinePart(Draft2Job job, Draft2InputPort inputPort, Object inputBinding, Object value, Object schema, String key, FilePathMapper filePathMapper) throws BindingException {
    logger.debug("Building command line part for value {} and schema {}", value, schema);

    Draft2CommandLineTool commandLineTool = (Draft2CommandLineTool) job.getApp();
    
    if (inputBinding == null) {
      return null;
    }

    int position = Draft2BindingHelper.getPosition(inputBinding);
    String separator = Draft2BindingHelper.getSeparator(inputBinding);
    String prefix = Draft2BindingHelper.getPrefix(inputBinding);
    String itemSeparator = Draft2BindingHelper.getItemSeparator(inputBinding);
    String keyValue = inputPort != null ? inputPort.getId() : "";

    Object valueFrom = Draft2BindingHelper.getValueFrom(inputBinding);
    if (valueFrom != null) {
      if (Draft2ExpressionBeanHelper.isExpression(valueFrom)) {
        try {
          value = Draft2ExpressionBeanHelper.evaluate(job, value, valueFrom);
        } catch (Draft2ExpressionException e) {
          throw new BindingException(e);
        }
      } else {
        value = valueFrom;
      }
    }

    boolean isFile = Draft2SchemaHelper.isFileFromValue(value);
    if (isFile) {
      try {
        value = filePathMapper.map(Draft2FileValueHelper.getPath(value), new HashMap<>());
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
        return new Draft2CommandLinePart.Builder(position, isFile).part(prefix).keyValue(keyValue).build();
      } else {
        return null;
      }
    }

    if (value instanceof Map<?, ?>) {
      Draft2CommandLinePart.Builder commandLinePartBuilder = new Draft2CommandLinePart.Builder(position, isFile);
      commandLinePartBuilder.keyValue(keyValue);
      
      for (Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()) {
        String fieldKey = entry.getKey();
        Object fieldValue = entry.getValue();

        Object field = Draft2SchemaHelper.getField(fieldKey, Draft2SchemaHelper.getSchemaForRecordField(job.getApp().getSchemaDefs(), schema));          
        
        if (field == null) {
          logger.info("Field {} not found in schema {}", fieldKey, schema);
          continue;
        }

        Object fieldBinding = Draft2SchemaHelper.getInputBinding(field);
        Object fieldType = Draft2SchemaHelper.getType(field);
        Object fieldSchema = Draft2SchemaHelper.findSchema(commandLineTool.getSchemaDefs(), fieldType);

        Draft2CommandLinePart fieldCommandLinePart = buildCommandLinePart(job, inputPort, fieldBinding, fieldValue, fieldSchema, fieldKey, filePathMapper);

        if (fieldCommandLinePart != null) {
          fieldCommandLinePart.setKeyValue(fieldKey);
          commandLinePartBuilder.part(fieldCommandLinePart);
        }
      }
      return commandLinePartBuilder.build().sort();
    }

    if (value instanceof List<?>) {
      Draft2CommandLinePart.Builder commandLinePartBuilder = new Draft2CommandLinePart.Builder(position, isFile);
      commandLinePartBuilder.keyValue(keyValue);
      
      for (Object item : ((List<?>) value)) {
        Object arrayItemSchema = Draft2SchemaHelper.getSchemaForArrayItem(item, commandLineTool.getSchemaDefs(), schema);
        Object arrayItemInputBinding = new HashMap<>();
        if (schema != null && Draft2SchemaHelper.getInputBinding(schema) != null) {
          arrayItemInputBinding = (Map<String, Object>) Draft2SchemaHelper.getInputBinding(schema);
        }
        
        Draft2CommandLinePart subpart = buildCommandLinePart(job, inputPort, arrayItemInputBinding, item, arrayItemSchema, key, filePathMapper);
        if (subpart != null) {
          commandLinePartBuilder.part(subpart);
        }
      }

      Draft2CommandLinePart commandLinePart = commandLinePartBuilder.build();

      List<Object> flattenedValues = commandLinePart.flatten();

      if (itemSeparator != null) {
        String joinedItems = Joiner.on(itemSeparator).join(flattenedValues);
        if (prefix == null) {
          return new Draft2CommandLinePart.Builder(position, isFile).keyValue(keyValue).part(joinedItems).build();
        }
        if (StringUtils.isWhitespace(separator) && separator.length() > 0) {
          return new Draft2CommandLinePart.Builder(position, isFile).keyValue(keyValue).part(prefix).part(joinedItems).build();
        } else {
          return new Draft2CommandLinePart.Builder(position, isFile).keyValue(keyValue).part(prefix + separator + joinedItems).build();
        }
      }
      if (prefix == null) {
        return new Draft2CommandLinePart.Builder(position, isFile).keyValue(keyValue).parts(flattenedValues).build();
      }
      List<Object> prefixedValues = new ArrayList<>();
      prefixedValues.add(prefix);
      for (Object arrayItem : flattenedValues) {
        prefixedValues.add(arrayItem);
      }
      return new Draft2CommandLinePart.Builder(position, isFile).keyValue(keyValue).parts(prefixedValues).build();
    }

    if (prefix == null) {
      return new Draft2CommandLinePart.Builder(position, isFile).keyValue(keyValue).part(value).build();
    }
    if (Draft2BindingHelper.DEFAULT_SEPARATOR.equals(separator)) {
      return new Draft2CommandLinePart.Builder(position, isFile).keyValue(keyValue).part(prefix).part(value).build();
    }
    return new Draft2CommandLinePart.Builder(position, isFile).keyValue(keyValue).part(prefix + separator + value).build();
  }

}