package org.rabix.bindings.cwl.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.cwl.bean.resource.CWLResource;
import org.rabix.bindings.cwl.helper.CWLBindingHelper;
import org.rabix.bindings.cwl.helper.CWLSchemaHelper;
import org.rabix.bindings.cwl.json.CWLResourcesDeserializer;
import org.rabix.bindings.cwl.json.CWLStepPortsDeserializer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CWLStep {

  @JsonProperty("id")
  private String id;

  @JsonProperty("run")
  private CWLJobApp app;

  @JsonProperty("in")
  @JsonDeserialize(using = CWLStepPortsDeserializer.class)
  private List<Map<String, Object>> inputs;

  @JsonProperty("out")
  @JsonDeserialize(using = CWLStepPortsDeserializer.class)
  private List<Map<String, Object>> outputs;

  @JsonProperty("scatter")
  private Object scatter;
  
  @JsonProperty("scatterMethod")
  private String scatterMethod;
  
  @JsonProperty("hints")
  @JsonDeserialize(using = CWLResourcesDeserializer.class)
  protected List<CWLResource> hints = new ArrayList<>();
  
  @JsonProperty("requirements")
  @JsonDeserialize(using = CWLResourcesDeserializer.class)
  protected List<CWLResource> requirements = new ArrayList<>();
  
  @JsonIgnore
  private CWLJob job;

  @JsonCreator
  public CWLStep(@JsonProperty("id") String id, @JsonProperty("run") CWLJobApp app,
      @JsonProperty("scatter") Object scatter, @JsonProperty("scatterMethod") String scatterMethod, @JsonProperty("linkMerge") String linkMerge,
      @JsonProperty("in") List<Map<String, Object>> inputs, @JsonProperty("out") List<Map<String, Object>> outputs) {
    this.id = id;
    this.app = app;
    this.scatter = scatter;
    this.scatterMethod = scatterMethod;
    this.inputs = inputs;
    this.outputs = outputs;
    this.job = constructJob();
  }

  /**
   * Construct {@link CWLJob}
   */
  @JsonIgnore
  private CWLJob constructJob() {
    if (id == null) {
      String portId = null;
      if (inputs != null && inputs.size() > 0) {
        portId = (String) inputs.get(0).get(CWLSchemaHelper.STEP_PORT_ID);
      } else if (outputs != null && outputs.size() > 0) {
        portId = (String) outputs.get(0).get(CWLSchemaHelper.STEP_PORT_ID);
      }
      if (portId.contains(CWLSchemaHelper.PORT_ID_SEPARATOR)) {
        id = portId.substring(1, portId.lastIndexOf(CWLSchemaHelper.PORT_ID_SEPARATOR));
      }
    }
    Map<String, Object> inputMap = constructJobPorts(inputs);
    Map<String, Object> outputMap = constructJobPorts(outputs);
    return new CWLJob(app, inputMap, outputMap, scatter, scatterMethod, id);
  }

  /**
   * Transform input/output lists to {@link CWLJob} input/output maps
   */
  private Map<String, Object> constructJobPorts(List<Map<String, Object>> portList) {
    if (portList == null) {
      return null;
    }
    Map<String, Object> portMap = new HashMap<>();
    for (Map<String, Object> port : portList) {
      String id = CWLSchemaHelper.getLastInputId(CWLBindingHelper.getId(port));
      id = CWLSchemaHelper.normalizeId(id);
      Object defaultValue = CWLBindingHelper.getDefault(port);
      Object valueFrom = CWLBindingHelper.getValueFrom(port);
      portMap.put(id, new CWLStepInputs(defaultValue, valueFrom));
    }
    return portMap;
  }

  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }

  public CWLJobApp getApp() {
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
  
  public List<CWLResource> getHints() {
    return hints;
  }

  public void setHints(List<CWLResource> hints) {
    this.hints = hints;
  }

  public List<CWLResource> getRequirements() {
    return requirements;
  }

  public void setRequirements(List<CWLResource> requirements) {
    this.requirements = requirements;
  }

  @JsonIgnore
  public CWLJob getJob() {
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
    CWLStep other = (CWLStep) obj;
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
    return "CWLStep [id=" + id + ", app=" + app + ", inputs=" + inputs + ", outputs=" + outputs + ", scatter="
        + scatter + ", scatterMethod=" + scatterMethod + ", job=" + job + "]";
  }

}
