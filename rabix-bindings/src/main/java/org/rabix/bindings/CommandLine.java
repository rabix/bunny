package org.rabix.bindings;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class CommandLine {

  public static final String PART_SEPARATOR = "\u0020";
  
  private final List<String> parts;
  
  private final String standardIn;
  private final String standardOut;
  private final String standardError;
  
  public CommandLine(List<String> parts, String standardIn, String standardOut, String standardError) {
    this.parts = parts;
    this.standardIn = standardIn;
    this.standardOut = standardOut;
    this.standardError = standardError;
  }

  public String build() {
    StringBuilder builder = new StringBuilder();
   
    for (String part : parts) {
      builder.append(part).append(PART_SEPARATOR);
    }
    if (!StringUtils.isEmpty(standardIn)) {
      builder.append(PART_SEPARATOR).append("<").append(PART_SEPARATOR).append(standardIn);
    }
    if (!StringUtils.isEmpty(standardOut)) {
      builder.append(PART_SEPARATOR).append(">").append(PART_SEPARATOR).append(standardOut);
    }
    return normalizeCommandLine(builder.toString());
  }
  
  /**
   * Normalize command line (remove multiple spaces, etc.)
   */
  private String normalizeCommandLine(String commandLine) {
    return commandLine.trim().replaceAll(PART_SEPARATOR + "+", PART_SEPARATOR);
  }
  
  public List<String> getParts() {
    return parts;
  }

  public String getStandardIn() {
    return standardIn;
  }

  public String getStandardOut() {
    return standardOut;
  }

  public String getStandardError() {
    return standardError;
  }

  @Override
  public String toString() {
    return "CommandLine [parts=" + parts + ", standardIn=" + standardIn + ", standardOut=" + standardOut + ", standardError=" + standardError + "]";
  }
  
}
