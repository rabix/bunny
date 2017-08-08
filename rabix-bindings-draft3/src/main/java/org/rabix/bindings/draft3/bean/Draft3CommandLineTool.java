package org.rabix.bindings.draft3.bean;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.draft3.expression.Draft3ExpressionException;
import org.rabix.bindings.draft3.expression.Draft3ExpressionResolver;
import org.rabix.bindings.model.JobAppType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = Draft3CommandLineTool.class)
public class Draft3CommandLineTool extends Draft3JobApp {

  public static final String KEY_ARGUMENT_VALUE = "valueFrom";

  @JsonProperty("stdin")
  private Object stdin;
  @JsonProperty("stdout")
  private Object stdout;
  @JsonProperty("baseCommand")
  private Object baseCommand;
  @JsonProperty("arguments")
  private List<Object> arguments;
  @JsonProperty("resources")
  private Object runtime;
  

  public Draft3CommandLineTool() {
    super();
    this.baseCommand = new ArrayList<>();
    this.arguments = new ArrayList<>();
  }

  @SuppressWarnings("unchecked")
  public List<Object> getBaseCmd(Draft3Job job) throws Draft3ExpressionException {
    List<Object> result = new LinkedList<>();
    if (baseCommand instanceof List<?>) {
      result = (List<Object>) baseCommand;
    } else if (baseCommand instanceof String) {
      result = new LinkedList<>();
      result.add(baseCommand);
    }
    return result;
  }
  
  public String getStdin(Draft3Job job) throws Draft3ExpressionException {
    String evaluatedStdin = Draft3ExpressionResolver.resolve(stdin, job, null);
    return evaluatedStdin != null ? evaluatedStdin.toString() : null;
  }

  public String getStdout(Draft3Job job) throws Draft3ExpressionException {
    String evaluatedStdout = Draft3ExpressionResolver.resolve(stdout, job, null);
    return evaluatedStdout != null ? evaluatedStdout.toString() : null;
  }

  public String getStderr(Draft3Job job) throws Draft3ExpressionException {
    String stdout = getStdout(job);
    return changeExtension(stdout, "err");
  }
  
  @SuppressWarnings("unchecked")
  public Draft3Runtime getRuntime() {
    Long cpu = null;
    Long mem = null;
    String outdir = null;
    String tmpdir = null;
    Long outdirSize = null;
    Long tmpdirSize = null;
    if(runtime instanceof Map) {
      cpu = (Long) ((Map<String, Object>) runtime).get("cpu");
      mem = (Long) ((Map<String, Object>) runtime).get("mem");
      outdir = (String) ((Map<String, Object>) runtime).get("workingDir");
      tmpdir = (String) ((Map<String, Object>) runtime).get("workingDir");
    }
    return new Draft3Runtime(cpu, mem, outdir, tmpdir, outdirSize, tmpdirSize);
  }

  @JsonIgnore
  public boolean hasArguments() {
    return arguments != null;
  }

  public List<Object> getArguments() {
    return arguments;
  }

  @JsonIgnore
  public Object getArgument(Draft3Job job, Object binding) throws Draft3ExpressionException {
    if (binding instanceof Map<?, ?>) {
      Object value = ((Map<?, ?>) binding).get(KEY_ARGUMENT_VALUE);
      if (value != null) {
        return Draft3ExpressionResolver.resolve(value, job, null);
      }
    }
    return Draft3ExpressionResolver.resolve(binding, job, null);
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
  public JobAppType getType() {
    return JobAppType.COMMAND_LINE_TOOL;
  }

  @Override
  public String toString() {
    return "CommandLineTool [stdin=" + stdin + ", stdout=" + stdout + ", baseCommands=" + baseCommand + ", arguments="
        + arguments + ", successCodes=" + successCodes + ", id=" + getId() + ", context=" + getContext() + ", description="
        + getDescription() + ", label=" + getLabel() + ", contributor=" + contributor + ", owner=" + owner + ", inputs=" + getInputs()
        + ", outputs=" + getOutputs() + ", requirements=" + requirements + "]";
  }

}
