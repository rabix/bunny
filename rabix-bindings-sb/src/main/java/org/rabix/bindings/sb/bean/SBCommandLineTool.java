package org.rabix.bindings.sb.bean;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.sb.expression.SBExpressionException;
import org.rabix.bindings.sb.expression.helper.SBExpressionBeanHelper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = SBCommandLineTool.class)
public class SBCommandLineTool extends SBJobApp {

  public static final String KEY_ARGUMENT_VALUE = "valueFrom";

  @JsonProperty("stdin")
  private Object stdin;
  @JsonProperty("stdout")
  private Object stdout;
  @JsonProperty("baseCommand")
  private Object baseCommand;
  @JsonProperty("arguments")
  private List<Object> arguments;
  

  public SBCommandLineTool() {
    super();
    this.baseCommand = new ArrayList<>();
    this.arguments = new ArrayList<>();
  }

  @SuppressWarnings("unchecked")
  public List<Object> getBaseCmd(SBJob job) throws SBExpressionException {
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
  
  private Object transformBaseCommand(SBJob job, Object baseCommand) throws SBExpressionException {
    if (SBExpressionBeanHelper.isExpression(baseCommand)) {
      return SBExpressionBeanHelper.evaluate(job, baseCommand);
    } else {
      return baseCommand;
    }
  }

  public String getStdin(SBJob job) throws SBExpressionException {
    if (SBExpressionBeanHelper.isExpression(stdin)) {
      return SBExpressionBeanHelper.evaluate(job, stdin);
    }
    return stdin != null ? stdin.toString() : null;
  }

  public String getStdout(SBJob job) throws SBExpressionException {
    if (SBExpressionBeanHelper.isExpression(stdout)) {
      return SBExpressionBeanHelper.evaluate(job, stdout);
    }
    return stdout != null ? stdout.toString() : null;
  }

  public String getStderr(SBJob job) throws SBExpressionException {
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
  public Object getArgument(SBJob job, Object binding) throws SBExpressionException {
    if (binding instanceof Map<?, ?>) {
      Object value = ((Map<?, ?>) binding).get(KEY_ARGUMENT_VALUE);
      if (value == null) {
        return null;
      }
      if (SBExpressionBeanHelper.isExpression(value)) {
        return SBExpressionBeanHelper.evaluate(job, value);
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
  public SBJobAppType getType() {
    return SBJobAppType.COMMAND_LINE_TOOL;
  }

  @Override
  public String toString() {
    return "CommandLineTool [stdin=" + stdin + ", stdout=" + stdout + ", baseCommands=" + baseCommand + ", arguments="
        + arguments + ", successCodes=" + successCodes + ", id=" + getId() + ", context=" + getContext() + ", description="
        + getDescription() + ", label=" + getLabel() + ", contributor=" + contributor + ", owner=" + owner + ", inputs=" + getInputs()
        + ", outputs=" + getOutputs() + ", requirements=" + requirements + "]";
  }

}
