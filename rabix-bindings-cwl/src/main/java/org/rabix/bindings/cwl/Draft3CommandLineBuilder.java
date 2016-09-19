package org.rabix.bindings.draft3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolCommandLineBuilder;
import org.rabix.bindings.draft3.bean.Draft3CommandLineTool;
import org.rabix.bindings.draft3.bean.Draft3InputPort;
import org.rabix.bindings.draft3.bean.Draft3Job;
import org.rabix.bindings.draft3.expression.Draft3ExpressionException;
import org.rabix.bindings.draft3.expression.Draft3ExpressionResolver;
import org.rabix.bindings.draft3.helper.Draft3BindingHelper;
import org.rabix.bindings.draft3.helper.Draft3FileValueHelper;
import org.rabix.bindings.draft3.helper.Draft3JobHelper;
import org.rabix.bindings.draft3.helper.Draft3SchemaHelper;
import org.rabix.bindings.model.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;

public class Draft3CommandLineBuilder implements ProtocolCommandLineBuilder {

  public static final String PART_SEPARATOR = "\u0020";

  private final static Logger logger = LoggerFactory.getLogger(Draft3CommandLineBuilder.class);
  
  public static final Escaper SHELL_ESCAPE;
  static {
      final Escapers.Builder builder = Escapers.builder();
      builder.addEscape('\'', "'\"'\"'");
      SHELL_ESCAPE = builder.build();
  }
  
  @Override
  public String buildCommandLine(Job job) throws BindingException {
    Draft3Job draft2Job = Draft3JobHelper.getDraft3Job(job);
    if (draft2Job.getApp().isExpressionTool()) {
      return null;
    }
    return buildCommandLine(draft2Job);
  }
  
  @Override
  public List<String> buildCommandLineParts(Job job) throws BindingException {
    Draft3Job draft2Job = Draft3JobHelper.getDraft3Job(job);
    if (!draft2Job.getApp().isCommandLineTool()) {
      return null;
    }
    return Lists.transform(buildCommandLineParts(draft2Job), new Function<Object, String>() {
      public String apply(Object obj) {
        return obj.toString();
      }
    });
  }
  
