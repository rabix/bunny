package org.rabix.backend.local.tes.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TESTask {

  @JsonProperty("name")
  private String name;
  @JsonProperty("projectID")
  private String projectId;
  @JsonProperty("description")
  private String description;
  @JsonProperty("inputs")
  private List<TESTaskParameter> inputs;
  @JsonProperty("outputs")
  private List<TESTaskParameter> outputs;
  @JsonProperty("resources")
  private TESResources resources;
  @JsonProperty("taskID")
  private String taskId;
  @JsonProperty("docker")
  private List<TESDockerExecutor> dockerExecutors;

  public TESTask(@JsonProperty("name") String name, @JsonProperty("projectID") String projectId,
      @JsonProperty("description") String description, @JsonProperty("inputs") List<TESTaskParameter> inputs,
      @JsonProperty("outputs") List<TESTaskParameter> outputs, @JsonProperty("resources") TESResources resources,
      @JsonProperty("taskID") String taskId, @JsonProperty("docker") List<TESDockerExecutor> dockerExecutors) {
    this.name = name;
    this.projectId = projectId;
    this.description = description;
    this.inputs = inputs;
    this.outputs = outputs;
    this.resources = resources;
    this.taskId = taskId;
    this.dockerExecutors = dockerExecutors;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getProjectId() {
    return projectId;
  }

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<TESTaskParameter> getInputs() {
    return inputs;
  }

  public void setInputs(List<TESTaskParameter> inputs) {
    this.inputs = inputs;
  }

  public List<TESTaskParameter> getOutputs() {
    return outputs;
  }

  public void setOutputs(List<TESTaskParameter> outputs) {
    this.outputs = outputs;
  }

  public TESResources getResources() {
    return resources;
  }

  public void setResources(TESResources resources) {
    this.resources = resources;
  }

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public List<TESDockerExecutor> getDockerExecutors() {
    return dockerExecutors;
  }

  public void setDockerExecutors(List<TESDockerExecutor> dockerExecutors) {
    this.dockerExecutors = dockerExecutors;
  }

  @Override
  public String toString() {
    return "TESTask [name=" + name + ", projectId=" + projectId + ", description=" + description + ", inputs=" + inputs
        + ", outputs=" + outputs + ", resources=" + resources + ", taskId=" + taskId + ", dockerExecutors="
        + dockerExecutors + "]";
  }

}
