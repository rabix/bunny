package org.rabix.bindings.sb.bean;

import java.util.Map;

import org.rabix.bindings.sb.SBJobProcessor;
import org.rabix.bindings.sb.bean.resource.SBCpuResource;
import org.rabix.bindings.sb.bean.resource.SBMemoryResource;
import org.rabix.bindings.sb.bean.resource.requirement.SBIORequirement;
import org.rabix.bindings.sb.expression.SBExpressionException;
import org.rabix.bindings.sb.helper.SBSchemaHelper;
import org.rabix.common.json.BeanPropertyView;
import org.rabix.common.json.processor.BeanProcessorClass;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@BeanProcessorClass(name = SBJobProcessor.class)
public final class SBJob {

  @JsonProperty("id")
  private String id;

  @JsonProperty("app")
  private SBJobApp app;

  @JsonProperty("inputs")
  private Map<String, Object> inputs;

  @JsonProperty("outputs")
  private Map<String, Object> outputs;

  @JsonProperty("scatter")
  @JsonView(BeanPropertyView.Full.class)
  private Object scatter;
  
  @JsonProperty("scatterMethod")
  @JsonView(BeanPropertyView.Full.class)
  private String scatterMethod;
  
  @JsonProperty("allocatedResources")
  private SBResources resources;

  @JsonCreator
  public SBJob(@JsonProperty("app") SBJobApp app,
      @JsonProperty("inputs") Map<String, Object> inputs,
      @JsonProperty("outputs") Map<String, Object> outputs,
      @JsonProperty("allocatedResources") SBResources resources,
      @JsonProperty("id") String id, @JsonProperty("scatter") Object scatter, 
      @JsonProperty("scatterMethod") String scatterMethod) {
    this.id = id;
    this.app = app;
    this.inputs = inputs;
    this.outputs = outputs;
    this.resources = resources;
    this.scatter = scatter;
    this.scatterMethod = scatterMethod;
    processPortDefaults();
  }
  
  private void processPortDefaults() {
    if (inputs == null) {
      return;
    }
    for (SBInputPort inputPort : app.getInputs()) {
      String normalizedId = SBSchemaHelper.normalizeId(inputPort.getId());
      if (!inputs.containsKey(normalizedId) && inputPort.getDefaultValue() != null) {
        inputs.put(normalizedId, inputPort.getDefaultValue());
      }
    }
  }

  public SBJob(SBJobApp app, Map<String, Object> inputs, Map<String, Object> outputs, Object scatter, String scatterMethod, String id) {
    this.id = id;
    this.app = app;
    this.scatter = scatter;
    this.inputs = inputs;
    this.outputs = outputs;
    this.scatterMethod = scatterMethod;
    processPortDefaults();
  }
  
  public SBJob(String id, SBJobApp app, Map<String, Object> inputs, Map<String, Object> outputs) {
    this.id = id;
    this.app = app;
    this.inputs = inputs;
    this.outputs = outputs;
    processPortDefaults();
  }

  @JsonIgnore
  public Integer getCPU() throws SBExpressionException {
    SBCpuResource cpuRequirement = app.getCpuRequirement();
    if (cpuRequirement == null || cpuRequirement.getCpu(this) == 0) {
      return null;
    }
    return cpuRequirement.getCpu(this);
  }

  @JsonIgnore
  public Integer getMemory() throws SBExpressionException {
    SBMemoryResource memoryRequirement = app.getMemoryRequirement();
    if (memoryRequirement == null || memoryRequirement.getMemory(this) == 0) {
      return null;
    }
    return memoryRequirement.getMemory(this);
  }

  @JsonIgnore
  public Integer getIO() throws SBExpressionException {
    SBIORequirement ioRequirement = app.getIORequirement();
    return ioRequirement != null ? ioRequirement.getIO(this) : SBIORequirement.DEFAULT_VALUE;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
  
  public void setInputs(Map<String, Object> inputs) {
    this.inputs = inputs;
  }

  public SBJobApp getApp() {
    return app;
  }

  public Map<String, Object> getInputs() {
    return inputs;
  }

  public Map<String, Object> getOutputs() {
    return outputs;
  }

  public void setResources(SBResources resources) {
    this.resources = resources;
  }
  
  public SBResources getResources() {
    return resources;
  }

  public Object getScatter() {
    return scatter;
  }
  
  public String getScatterMethod() {
    return scatterMethod;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SBJob other = (SBJob) obj;
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
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((app == null) ? 0 : app.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "Job [id=" + id + ", app=" + app + ", inputs=" + inputs + ", outputs=" + outputs + ", scatter=" + scatter + ", resources=" + resources + "]";
  }

}
