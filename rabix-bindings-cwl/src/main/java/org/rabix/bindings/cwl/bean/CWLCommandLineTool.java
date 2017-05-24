package org.rabix.bindings.cwl.bean;

import java.util.*;
import java.util.stream.Collectors;

import org.rabix.bindings.cwl.bean.resource.CWLResourceType;
import org.rabix.bindings.cwl.bean.resource.requirement.CWLDockerResource;
import org.rabix.bindings.cwl.expression.CWLExpressionException;
import org.rabix.bindings.cwl.expression.CWLExpressionResolver;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.rabix.bindings.model.ValidationReport;

@JsonDeserialize(as = CWLCommandLineTool.class)
public class CWLCommandLineTool extends CWLJobApp {

  public static final String KEY_ARGUMENT_VALUE = "valueFrom";
  
  public static final String STDIN_KEY = "stdin";
  public static final String STDOUT_KEY = "stdout";
  public static final String STDERR_KEY = "stderr";
  
  public static final String RANDOM_STDOUT_PREFIX = "random_stdout_";
  public static final String RANDOM_STDERR_PREFIX = "random_error_";

  @JsonProperty("stdin")
  private String stdin;
  @JsonProperty("stdout")
  private String stdout;
  @JsonProperty("stderr")
  private String stderr;
  @JsonProperty("baseCommand")
  private Object baseCommand;
  @JsonProperty("arguments")
  private List<Object> arguments;

  public CWLCommandLineTool() {
    super();
    this.baseCommand = new ArrayList<>();
    this.arguments = new ArrayList<>();
  }

  @SuppressWarnings("unchecked")
  public List<Object> getBaseCmd(CWLJob job) throws CWLExpressionException {
    List<Object> result = new ArrayList<>();
    if (baseCommand instanceof List<?>) {
      result = (List<Object>) baseCommand;
    } else if (baseCommand instanceof String) {
      result.add(baseCommand);
    }
    return result;
  }
  
  public String getStdin(CWLJob job) throws CWLExpressionException {
    String evaluatedStdin = CWLExpressionResolver.resolve(stdin, job, null);
    return evaluatedStdin;
  }
  
  public void setStdin(String stdin) {
    this.stdin = stdin;
  }

  public String getStdout(CWLJob job) throws CWLExpressionException {
    String evaluatedStdout = CWLExpressionResolver.resolve(stdout, job, null);
    return evaluatedStdout;
  }
  
  @JsonIgnore
  public Object getStdoutRaw() {
    return stdout;
  }

  public void setStdout(String stdout) {
    this.stdout = stdout;
  }
  
  public String getStderr(CWLJob job) throws CWLExpressionException {
    String evaluatedStderr = CWLExpressionResolver.resolve(stderr, job, null);
    return evaluatedStderr;
  }
  
  public void setStderr(String stderr) {
    this.stderr = stderr;
  }
  
  @JsonIgnore
  public Object getStderrRaw() {
    return stderr;
  }

  @JsonIgnore
  public boolean hasArguments() {
    return arguments != null;
  }

  public List<Object> getArguments() {
    return arguments;
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
        + getDescription() + ", label=" + getLabel() + ", inputs=" + getInputs()
        + ", outputs=" + getOutputs() + ", requirements=" + requirements + "]";
  }

  @Override
  public ValidationReport validate() {
    List<ValidationReport.Item> messages = new ArrayList<>();
    CWLDockerResource dockerResource = lookForResource(CWLResourceType.DOCKER_RESOURCE, CWLDockerResource.class);
    messages.addAll(ValidationReport.messagesToItems(checkDockerRequirement(dockerResource), ValidationReport.Severity.ERROR));
    messages.addAll(ValidationReport.messagesToItems(validatePortUniqueness(), ValidationReport.Severity.ERROR));
    messages.addAll(validateBaseCommand());
    messages.addAll(validateArguments());
    return new ValidationReport(messages);
  }

  private List<ValidationReport.Item> validateBaseCommand() {
    List<ValidationReport.Item> messages = new ArrayList<>();
    if (baseCommand == null) {
      messages.add(ValidationReport.warning("Tool doesn't have a 'baseCommand'"));
    } else if (baseCommand instanceof String) {
      if (((String) baseCommand).isEmpty()) {
        messages.add(ValidationReport.warning("Tool's 'baseCommand' is empty"));
      }
    } else if (baseCommand instanceof List) {
      List baseList = (List) baseCommand;
      if (baseList.isEmpty()) {
        messages.add(ValidationReport.warning("Tool's 'baseCommand' is empty"));
      } else {
        for (Object o : baseList) {
          if (! (o instanceof String)) {
            messages.add(ValidationReport.error("Tool's 'baseCommand' must be a string or a list of strings, got '" + o + "' instead"));
          }
        }
      }
    } else {
      messages.add(ValidationReport.error("Tool's 'baseCommand' must be a string or a list of strings, got '" + baseCommand + "' instead"));
    }

    return messages;
  }

  private List<ValidationReport.Item> validateArguments() {
    List<ValidationReport.Item> messages = new ArrayList<>();

    for (Object argument : arguments) {
      if (argument instanceof Map<?, ?>) {
        if (!((Map) argument).containsKey("valueFrom")) {
          messages.add(ValidationReport.error("CommandLineBinding in 'arguments' must have a 'valueFrom' present"));
        }

      } else if (! (argument instanceof String)) {
        messages.add(ValidationReport.error("Tool's 'arguments' must be a list of strings or CommandLineBindings, got '" + argument + "' instead"));
      }
    }

    return messages;
  }
}
