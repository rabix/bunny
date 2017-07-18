package org.rabix.bindings;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.rabix.common.helper.EncodingHelper;

public class CommandLine {

  public static final String PART_SEPARATOR = "\u0020";
  
  private final List<Part> parts;
  
  private final String standardIn;
  private final String standardOut;
  private final String standardError;

  private final boolean runInShell;
  
  public CommandLine(List<Part> parts, String standardIn, String standardOut, String standardError, boolean runInShell) {
    this.parts = parts;
    this.standardIn = standardIn;
    this.standardOut = standardOut;
    this.standardError = standardError;
    this.runInShell = runInShell;
  }

  public String build() {
    StringBuilder builder = new StringBuilder();
   
    for (Part part : parts) {
      builder.append(part.toString()).append(PART_SEPARATOR);
    }
    if (!StringUtils.isEmpty(standardIn)) {
      builder.append("<").append(PART_SEPARATOR).append(standardIn).append(PART_SEPARATOR);
    }
    if (!StringUtils.isEmpty(standardOut)) {
      builder.append(">").append(PART_SEPARATOR).append(standardOut);
    }
    return normalizeCommandLine(builder.toString());
  }
  
  /**
   * Normalize command line (remove multiple spaces, etc.)
   */
  private String normalizeCommandLine(String commandLine) {
    return commandLine.trim();//.replaceAll(PART_SEPARATOR + "+", PART_SEPARATOR);
  }
  
  public List<String> getParts() {
    return parts.stream().map(Part::getValue).collect(Collectors.toList());
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

  public boolean isRunInShell() {
    return runInShell;
  }

  @Override
  public String toString() {
    return "CommandLine [parts=" + parts + ", standardIn=" + standardIn + ", standardOut=" + standardOut + ", standardError=" + standardError + "]";
  }

  public static class Part {
    private String value;
    private boolean quote;

    public Part(String value) {
      this.value = value;
    }

    public Part(String value, boolean quote) {
      this.value = value;
      this.quote = quote;
    }

    public String getValue() {
      return value;
    }

    public boolean isQuote() {
      return quote;
    }

    public String toString() {
      return quote ? EncodingHelper.shellQuote(value): value;
    }
  }
  
}
