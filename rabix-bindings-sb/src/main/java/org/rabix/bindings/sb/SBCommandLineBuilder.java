package org.rabix.bindings.sb;

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
import org.rabix.bindings.mapper.FileMappingException;
import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.sb.bean.SBCommandLineTool;
import org.rabix.bindings.sb.bean.SBInputPort;
import org.rabix.bindings.sb.bean.SBJob;
import org.rabix.bindings.sb.expression.SBExpressionException;
import org.rabix.bindings.sb.expression.helper.SBExpressionBeanHelper;
import org.rabix.bindings.sb.helper.SBBindingHelper;
import org.rabix.bindings.sb.helper.SBFileValueHelper;
import org.rabix.bindings.sb.helper.SBJobHelper;
import org.rabix.bindings.sb.helper.SBSchemaHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class SBCommandLineBuilder implements ProtocolCommandLineBuilder {

  public static final String PART_SEPARATOR = "\u0020";

  private final static Logger logger = LoggerFactory.getLogger(SBCommandLineBuilder.class);

  @Override
  public CommandLine buildCommandLineObject(Job job, File workingDir, FilePathMapper filePathMapper) throws BindingException {
    SBJob sbJob = SBJobHelper.getSBJob(job);
    if (sbJob.getApp().isExpressionTool()) {
      return null;
    }
    
    SBCommandLineTool commandLineTool = (SBCommandLineTool) sbJob.getApp();
    List<CommandLine.Part> commandLineParts = Lists.transform(buildCommandLineParts(sbJob, workingDir, filePathMapper), (obj -> new CommandLine.Part(obj.toString())));

    String stdin = null;
    try {
      stdin = commandLineTool.getStdin(sbJob);
    } catch (SBExpressionException e) {
      throw new BindingException("Failed to extract standard input.", e);
    }

    String stdout = null;
    try {
      stdout = commandLineTool.getStdout(sbJob);
    } catch (SBExpressionException e) {
      throw new BindingException("Failed to extract standard outputs.", e);
    }

    CommandLine commandLine = new CommandLine(commandLineParts, stdin, stdout, null, true);
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
   * Builds command line string with both STDIN and STDOUT
   */
  public String buildCommandLine(SBJob job, File workingDir, FilePathMapper filePathMapper) throws BindingException {
    SBCommandLineTool commandLineTool = (SBCommandLineTool) job.getApp();
    
    List<Object> commandLineParts = buildCommandLineParts(job, workingDir, filePathMapper);
    StringBuilder builder = new StringBuilder();
    for (Object commandLinePart : commandLineParts) {
      builder.append(commandLinePart).append(PART_SEPARATOR);
    }

    String stdin = null;
    try {
      stdin = commandLineTool.getStdin(job);
    } catch (SBExpressionException e) {
      throw new BindingException("Failed to extract standard input.", e);
    }
    if (!StringUtils.isEmpty(stdin)) {
      builder.append(PART_SEPARATOR).append("<").append(PART_SEPARATOR).append(stdin);
    }

    String stdout = null;
    try {
      stdout = commandLineTool.getStdout(job);
    } catch (SBExpressionException e) {
      throw new BindingException("Failed to extract standard outputs.", e);
    }
    if (!StringUtils.isEmpty(stdout)) {
      builder.append(PART_SEPARATOR).append(">").append(PART_SEPARATOR).append(stdout);
    }

    String commandLine = normalizeCommandLine(builder.toString());
    logger.info("Command line built. CommandLine = {}", commandLine);
    return commandLine;
  }

  /**
   * Normalize command line (remove multiple spaces, etc.)
   */
  private String normalizeCommandLine(String commandLine) {
    return commandLine.trim().replaceAll(PART_SEPARATOR + "+", PART_SEPARATOR);
  }

  /**
   * Build command line arguments
   */
  public List<Object> buildCommandLineParts(SBJob job, File workingDir, FilePathMapper filePathMapper) throws BindingException {
    logger.info("Building command line parts...");

    SBCommandLineTool commandLineTool = (SBCommandLineTool) job.getApp();
    List<SBInputPort> inputPorts = commandLineTool.getInputs();
    List<Object> result = new LinkedList<>();

    try {
      List<Object> baseCmds = commandLineTool.getBaseCmd(job);
      result.addAll(baseCmds);

      List<SBCommandLinePart> commandLineParts = new LinkedList<>();

      if (commandLineTool.hasArguments()) {
        for (int i = 0; i < commandLineTool.getArguments().size(); i++) {
          Object argBinding = commandLineTool.getArguments().get(i);
          if (argBinding instanceof String) {
            SBCommandLinePart commandLinePart = new SBCommandLinePart.Builder(0, false).part(argBinding).keyValue("").build();
            commandLinePart.setArgsArrayOrder(i);
            commandLineParts.add(commandLinePart);
            continue;
          }
          Object argValue = commandLineTool.getArgument(job, argBinding);
          Map<String, Object> emptySchema = new HashMap<>();
          SBCommandLinePart commandLinePart = buildCommandLinePart(job, null, argBinding, argValue, emptySchema, null, filePathMapper);
          if (commandLinePart != null) {
            commandLinePart.setArgsArrayOrder(i);
            commandLineParts.add(commandLinePart);
          }
        }
      }

      for (SBInputPort inputPort : inputPorts) {
        String key = inputPort.getId();
        Object schema = inputPort.getSchema();

        SBCommandLinePart part = buildCommandLinePart(job, inputPort, inputPort.getInputBinding(), job.getInputs().get(SBSchemaHelper.normalizeId(key)), schema, key, filePathMapper);
        if (part != null) {
          commandLineParts.add(part);
        }
      }
      Collections.sort(commandLineParts, new SBCommandLinePart.CommandLinePartComparator());

      for (SBCommandLinePart part : commandLineParts) {
        List<Object> flattenedObjects = part.flatten();
        for (Object obj : flattenedObjects) {
          result.add(obj);
        }
      }
    } catch (SBExpressionException e) {
      throw new BindingException("Failed to build command line.", e);
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  private SBCommandLinePart buildCommandLinePart(SBJob job, SBInputPort inputPort, Object inputBinding, Object value, Object schema, String key, FilePathMapper filePathMapper) throws BindingException {
    logger.debug("Building command line part for value {} and schema {}", value, schema);

    SBCommandLineTool commandLineTool = (SBCommandLineTool) job.getApp();
    
    if (inputBinding == null) {
      return null;
    }

    int position = SBBindingHelper.getPosition(inputBinding);
    String separator = SBBindingHelper.getSeparator(inputBinding);
    String prefix = SBBindingHelper.getPrefix(inputBinding);
    String itemSeparator = SBBindingHelper.getItemSeparator(inputBinding);
    String keyValue = inputPort != null ? inputPort.getId() : "";

    Object valueFrom = SBBindingHelper.getValueFrom(inputBinding);
    if (valueFrom != null) {
      if (SBExpressionBeanHelper.isExpression(valueFrom)) {
        try {
          value = SBExpressionBeanHelper.evaluate(job, value, valueFrom);
        } catch (SBExpressionException e) {
          throw new BindingException(e);
        }
      } else {
        value = valueFrom;
      }
    }

    boolean isFile = SBSchemaHelper.isFileFromValue(value);
    if (isFile) {
      try {
        value = filePathMapper != null ? filePathMapper.map(SBFileValueHelper.getPath(value), new HashMap<>()) : SBFileValueHelper.getPath(value);
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
        return new SBCommandLinePart.Builder(position, isFile).part(prefix).keyValue(keyValue).build();
      } else {
        return null;
      }
    }

    if (value instanceof Map<?, ?>) {
      SBCommandLinePart.Builder commandLinePartBuilder = new SBCommandLinePart.Builder(position, isFile);
      commandLinePartBuilder.keyValue(keyValue);
      
      for (Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()) {
        String fieldKey = entry.getKey();
        Object fieldValue = entry.getValue();

        Object field = SBSchemaHelper.getField(fieldKey, SBSchemaHelper.getSchemaForRecordField(job.getApp().getSchemaDefs(), schema));
        if (field == null) {
          logger.info("Field {} not found in schema {}", fieldKey, schema);
          continue;
        }

        Object fieldBinding = SBSchemaHelper.getInputBinding(field);
        Object fieldType = SBSchemaHelper.getType(field);
        Object fieldSchema = SBSchemaHelper.findSchema(commandLineTool.getSchemaDefs(), fieldType);

        SBCommandLinePart fieldCommandLinePart = buildCommandLinePart(job, inputPort, fieldBinding, fieldValue, fieldSchema, fieldKey, filePathMapper);

        if (fieldCommandLinePart != null) {
          fieldCommandLinePart.setKeyValue(fieldKey);
          commandLinePartBuilder.part(fieldCommandLinePart);
        }
      }
      return commandLinePartBuilder.build().sort();
    }

    if (value instanceof List<?>) {
      SBCommandLinePart.Builder commandLinePartBuilder = new SBCommandLinePart.Builder(position, isFile);
      commandLinePartBuilder.keyValue(keyValue);
      
      for (Object item : ((List<?>) value)) {
        Object arrayItemSchema = SBSchemaHelper.getSchemaForArrayItem(commandLineTool.getSchemaDefs(), schema);
        Object arrayItemInputBinding = new HashMap<>();
        if (schema != null && SBSchemaHelper.getInputBinding(schema) != null) {
          arrayItemInputBinding = (Map<String, Object>) SBSchemaHelper.getInputBinding(schema);
        }
        
        SBCommandLinePart subpart = buildCommandLinePart(job, inputPort, arrayItemInputBinding, item, arrayItemSchema, key, filePathMapper);
        if (subpart != null) {
          commandLinePartBuilder.part(subpart);
        }
      }

      SBCommandLinePart commandLinePart = commandLinePartBuilder.build();

      List<Object> flattenedValues = commandLinePart.flatten();

      if (itemSeparator != null) {
        String joinedItems = Joiner.on(itemSeparator).join(flattenedValues);
        if (prefix == null) {
          return new SBCommandLinePart.Builder(position, isFile).part(joinedItems).build();
        }
        if (StringUtils.isWhitespace(separator) && separator.length() > 0) {
          return new SBCommandLinePart.Builder(position, isFile).keyValue(keyValue).part(prefix).part(joinedItems).build();
        } else {
          return new SBCommandLinePart.Builder(position, isFile).keyValue(keyValue).part(prefix + separator + joinedItems).build();
        }
      }
      if (prefix == null) {
        return new SBCommandLinePart.Builder(position, isFile).keyValue(keyValue).parts(flattenedValues).build();
      }
      List<Object> prefixedValues = new ArrayList<>();
      for (Object arrayItem : flattenedValues) {
        prefixedValues.add(prefix + separator + arrayItem);
      }
      return new SBCommandLinePart.Builder(position, isFile).keyValue(keyValue).parts(prefixedValues).build();
    }

    if (prefix == null) {
      return new SBCommandLinePart.Builder(position, isFile).keyValue(keyValue).part(value).build();
    }
    if (SBBindingHelper.DEFAULT_SEPARATOR.equals(separator)) {
      return new SBCommandLinePart.Builder(position, isFile).keyValue(keyValue).part(prefix).part(value).build();
    }
    return new SBCommandLinePart.Builder(position, isFile).keyValue(keyValue).part(prefix + separator + value).build();
  }

}