package org.rabix.executor.container.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

class TemplateScope {
  String image;
  Set<VolumeMap> volumes = new HashSet<>();
  String workingDir;
  String command;
  Map<String, String> env;
  UUID jobId;
  String jobName;

  public void setJobName(String jobName) {
    this.jobName = jobName;
  }

  public void setJobId(UUID jobId) {
    this.jobId = jobId;
  }

  public String getImage() {
    return image;
  }

  public void setImage(String image) {
    this.image = image;
  }

  public Set<VolumeMap> getVolumes() {
    return volumes;
  }

  public void setVolumes(Set<VolumeMap> volumes) {
    this.volumes = volumes;
  }

  public String getWorkingDir() {
    return workingDir;
  }

  public void setWorkingDir(String workingDir) {
    this.workingDir = workingDir;
  }

  public String getCommand() {
    return command;
  }

  public Function<String, String> escape() {
    return (String value)-> value.replaceAll("\\\"", "\\\\\"").replaceAll("\\$", java.util.regex.Matcher.quoteReplacement("\\$"));
  }

  public void setCommand(String command) {
    this.command = command;
  }

  public Set<Entry<String, String>> getEnv() {
    return env == null ? Collections.emptySet() : env.entrySet();
  }

  public void setEnv(Map<String, String> env) {
    this.env = env;
  }

  public static class VolumeMap {
    String path;
    String location;

    public VolumeMap(String path, String location) {
      super();
      this.path = path;
      this.location = location;
    }
  }
}
