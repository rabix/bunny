package org.rabix.bindings.model;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.rabix.common.helper.CloneHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Job {

  public static enum JobStatus {
    PENDING,
    READY,
    STARTED,
    ABORTED,
    FAILED,
    COMPLETED,
    RUNNING
  }

  @JsonProperty("id")
  private final String id;
  @JsonProperty("parentId")
  private final String parentId;
  @JsonProperty("rootId")
  private final String rootId;
  @JsonProperty("name")
  private final String name;
  @JsonProperty("app")
  private final String app;
  @JsonProperty("status")
  private final JobStatus status;
  @JsonProperty("message")
  private final String message;
  @JsonProperty("config")
  private final Map<String, Object> config;
  @JsonProperty("inputs")
  private final Map<String, Object> inputs;
  @JsonProperty("outputs")
  private final Map<String, Object> outputs;
  @JsonProperty("resources")
  private final Resources resources;
  
  @JsonProperty("visiblePorts")
  private Set<String> visiblePorts;
  
  public Job(String app, Map<String, Object> inputs) {
    this(null, null, generateId(), null, app, JobStatus.PENDING, null, inputs, null, null, null, null);
  }
  
  @JsonCreator
  public Job(@JsonProperty("id") String id,
      @JsonProperty("parentId") String parentId,
      @JsonProperty("rootId") String rootId,
      @JsonProperty("name") String name,
      @JsonProperty("app") String app, 
      @JsonProperty("status") JobStatus status,
      @JsonProperty("message") String message,
      @JsonProperty("inputs") Map<String, Object> inputs, 
      @JsonProperty("outputs") Map<String, Object> otputs,
      @JsonProperty("config") Map<String, Object> config,
      @JsonProperty("resources") Resources resources,
      @JsonProperty("visiblePorts") Set<String> visiblePorts) {
    this.id = id;
    this.parentId = parentId;
    this.rootId = rootId;
    this.name = name;
    this.app = app;
    this.status = status;
    this.message = message;
    this.inputs = inputs;
    this.outputs = otputs;
    this.resources = resources;
    this.config = config;
    this.visiblePorts = visiblePorts;
  }
  
  public static String generateId() {
    return UUID.randomUUID().toString();
  }
  
  public static Job cloneWithId(Job job, String id) {
    return new Job(id, job.parentId, job.rootId, job.name, job.app, job.status, job.message, job.inputs, job.outputs, job.config, job.resources, job.visiblePorts);
  }

  public static Job cloneWithIds(Job job, String id, String rootId) {
    return new Job(id, job.parentId, rootId, job.name, job.app, job.status, job.message, job.inputs, job.outputs, job.config, job.resources, job.visiblePorts);
  }
  
  public static Job cloneWithRootId(Job job, String rootId) {
    return new Job(job.getId(), job.parentId, rootId, job.name, job.app, job.status, job.message, job.inputs, job.outputs, job.config, job.resources, job.visiblePorts);
  }
  
  public static Job cloneWithConfig(Job job, Map<String, Object> config) {
    return new Job(job.id, job.parentId, job.rootId, job.name, job.app, job.status, job.message, job.inputs, job.outputs, config, job.resources, job.visiblePorts);
  }
  
  public static Job cloneWithStatus(Job job, JobStatus status) {
    return new Job(job.id, job.parentId, job.rootId, job.name, job.app, status, job.message, job.inputs, job.outputs, job.config, job.resources, job.visiblePorts);
  }
  
  public static Job cloneWithMessage(Job job, String message) {
    return new Job(job.id, job.parentId, job.rootId, job.name, job.app, job.status, message, job.inputs, job.outputs, job.config, job.resources, job.visiblePorts);
  }
  
  public static Job cloneWithInputs(Job job, Map<String, Object> inputs) {
    return new Job(job.id, job.parentId, job.rootId, job.name, job.app, job.status, job.message, inputs, job.outputs, job.config, job.resources, job.visiblePorts);
  }
  
  public static Job cloneWithOutputs(Job job, Map<String, Object> outputs) {
    return new Job(job.id, job.parentId, job.rootId, job.name, job.app, job.status, job.message, job.inputs, outputs, job.config, job.resources, job.visiblePorts);
  }
  
  public static Job cloneWithResources(Job job, Resources resources) {
    return new Job(job.id, job.parentId, job.rootId, job.name, job.app, job.status, job.message, job.inputs, job.outputs, job.config, resources, job.visiblePorts);
  }
  
  public static boolean isFinished(Job job) {
    return job.getStatus().equals(JobStatus.COMPLETED) 
        || job.getStatus().equals(JobStatus.ABORTED)
        || job.getStatus().equals(JobStatus.FAILED);
  }
  
  @JsonIgnore
  public boolean isRoot() {
    if (id == null) {
      return false;
    }
    return id.equals(rootId);
  }
  
  public String getId() {
    return id;
  }
  
  public String getParentId() {
    return parentId;
  }
  
  public String getRootId() {
    return rootId;
  }
  
  public String getName() {
    return name;
  }
  
  public String getApp() {
    return app;
  }
  
  public Resources getResources() {
    return resources;
  }
  
  public Set<String> getVisiblePorts() {
    return visiblePorts;
  }
  
  @SuppressWarnings("unchecked")
  public Map<String, Object> getInputs() {
    try {
      return (Map<String, Object>) CloneHelper.deepCopy(inputs);
    } catch (Exception e) {
      throw new RuntimeException("Failed to clone inputs", e);
    }
  }
  
  @SuppressWarnings("unchecked")
  public Map<String, Object> getOutputs() {
    try {
      return (Map<String, Object>) CloneHelper.deepCopy(outputs);
    } catch (Exception e) {
      throw new RuntimeException("Failed to clone outputs", e);
    }
  }
  
  public JobStatus getStatus() {
    return status;
  }
  
  public Map<String, Object> getConfig() {
    return config;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((app == null) ? 0 : app.hashCode());
    result = prime * result + ((config == null) ? 0 : config.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((inputs == null) ? 0 : inputs.hashCode());
    result = prime * result + ((message == null) ? 0 : message.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((outputs == null) ? 0 : outputs.hashCode());
    result = prime * result + ((parentId == null) ? 0 : parentId.hashCode());
    result = prime * result + ((resources == null) ? 0 : resources.hashCode());
    result = prime * result + ((rootId == null) ? 0 : rootId.hashCode());
    result = prime * result + ((status == null) ? 0 : status.hashCode());
    result = prime * result + ((visiblePorts == null) ? 0 : visiblePorts.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Job other = (Job) obj;
    if (app == null) {
      if (other.app != null)
        return false;
    } else if (!app.equals(other.app))
      return false;
    if (config == null) {
      if (other.config != null)
        return false;
    } else if (!config.equals(other.config))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (inputs == null) {
      if (other.inputs != null)
        return false;
    } else if (!inputs.equals(other.inputs))
      return false;
    if (message == null) {
      if (other.message != null)
        return false;
    } else if (!message.equals(other.message))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (outputs == null) {
      if (other.outputs != null)
        return false;
    } else if (!outputs.equals(other.outputs))
      return false;
    if (parentId == null) {
      if (other.parentId != null)
        return false;
    } else if (!parentId.equals(other.parentId))
      return false;
    if (resources == null) {
      if (other.resources != null)
        return false;
    } else if (!resources.equals(other.resources))
      return false;
    if (rootId == null) {
      if (other.rootId != null)
        return false;
    } else if (!rootId.equals(other.rootId))
      return false;
    if (status != other.status)
      return false;
    if (visiblePorts == null) {
      if (other.visiblePorts != null)
        return false;
    } else if (!visiblePorts.equals(other.visiblePorts))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Job [id=" + id + ", parentId=" + parentId + ", rootId=" + rootId + ", name=" + name + ", status=" + status + ", message=" + message + ", config=" + config + ", inputs=" + inputs + ", outputs=" + outputs + "]";
  }

}
