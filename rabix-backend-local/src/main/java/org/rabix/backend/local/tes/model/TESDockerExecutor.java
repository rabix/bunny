package org.rabix.backend.local.tes.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TESDockerExecutor {
  
  @JsonProperty("imageName")
  private String imageName;
  @JsonProperty("cmd")
  private List<String> commandLineParts;
  @JsonProperty("workdir")
  private String workingDirectory;
  @JsonProperty("stdin")
  private String standardIn;
  @JsonProperty("stdout")
  private String standardOut;
  @JsonProperty("stderr")
  private String standardError;
  
  @JsonCreator
  public TESDockerExecutor(@JsonProperty("imageName") String imageName, @JsonProperty("cmd") List<String> commandLineParts, @JsonProperty("workdir") String workingDirectory, @JsonProperty("stdin") String standardIn, @JsonProperty("stdout") String standardOut, @JsonProperty("stderr") String standardError) {
    this.imageName = imageName;
    this.commandLineParts = commandLineParts;
    this.workingDirectory = workingDirectory;
    this.standardIn = standardIn;
    this.standardOut = standardOut;
    this.standardError = standardError;
  }

  public String getImageName() {
    return imageName;
  }

  public void setImageName(String imageName) {
    this.imageName = imageName;
  }

  public List<String> getCommandLineParts() {
    return commandLineParts;
  }

  public void setCommandLineParts(List<String> commandLineParts) {
    this.commandLineParts = commandLineParts;
  }

  public String getWorkingDirectory() {
    return workingDirectory;
  }

  public void setWorkingDirectory(String workingDirectory) {
    this.workingDirectory = workingDirectory;
  }

  public String getStandardIn() {
    return standardIn;
  }

  public void setStandardIn(String standardIn) {
    this.standardIn = standardIn;
  }

  public String getStandardOut() {
    return standardOut;
  }

  public void setStandardOut(String standardOut) {
    this.standardOut = standardOut;
  }

  public String getStandardError() {
    return standardError;
  }

  public void setStandardError(String standardError) {
    this.standardError = standardError;
  }

  @Override
  public String toString() {
    return "TESDockerExecutor [imageName=" + imageName + ", commandLineParts=" + commandLineParts
        + ", workingDirectory=" + workingDirectory + ", standardIn=" + standardIn + ", standardOut=" + standardOut
        + ", standardError=" + standardError + "]";
  }

}
