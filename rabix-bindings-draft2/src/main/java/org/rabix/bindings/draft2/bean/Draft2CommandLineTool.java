package org.rabix.bindings.draft2.bean;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.draft2.expression.Draft2ExpressionException;
import org.rabix.bindings.draft2.expression.helper.Draft2ExpressionBeanHelper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = Draft2CommandLineTool.class)
public class Draft2CommandLineTool extends Draft2JobApp {

  public static final String KEY_ARGUMENT_VALUE = "valueFrom";

  @JsonProperty("stdin")
  private Object stdin;
  @JsonProperty("stdout")
  private Object stdout;
  @JsonProperty("baseCommand")
  private Object baseCommand;
  @JsonProperty("arguments")
  private List<Object> arguments;
  

  public Draft2CommandLineTool() {
    super();
    this.baseCommand = new ArrayList<>();
    this.arguments = new ArrayList<>();
  }

  @SuppressWarnings("unchecked")
  public List<Object> getBaseCmd(Draft2Job job) throws Draft2ExpressionException {
    List<Object> result = new LinkedList<>();

    if (baseCommand instanceof List<?>) {
      for (Object baseCmd : ((List<Object>) baseCommand)) {
        Object transformed = transformBaseCommand(job, baseCmd);
        if (transformed != null) {
          result.add(transformed);
        }
      }
    } else if (baseCommand instanceof String) {
      Object transformed = transformBaseCommand(job, baseCommand);
      if (transformed != null) {
        result.add(transformed);
      }
    }
    return result;
  }
  
  private Object transformBaseCommand(Draft2Job job, Object baseCommand) throws Draft2ExpressionException {
    if (Draft2ExpressionBeanHelper.isExpression(baseCommand)) {
      return Draft2ExpressionBeanHelper.evaluate(job, baseCommand);
    } else {
      return baseCommand;
    }
  }

  public String getStdin(Draft2Job job) throws Draft2ExpressionException {
    if (Draft2ExpressionBeanHelper.isExpression(stdin)) {
      return Draft2ExpressionBeanHelper.evaluate(job, stdin);
    }
    return stdin != null ? stdin.toString() : null;
  }

  public String getStdout(Draft2Job job) throws Draft2ExpressionException {
    if (Draft2ExpressionBeanHelper.isExpression(stdout)) {
      return Draft2ExpressionBeanHelper.evaluate(job, stdout);
    }
    return stdout != null ? stdout.toString() : null;
  }

  public String getStderr(Draft2Job job) throws Draft2ExpressionException {
    String stdout = getStdout(job);
    return changeExtension(stdout, "err");
  }

  @JsonIgnore
  public boolean hasArguments() {
    return arguments != null;
  }

  public List<Object> getArguments() {
    return arguments;
  }

  @JsonIgnore
  public Object getArgument(Draft2Job job, Object binding) throws Draft2ExpressionException {
    if (binding instanceof Map<?, ?>) {
      Object value = ((Map<?, ?>) binding).get(KEY_ARGUMENT_VALUE);
      if (value == null) {
        return null;
      }
      if (Draft2ExpressionBeanHelper.isExpression(value)) {
        return Draft2ExpressionBeanHelper.evaluate(job, value);
      }
      return value;
    }
    return null;
  }

  /**
   * Replaces extension if there is any
   */
  private String changeExtension(String fileName, String extension) {
    if (fileName == null) {
      return null;
    }
    int lastIndexOf = fileName.lastIndexOf(".");
    return lastIndexOf != -1 ? fileName.substring(0, lastIndexOf + 1) + extension : fileName + "." + extension;
  }

  @Override
  @JsonIgnore
  public Draft2JobAppType getType() {
    return Draft2JobAppType.COMMAND_LINE_TOOL;
  }

  @Override
  public String toString() {
    return "CommandLineTool [stdin=" + stdin + ", stdout=" + stdout + ", baseCommands=" + baseCommand + ", arguments="
        + arguments + ", successCodes=" + successCodes + ", id=" + getId() + ", context=" + getContext() + ", description="
        + getDescription() + ", label=" + getLabel() + ", contributor=" + contributor + ", owner=" + owner + ", inputs=" + getInputs()
        + ", outputs=" + getOutputs() + ", requirements=" + requirements + "]";
  }

}
