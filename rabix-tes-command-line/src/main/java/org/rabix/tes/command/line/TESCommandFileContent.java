package org.rabix.tes.command.line;

import java.util.Map;
import java.util.Map.Entry;

import org.rabix.tes.command.line.service.TESCommandLineException;

public class TESCommandFileContent {

  public final static String KEY_SEPARATOR = "=";
  public final static String RESULT_FILENAME = "command.sh";
  public final static String CWL_OUTPUT_JSON = "cwl.output.json";

  public static class TESCommandResultContentBuilder {
    private String commandLine;
    private Map<String, String> environmentVariables;

    public TESCommandResultContentBuilder commandLine(String commandLine) {
      this.commandLine = commandLine;
      return this;
    }

    public TESCommandResultContentBuilder environmentVariables(Map<String, String> environmentVariables) {
      this.environmentVariables = environmentVariables;
      return this;
    }
    
    public String build() throws TESCommandLineException {
      if (commandLine == null) {
        throw new TESCommandLineException("Command line is not set.");
      } 
      StringBuilder contentBuilder = new StringBuilder();
      if (environmentVariables != null) {
        for (Entry<String, String> variableEntry : environmentVariables.entrySet()) {
          contentBuilder.append("export ").append(variableEntry.getKey()).append("=").append(variableEntry.getValue()).append("\n");
        }
      }
      contentBuilder.append(commandLine);
      return contentBuilder.toString();
    }
  }

}
