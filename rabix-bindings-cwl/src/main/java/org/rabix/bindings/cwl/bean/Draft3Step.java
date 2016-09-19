package org.rabix.bindings.draft3.bean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.draft3.helper.Draft3BindingHelper;
import org.rabix.bindings.draft3.helper.Draft3SchemaHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Draft3Step {

  @JsonProperty("id")
  private String id;

  @JsonProperty("run")
  private Draft3JobApp app;

  @JsonProperty("inputs")
  private List<Map<String, Object>> inputs;

  @JsonProperty("outputs")
  private List<Map<String, Object>> outputs;

  @JsonProperty("scatter")
  private Object scatter;
  
  @JsonProperty("scatterMethod")
  private String scatterMethod;
  
  @JsonIgnore
  private Draft3Job job;

  @JsonCreator
  public Draft3Step(@JsonProperty("id") String id, @JsonProperty("run") Draft3JobApp app,
      @JsonProperty("scatter") Object scatter, @JsonProperty("scatterMethod") String scatterMethod, @JsonProperty("linkMerge") String linkMerge,
      @JsonProperty("inputs") List<Map<String, Object>> inputs, @JsonProperty("outputs") List<Map<String, Object>> outputs) {
    this.id = id;
    this.app = app;
    this.scatter = scatter;
    this.scatterMethod = scatterMethod;
    this.inputs = inputs;
    this.outputs = outputs;
    this.job = constructJob();
  }

  /**
   * Construct {@link Draft3Job}
   */
  @JsonIgnore
  private Draft3Job constructJob() {
    if (id == null) {
      String portId = null;
      if (inputs != null && inputs.size() > 0) {
        portId = (String) inputs.get(0).get(Draft3SchemaHelper.STEP_PORT_ID);
      } else if (outputs != null && outputs.size() > 0) {
        portId = (String) outputs.get(0).get(Draft3SchemaHelper.STEP_PORT_ID);
      }
      if (portId.contains(Draft3SchemaHelper.PORT_ID_SEPARATOR)) {
        id = portId.substring(1, portId.lastIndexOf(Draft3SchemaHelper.PORT_ID_SEPARATOR));
      }
    }
    Map<String, Object> inputMap = constructJobPorts(inputs);
    Map<String, Object> outputMap = constructJobPorts(outputs);
    return new Draft3Job(app, inputMap, outputMap, scatter, scatterMethod, id);
  }

  /**
   * Transform input/output lists to {@link Draft3Job} input/output maps
   */
  private Map<String, Object> constructJobPorts(List<Map<String, Object>> portList) {
    if (portList == null) {
      return null;
    }
    Map<String, Object> portMap = new HashMap<>();
    for (Map<String, Object> port : portList) {
      String id = Draft3SchemaHelper.getLastInputId(Draft3BindingHelper.getId(port));
      id = Draft3SchemaHelper.normalizeId(id);
      Object value = Draft3BindingHelper.getDefault(port);
      if (value != null) {
        portMap.put(id, value);
      }
    }
    return portMap;
  }

  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }

  public Draft3JobApp getApp() {
    return app;
  }

  public List<Map<String, Object>> getInputs() {
    return inputs;
  }

  public List<Map<String, Object>> getOutputs() {
    return outputs;
  }

  public Object getScatter() {
    return scatter;
  }
  
  public String getScatterMethod() {
    return scatterMethod;
  }

  @JsonIgnore
  public Draft3Job getJob() {
    return job;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((app == null) ? 0 : app.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((inputs == null) ? 0 : inputs.hashCode());
    result = prime * result + ((job == null) ? 0 : job.hashCode());
    result = prime * result + ((outputs == null) ? 0 : outputs.hashCode());
    result = prime * result + ((scatter == null) ? 0 : scatter.hashCode());
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
    Draft3Step other = (Draft3Step) obj;
    if (app == null) {
      if (other.app != null)
        return false;
    } else if (!app.equals(other.app))
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
    if (job == null) {
      if (other.job != null)
        return false;
    } else if (!job.equals(other.job))
      return false;
    if (outputs == null) {
      if (other.outputs != null)
        return false;
    } else if (!outputs.equals(other.outputs))
      return false;
    if (scatter == null) {
      if (other.scatter != null)
        return false;
    } else if (!scatter.equals(other.scatter))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Draft3Step [id=" + id + ", app=" + app + ", inputs=" + inputs + ", outputs=" + outputs + ", scatter="
        + scatter + ", scatterMethod=" + scatterMethod + ", job=" + job + "]";
  }

}
