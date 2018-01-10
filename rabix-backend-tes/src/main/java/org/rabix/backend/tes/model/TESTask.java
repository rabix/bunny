package org.rabix.backend.tes.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class TESTask {

  @JsonProperty("id")
  private String id;
  @JsonProperty("state")
  private TESState state;
  @JsonProperty("name")
  private String name;
  @JsonProperty("description")
  private String description;
  @JsonProperty("inputs")
  private List<TESInput> inputs;
  @JsonProperty("outputs")
  private List<TESOutput> outputs;
  @JsonProperty("resources")
  private TESResources resources;
  @JsonProperty("executors")
  private List<TESExecutor> executors;
  @JsonProperty("creation_time")
  private String createTime;
  @JsonProperty("volumes")
  private List<String> volumes;
  @JsonProperty("tags")
  private Map<String, String> tags;
  @JsonProperty("logs")
  private List<TESTaskLogs> logs;

  public TESTask(@JsonProperty("name") String name,
                 @JsonProperty("description") String description,
                 @JsonProperty("inputs") List<TESInput> inputs,
                 @JsonProperty("outputs") List<TESOutput> outputs,
                 @JsonProperty("resources") TESResources resources,
                 @JsonProperty("executors") List<TESExecutor> executors,
                 @JsonProperty("volumes") List<String> volumes,
                 @JsonProperty("tags") Map<String, String> tags,
                 @JsonProperty("creation_time") String createTime) {
    this.name = name;
    this.description = description;
    this.inputs = inputs;
    this.outputs = outputs;
    this.resources = resources;
    this.executors = executors;
    this.volumes = volumes;
    this.tags = tags;
    this.createTime = createTime;
  }

  @JsonCreator
  public TESTask(@JsonProperty("id") String id,
                 @JsonProperty("state") TESState state,
                 @JsonProperty("name") String name,
                 @JsonProperty("description") String description,
                 @JsonProperty("inputs") List<TESInput> inputs,
                 @JsonProperty("outputs") List<TESOutput> outputs,
                 @JsonProperty("resources") TESResources resources,
                 @JsonProperty("executors") List<TESExecutor> executors,
                 @JsonProperty("volumes") List<String> volumes,
                 @JsonProperty("tags") Map<String, String> tags,
                 @JsonProperty("logs") List<TESTaskLogs> logs,
                 @JsonProperty("creation_time") String createTime) {
    this.id = id;
    this.state = state;
    this.name = name;
    this.description = description;
    this.inputs = inputs;
    this.outputs = outputs;
    this.resources = resources;
    this.executors = executors;
    this.volumes = volumes;
    this.createTime = createTime;
    this.tags = tags;
    this.logs = logs;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<TESInput> getInputs() {
    return inputs;
  }

  public void setInputs(List<TESInput> inputs) {
    this.inputs = inputs;
  }

  public List<TESOutput> getOutputs() {
    return outputs;
  }

  public void setOutputs(List<TESOutput> outputs) {
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

  public String getCreateTime() {
    return createTime;
  }
  
  public List<TESTaskLogs> getLogs() { return logs; }

  @Override
  public String toString() {
    return "TESTask [name=" + name + ", description=" + description + ", inputs=" + inputs + ", outputs=" + outputs
        + ", resources=" + resources + ", executors=" + executors + "]";
  }

}
