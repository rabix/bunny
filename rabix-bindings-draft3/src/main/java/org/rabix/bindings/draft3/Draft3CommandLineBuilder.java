package org.rabix.bindings.draft3;

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
import org.rabix.bindings.draft3.bean.Draft3CommandLineTool;
import org.rabix.bindings.draft3.bean.Draft3InputPort;
import org.rabix.bindings.draft3.bean.Draft3Job;
import org.rabix.bindings.draft3.expression.Draft3ExpressionException;
import org.rabix.bindings.draft3.expression.Draft3ExpressionResolver;
import org.rabix.bindings.draft3.helper.Draft3BindingHelper;
import org.rabix.bindings.draft3.helper.Draft3FileValueHelper;
import org.rabix.bindings.draft3.helper.Draft3JobHelper;
import org.rabix.bindings.draft3.helper.Draft3SchemaHelper;
import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.bindings.model.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;

public class Draft3CommandLineBuilder implements ProtocolCommandLineBuilder {

  private final static Logger logger = LoggerFactory.getLogger(Draft3CommandLineBuilder.class);
  
  public static final Escaper SHELL_ESCAPE;
  static {
      final Escapers.Builder builder = Escapers.builder();
      builder.addEscape('\'', "'\"'\"'");
      SHELL_ESCAPE = builder.build();
  }
  
  @Override
  public CommandLine buildCommandLineObject(Job job, File workingDir, FilePathMapper filePathMapper) throws BindingException {
    Draft3Job draft3Job = Draft3JobHelper.getDraft3Job(job);
    if (draft3Job.getApp().isExpressionTool()) {
      return null;
    }
    
    Draft3CommandLineTool commandLineTool = (Draft3CommandLineTool) draft3Job.getApp();
    List<String> commandLineParts = Lists.transform(buildCommandLineParts(draft3Job, workingDir, filePathMapper), new Function<Object, String>() {
      public String apply(Object obj) {
        return obj.toString();
      }
    });

    String stdin = null;
    try {
      stdin = commandLineTool.getStdin(draft3Job);
    } catch (Draft3ExpressionException e) {
      logger.error("Failed to extract standard input.", e);
      throw new BindingException("Failed to extract standard input.", e);
    }

    String stdout = null;
    try {
      stdout = commandLineTool.getStdout(draft3Job);
    } catch (Draft3ExpressionException e) {
      logger.error("Failed to extract standard output.", e);
      throw new BindingException("Failed to extract standard outputs.", e);
    }

    CommandLine commandLine = new CommandLine(commandLineParts, stdin, stdout, null);
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
  @SuppressWarnings("rawtypes")
  public List<Object> buildCommandLineParts(Draft3Job job, File workingDir, FilePathMapper filePathMapper) throws BindingException {
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
        
        if(schema instanceof Map && ((Map) schema).get("type").equals("record") && inputPort.getInputBinding() == null) {
          List<Draft3CommandLinePart> parts = buildRecordCommandLinePart(job, job.getInputs().get(Draft3SchemaHelper.normalizeId(key)), schema);
          commandLineParts.addAll(parts);
        }
        else {
          Draft3CommandLinePart part = buildCommandLinePart(job, inputPort, inputPort.getInputBinding(), job.getInputs().get(Draft3SchemaHelper.normalizeId(key)), schema, key);
          if (part != null) {
            commandLineParts.add(part);
          }
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
  
  
  @SuppressWarnings("rawtypes")
  private List<Draft3CommandLinePart> buildRecordCommandLinePart(Draft3Job job, Object value, Object schema) throws BindingException {
    List<Draft3CommandLinePart> result = new ArrayList<Draft3CommandLinePart>();
    for(Object sch: (List)((Map) schema).get("fields")) {
      if(sch instanceof Map && ((Map) sch).get("type").equals("record") && ((Map) sch).get("inputBinding") == null) {
        result.addAll(buildRecordCommandLinePart(job, value, sch));
      }
      else {
        Object inputBinding = Draft3SchemaHelper.getInputBinding(sch);
        Object key = Draft3SchemaHelper.getName(sch);
        if (inputBinding == null) {
          continue;
        }
        result.add(buildCommandLinePart(job, null, inputBinding,((Map) value).get(key), sch, (String) key));
      }
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
        value = Draft3ExpressionResolver.resolve(valueFrom, job, value);
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
        Object arrayItemSchema = Draft3SchemaHelper.getSchemaForArrayItem(item, commandLineTool.getSchemaDefs(), schema);
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
        if (StringUtils.isWhitespace(separator) && separator.length() > 0) {
          return new Draft3CommandLinePart.Builder(position, isFile).keyValue(keyValue).part(prefix).part(joinedItems).build();
        } else {
          return new Draft3CommandLinePart.Builder(position, isFile).keyValue(keyValue).part(prefix + separator + joinedItems).build();
        }
      }
      if (prefix == null) {
        return new Draft3CommandLinePart.Builder(position, isFile).keyValue(keyValue).parts(flattenedValues).build();
      }
      List<Object> prefixedValues = new ArrayList<>();
      prefixedValues.add(prefix);
      for (Object arrayItem : flattenedValues) {
        prefixedValues.add(arrayItem);
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