  /**
   * Builds command line string with both STDIN and STDOUT
   */
  public String buildCommandLine(Draft3Job job) throws BindingException {
    Draft3CommandLineTool commandLineTool = (Draft3CommandLineTool) job.getApp();
    
    List<Object> commandLineParts = buildCommandLineParts(job);
    StringBuilder builder = new StringBuilder();
    for (Object commandLinePart : commandLineParts) {
      builder.append(commandLinePart).append(PART_SEPARATOR);
    }

    String stdin = null;
    try {
      stdin = commandLineTool.getStdin(job);
    } catch (Draft3ExpressionException e) {
      logger.error("Failed to extract standard input.", e);
      throw new BindingException("Failed to extract standard input.", e);
    }
    if (!StringUtils.isEmpty(stdin)) {
      builder.append(PART_SEPARATOR).append("<").append(PART_SEPARATOR).append(stdin);
    }

    String stdout = null;
    try {
      stdout = commandLineTool.getStdout(job);
    } catch (Draft3ExpressionException e) {
      logger.error("Failed to extract standard output.", e);
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
  public List<Object> buildCommandLineParts(Draft3Job job) throws BindingException {
    logger.info("Building command line parts...");

    Draft3CommandLineTool commandLineTool = (Draft3CommandLineTool) job.getApp();
    List<Draft3InputPort> inputPorts = commandLineTool.getInputs();
    List<Object> result = new LinkedList<>();

    try {
      List<Object> baseCmds = commandLineTool.getBaseCmd(job);
      result.addAll(baseCmds);

      List<Draft3CommandLinePart> commandLineParts = new LinkedList<>();

      if (commandLineTool.hasArguments()) {
        for (int i = 0; i < commandLineTool.getArguments().size(); i++) {
          Object argBinding = commandLineTool.getArguments().get(i);
          if (argBinding instanceof String) {
            Draft3CommandLinePart commandLinePart = new Draft3CommandLinePart.Builder(0, false).part(argBinding).keyValue("").build();
            commandLinePart.setArgsArrayOrder(i);
            commandLineParts.add(commandLinePart);
            continue;
          }
          Object argValue = commandLineTool.getArgument(job, argBinding);
          Map<String, Object> emptySchema = new HashMap<>();
          Draft3CommandLinePart commandLinePart = buildCommandLinePart(job, null, argBinding, argValue, emptySchema, null);
          if (commandLinePart != null) {
            commandLinePart.setArgsArrayOrder(i);
            commandLineParts.add(commandLinePart);
          }
        }
      }

      for (Draft3InputPort inputPort : inputPorts) {
        String key = inputPort.getId();
        Object schema = inputPort.getSchema();

        Draft3CommandLinePart part = buildCommandLinePart(job, inputPort, inputPort.getInputBinding(), job.getInputs().get(Draft3SchemaHelper.normalizeId(key)), schema, key);
        if (part != null) {
          commandLineParts.add(part);
        }
      }
      Collections.sort(commandLineParts, new Draft3CommandLinePart.CommandLinePartComparator());

      for (Draft3CommandLinePart part : commandLineParts) {
        List<Object> flattenedObjects = part.flatten();
        for (Object obj : flattenedObjects) {
          result.add(obj);
        }
      }
    } catch (Draft3ExpressionException e) {
      logger.error("Failed to build command line.", e);
      throw new BindingException("Failed to build command line.", e);
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  private Draft3CommandLinePart buildCommandLinePart(Draft3Job job, Draft3InputPort inputPort, Object inputBinding, Object value, Object schema, String key) throws BindingException {
    logger.debug("Building command line part for value {} and schema {}", value, schema);

    Draft3CommandLineTool commandLineTool = (Draft3CommandLineTool) job.getApp();
    
    if (inputBinding == null) {
      return null;
    }

    int position = Draft3BindingHelper.getPosition(inputBinding);
    String separator = Draft3BindingHelper.getSeparator(inputBinding);
    String prefix = Draft3BindingHelper.getPrefix(inputBinding);
    String itemSeparator = Draft3BindingHelper.getItemSeparator(inputBinding);
    String keyValue = inputPort != null ? inputPort.getId() : "";

    Object valueFrom = Draft3BindingHelper.getValueFrom(inputBinding);
    if (valueFrom != null) {
      try {
        value = Draft3ExpressionResolver.resolve(valueFrom, job, null);
      } catch (Draft3ExpressionException e) {
        throw new BindingException(e);
      }
    }

    boolean isFile = Draft3SchemaHelper.isFileFromValue(value);
    if (isFile) {
      value = Draft3FileValueHelper.getPath(value);
    }

    if (value == null) {
      return null;
    }

    if (value instanceof Boolean) {
      if (((Boolean) value)) {
        if (prefix == null) {
          throw new BindingException("Missing prefix for " + inputPort.getId() + " input.");
        }
        return new Draft3CommandLinePart.Builder(position, isFile).part(prefix).keyValue(keyValue).build();
      } else {
        return null;
      }
    }

    if (value instanceof Map<?, ?>) {
      Draft3CommandLinePart.Builder commandLinePartBuilder = new Draft3CommandLinePart.Builder(position, isFile);
      commandLinePartBuilder.keyValue(keyValue);
      
      for (Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()) {
        String fieldKey = entry.getKey();
        Object fieldValue = entry.getValue();

        Object field = Draft3SchemaHelper.getField(fieldKey, Draft3SchemaHelper.getSchemaForRecordField(job.getApp().getSchemaDefs(), schema));
        if (field == null) {
          logger.info("Field {} not found in schema {}", fieldKey, schema);
          continue;
        }

        Object fieldBinding = Draft3SchemaHelper.getInputBinding(field);
        Object fieldType = Draft3SchemaHelper.getType(field);
        Object fieldSchema = Draft3SchemaHelper.findSchema(commandLineTool.getSchemaDefs(), fieldType);

        Draft3CommandLinePart fieldCommandLinePart = buildCommandLinePart(job, inputPort, fieldBinding, fieldValue, fieldSchema, fieldKey);

        if (fieldCommandLinePart != null) {
          fieldCommandLinePart.setKeyValue(fieldKey);
          commandLinePartBuilder.part(fieldCommandLinePart);
        }
      }
      return commandLinePartBuilder.build().sort();
    }

    if (value instanceof List<?>) {
      Draft3CommandLinePart.Builder commandLinePartBuilder = new Draft3CommandLinePart.Builder(position, isFile);
      commandLinePartBuilder.keyValue(keyValue);
      
      for (Object item : ((List<?>) value)) {
        Object arrayItemSchema = Draft3SchemaHelper.getSchemaForArrayItem(commandLineTool.getSchemaDefs(), schema);
        Object arrayItemInputBinding = new HashMap<>();
        if (schema != null && Draft3SchemaHelper.getInputBinding(schema) != null) {
          arrayItemInputBinding = (Map<String, Object>) Draft3SchemaHelper.getInputBinding(schema);
        }
        
        Draft3CommandLinePart subpart = buildCommandLinePart(job, inputPort, arrayItemInputBinding, item, arrayItemSchema, key);
        if (subpart != null) {
          commandLinePartBuilder.part(subpart);
        }
      }

      Draft3CommandLinePart commandLinePart = commandLinePartBuilder.build();

      List<Object> flattenedValues = commandLinePart.flatten();

      if (itemSeparator != null) {
        String joinedItems = Joiner.on(itemSeparator).join(flattenedValues);
        if (prefix == null) {
          return new Draft3CommandLinePart.Builder(position, isFile).part(joinedItems).build();
        }
        if (StringUtils.isWhitespace(separator)) {
          return new Draft3CommandLinePart.Builder(position, isFile).keyValue(keyValue).part(prefix).part(joinedItems).build();
        } else {
          return new Draft3CommandLinePart.Builder(position, isFile).keyValue(keyValue).part(prefix + separator + joinedItems).build();
        }
      }
      if (prefix == null) {
        return new Draft3CommandLinePart.Builder(position, isFile).keyValue(keyValue).parts(flattenedValues).build();
      }
      List<Object> prefixedValues = new ArrayList<>();
      for (Object arrayItem : flattenedValues) {
        prefixedValues.add(prefix + separator + arrayItem);
      }
      return new Draft3CommandLinePart.Builder(position, isFile).keyValue(keyValue).parts(prefixedValues).build();
    }

    if (prefix == null) {
      return new Draft3CommandLinePart.Builder(position, isFile).keyValue(keyValue).part(value).build();
    }
    if (Draft3BindingHelper.DEFAULT_SEPARATOR.equals(separator)) {
      return new Draft3CommandLinePart.Builder(position, isFile).keyValue(keyValue).part(prefix).part(value).build();
    }
    return new Draft3CommandLinePart.Builder(position, isFile).keyValue(keyValue).part(prefix + separator + value).build();
  }

}