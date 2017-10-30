package org.rabix.backend.tes.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TESTask {

  @JsonProperty("id")
  private String id;
  @JsonProperty("state")
  private TESState state;
  @JsonProperty("name")
  private String name;
  @JsonProperty("project")
  private String project;
  @JsonProperty("description")
  private String description;
  @JsonProperty("inputs")
  private List<TESTaskParameter> inputs;
  @JsonProperty("outputs")
  private List<TESTaskParameter> outputs;
  @JsonProperty("resources")
  private TESResources resources;
  @JsonProperty("executors")
  private List<TESExecutor> executors;
  @JsonProperty("volumes")
  private List<String> volumes;
  @JsonProperty("tags")
  private Map<String, String> tags;
  @JsonProperty("logs")
  private List<TESTaskLogs> logs;

  public TESTask(@JsonProperty("name") String name,
                 @JsonProperty("project") String project,
                 @JsonProperty("description") String description,
                 @JsonProperty("inputs") List<TESTaskParameter> inputs,
                 @JsonProperty("outputs") List<TESTaskParameter> outputs,
                 @JsonProperty("resources") TESResources resources,
                 @JsonProperty("executors") List<TESExecutor> executors,
                 @JsonProperty("volumes") List<String> volumes,
                 @JsonProperty("tags") Map<String, String> tags) {
    this.name = name;
    this.description = description;
    this.project = project;
    this.inputs = inputs;
    this.outputs = outputs;
    this.resources = resources;
    this.executors = executors;
    this.volumes = volumes;
    this.tags = tags;
  }

  @JsonCreator
  public TESTask(@JsonProperty("id") String id,
                 @JsonProperty("state") TESState state,
                 @JsonProperty("name") String name,
                 @JsonProperty("project") String project,
                 @JsonProperty("description") String description,
                 @JsonProperty("inputs") List<TESTaskParameter> inputs,
                 @JsonProperty("outputs") List<TESTaskParameter> outputs,
                 @JsonProperty("resources") TESResources resources,
                 @JsonProperty("executors") List<TESExecutor> executors,
                 @JsonProperty("volumes") List<String> volumes,
                 @JsonProperty("tags") Map<String, String> tags,
                 @JsonProperty("logs") List<TESTaskLogs> logs) {
    this.id = id;
    this.state = state;
    this.name = name;
    this.description = description;
    this.project = project;
    this.inputs = inputs;
    this.outputs = outputs;
    this.resources = resources;
    this.executors = executors;
    this.volumes = volumes;
    this.tags = tags;
    this.logs = logs;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getProject() {
    return project;
  }

  public void setProject(String project) {
    this.project = project;
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

  public List<TESExecutor> getExecutors() {
    return executors;
  }

  public void setExecutors(List<TESExecutor> executors) {
    this.executors = executors;
  }

  public List<String> getVolumes() {
    return volumes;
  }

  public void setVolumes(List<String> volumes) {
    this.volumes = volumes;
  }

  public Map<String, String> getTags() {
    return tags;
  }

  public void setTags(Map<String, String> tags) {
    this.tags = tags;
  }

  public String getId() {
    return id;
  }

  public TESState getState() {
    return state;
  }

  public List<TESTaskLogs> getLogs() { return logs; }

  @Override
  public String toString() {
    return "TESTask [name=" + name + ", project=" + project + ", description=" + description + ", inputs=" + inputs + ", outputs=" + outputs
        + ", resources=" + resources + ", executors=" + executors + "]";
  }

}
