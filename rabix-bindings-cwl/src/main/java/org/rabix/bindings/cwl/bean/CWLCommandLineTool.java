package org.rabix.bindings.cwl.bean;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.rabix.bindings.cwl.expression.CWLExpressionException;
import org.rabix.bindings.cwl.expression.CWLExpressionResolver;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = CWLCommandLineTool.class)
public class CWLCommandLineTool extends CWLJobApp {

  public static final String KEY_ARGUMENT_VALUE = "valueFrom";
  
  public static final String STDIN_KEY = "stdin";
  public static final String STDOUT_KEY = "stdout";
  public static final String STDERR_KEY = "stderr";
  
  public static final String RANDOM_STDOUT_PREFIX = "random_stdout_";
  public static final String RANDOM_STDERR_PREFIX = "random_error_";

  @JsonProperty("stdin")
  private Object stdin;
  @JsonProperty("stdout")
  private Object stdout;
  @JsonProperty("stderr")
  private Object stderr;
  @JsonProperty("baseCommand")
  private Object baseCommand;
  @JsonProperty("arguments")
  private List<Object> arguments;
  @JsonProperty("runtime")
  private Object runtime;

  public CWLCommandLineTool() {
    super();
    this.baseCommand = new ArrayList<>();
    this.arguments = new ArrayList<>();
  }

  @SuppressWarnings("unchecked")
  public List<Object> getBaseCmd(CWLJob job) throws CWLExpressionException {
    List<Object> result = new LinkedList<>();
    if (baseCommand instanceof List<?>) {
      result = (List<Object>) baseCommand;
    } else if (baseCommand instanceof String) {
      result = new LinkedList<>();
      result.add(baseCommand);
    }
    return result;
  }
  
  public String getStdin(CWLJob job) throws CWLExpressionException {
    String evaluatedStdin = CWLExpressionResolver.resolve(stdin, job, null);
    return evaluatedStdin != null ? evaluatedStdin : null;
  }
  
  public void setStdin(Object stdin) {
    this.stdin = stdin;
  }

  public String getStdout(CWLJob job) throws CWLExpressionException {
    String evaluatedStdout = CWLExpressionResolver.resolve(stdout, job, null);
    return evaluatedStdout != null ? evaluatedStdout : null;
  }
  
  @JsonIgnore
  public Object getStdoutRaw() {
    return stdout;
  }

  public void setStdout(Object stdout) {
    this.stdout = stdout;
  }
  
  public String getStderr(CWLJob job) throws CWLExpressionException {
    String evaluatedStderr = CWLExpressionResolver.resolve(stderr, job, null);
    return evaluatedStderr != null ? evaluatedStderr : null;
  }
  
  public void setStderr(Object stderr) {
    this.stderr = stderr;
  }
  
  @JsonIgnore
  public Object getStderrRaw() {
    return stderr;
  }
  
  @SuppressWarnings("unchecked")
  public CWLRuntime getRuntime() {
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
    return new CWLRuntime(cpu, mem, outdir, tmpdir, outdirSize, tmpdirSize);
  }

  @JsonIgnore
  public boolean hasArguments() {
    return arguments != null;
  }

  public List<Object> getArguments() {
    return arguments;
  }

  @JsonIgnore
  public Object getArgument(CWLJob job, Object binding) throws CWLExpressionException {
    if (binding instanceof Map<?, ?>) {
      Object value = ((Map<?, ?>) binding).get(KEY_ARGUMENT_VALUE);
      if (value != null) {
        return CWLExpressionResolver.resolve(value, job, null);
      }
    }
    return CWLExpressionResolver.resolve(binding, job, null);
  }

  public static String generateRandomStdoutGlob() {
    return RANDOM_STDOUT_PREFIX + UUID.randomUUID().toString();
  }
  
  public static String generateRandomStderrGlob() {
    return RANDOM_STDERR_PREFIX + UUID.randomUUID().toString();
  }
  
  @Override
  @JsonIgnore
  public CWLJobAppType getType() {
    return CWLJobAppType.COMMAND_LINE_TOOL;
  }

  @Override
  public String toString() {
    return "CommandLineTool [stdin=" + stdin + ", stdout=" + stdout + ", baseCommands=" + baseCommand + ", arguments="
        + arguments + ", successCodes=" + successCodes + ", id=" + getId() + ", context=" + getContext() + ", description="
        + getDescription() + ", label=" + getLabel() + ", contributor=" + contributor + ", owner=" + owner + ", inputs=" + getInputs()
        + ", outputs=" + getOutputs() + ", requirements=" + requirements + "]";
  }

}
