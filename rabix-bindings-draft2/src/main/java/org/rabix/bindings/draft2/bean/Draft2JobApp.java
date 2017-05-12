package org.rabix.bindings.draft2.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.rabix.bindings.draft2.bean.resource.Draft2CpuResource;
import org.rabix.bindings.draft2.bean.resource.Draft2MemoryResource;
import org.rabix.bindings.draft2.bean.resource.Draft2Resource;
import org.rabix.bindings.draft2.bean.resource.Draft2ResourceType;
import org.rabix.bindings.draft2.bean.resource.requirement.Draft2CreateFileRequirement;
import org.rabix.bindings.draft2.bean.resource.requirement.Draft2DockerResource;
import org.rabix.bindings.draft2.bean.resource.requirement.Draft2EnvVarRequirement;
import org.rabix.bindings.draft2.bean.resource.requirement.Draft2ExpressionEngineRequirement;
import org.rabix.bindings.draft2.bean.resource.requirement.Draft2IORequirement;
import org.rabix.bindings.draft2.bean.resource.requirement.Draft2SchemaDefRequirement;
import org.rabix.bindings.draft2.json.Draft2JobAppDeserializer;
import org.rabix.bindings.model.Application;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.ValidationReport;
import org.rabix.common.json.BeanSerializer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using=Draft2JobAppDeserializer.class)
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Draft2JobApp extends Application {

  @JsonProperty("contributor")
  protected List<String> contributor = new ArrayList<>();
  @JsonProperty("owner")
  protected List<String> owner = new ArrayList<>();
  @JsonProperty("cwlVersion")
  protected String cwlVersion;

  @JsonProperty("inputs")
  protected List<Draft2InputPort> inputs = new ArrayList<>();
  @JsonProperty("outputs")
  protected List<Draft2OutputPort> outputs = new ArrayList<>();

  @JsonProperty("hints")
  protected List<Draft2Resource> hints = new ArrayList<>();
  @JsonProperty("requirements")
  protected List<Draft2Resource> requirements = new ArrayList<>();
  
  @JsonProperty("successCodes")
  protected List<Integer> successCodes = new ArrayList<>();

  @JsonIgnore
  public String getId() {
    return (String) getProperty("id");
  }
  
  public List<Integer> getSuccessCodes() {
    return successCodes;
  }

  @JsonIgnore
  public List<Map<String, Object>> getSchemaDefs() {
    Draft2SchemaDefRequirement schemaDefRequirement = lookForResource(Draft2ResourceType.SCHEMA_DEF_REQUIREMENT, Draft2SchemaDefRequirement.class);
    return schemaDefRequirement != null ? schemaDefRequirement.getSchemaDefs() : null;
  }

  @JsonIgnore
  public Draft2DockerResource getContainerResource() throws IllegalArgumentException {
    Draft2DockerResource dockerResource = lookForResource(Draft2ResourceType.DOCKER_RESOURCE, Draft2DockerResource.class);
    if (dockerResource != null) {
      validateDockerRequirement(dockerResource);
    }
    return dockerResource;
  }

  /**
   * Do some basic validation
   */
  private void validateDockerRequirement(Draft2DockerResource requirement) {
    String imageId = requirement.getImageId();
    String dockerPull = requirement.getDockerPull();

    if (StringUtils.isEmpty(dockerPull) && StringUtils.isEmpty(imageId)) {
      throw new IllegalArgumentException("Docker requirements are empty.");
    }
  }

  @JsonIgnore
  public Draft2CpuResource getCpuRequirement() {
    return lookForResource(Draft2ResourceType.CPU_RESOURCE, Draft2CpuResource.class);
  }

  @JsonIgnore
  public Draft2MemoryResource getMemoryRequirement() {
    return lookForResource(Draft2ResourceType.MEMORY_RESOURCE, Draft2MemoryResource.class);
  }

  @JsonIgnore
  public Draft2IORequirement getIORequirement() {
    return lookForResource(Draft2ResourceType.IO_REQUIREMENT, Draft2IORequirement.class);
  }

  @JsonIgnore
  public List<Draft2ExpressionEngineRequirement> getExpressionEngineRequirements() {
    return lookForResources(Draft2ResourceType.EXPRESSION_ENGINE_REQUIREMENT, Draft2ExpressionEngineRequirement.class);
  }
  
  @JsonIgnore
  public Draft2EnvVarRequirement getEnvVarRequirement() {
    return lookForResource(Draft2ResourceType.ENV_VAR_REQUIREMENT, Draft2EnvVarRequirement.class);
  }
  
  public <T extends Draft2Resource> T getRequirement(Draft2ResourceType type, Class<T> clazz) {
    List<T> resources = getRequirements(type, clazz);
    if (resources != null && !resources.isEmpty()) {
      return resources.get(0);
    }
    return null;
  }
  
  public <T extends Draft2Resource> T getHint(Draft2ResourceType type, Class<T> clazz) {
    List<T> resources = getHints(type, clazz);
    if(resources != null && !resources.isEmpty()) {
      return resources.get(0);
    }
    return null;
  }
  
  public <T> void setHint(Draft2Resource resource) {
    for (Draft2Resource hint : hints) {
    if (resource.getTypeEnum().equals(hint.getTypeEnum())) {
      hints.remove(hint);
      hints.add(resource);
      break;
      }
    }
  }

  @JsonIgnore
  public Draft2CreateFileRequirement getCreateFileRequirement() {
    return lookForResource(Draft2ResourceType.CREATE_FILE_REQUIREMENT, Draft2CreateFileRequirement.class);
  }

  /**
   * Find one resource by type 
   */
  private <T extends Draft2Resource> T lookForResource(Draft2ResourceType type, Class<T> clazz) {
    List<T> resources = lookForResources(type, clazz);
    return resources != null && resources.size() > 0 ? resources.get(0) : null;
  }
  
  /**
   * Find all resources by type 
   */
  private <T extends Draft2Resource> List<T> lookForResources(Draft2ResourceType type, Class<T> clazz) {
    List<T> resources = getRequirements(type, clazz);
    if (resources == null || resources.size() == 0) {
      resources = getHints(type, clazz);
    }
    return resources;
  }
  
  @JsonIgnore
  private <T extends Draft2Resource> List<T> getRequirements(Draft2ResourceType type, Class<T> clazz) {
    if (requirements == null) {
      return null;
    }
    List<T> result = new ArrayList<>();
    for (Draft2Resource requirement : requirements) {
      if (type.equals(requirement.getTypeEnum())) {
        result.add(clazz.cast(requirement));
      }
    }
    return result;
  }

  @JsonIgnore
  private <T extends Draft2Resource> List<T> getHints(Draft2ResourceType type, Class<T> clazz) {
    if (hints == null) {
      return null;
    }
    List<T> result = new ArrayList<>();
    for (Draft2Resource hint : hints) {
      if (type.equals(hint.getTypeEnum())) {
        result.add(clazz.cast(hint));
      }
    }
    return result;
  }

  public ApplicationPort getPort(String id, Class<? extends ApplicationPort> clazz) {
    if (Draft2InputPort.class.equals(clazz)) {
      return getInput(id);
    }
    if (Draft2OutputPort.class.equals(clazz)) {
      return getOutput(id);
    }
    return null;
  }

  @JsonIgnore
  public Draft2InputPort getInput(String id) {
    if (getInputs() == null) {
      return null;
    }
    for (Draft2InputPort input : getInputs()) {
      if (input.getId().substring(1).equals(id) || input.getId().equals(id)) {
        return input;
      }
    }
    return null;
  }

  @JsonIgnore
  public Draft2OutputPort getOutput(String id) {
    if (getOutputs() == null) {
      return null;
    }
    for (Draft2OutputPort output : getOutputs()) {
      if (output.getId().substring(1).equals(id) || output.getId().equals(id)) {
        return output;
      }
    }
    return null;
  }

  @Override
  public ValidationReport validate() {
    return new ValidationReport();
  }

  @Override
  @JsonIgnore
  public String getVersion() {
    return getCwlVersion();
  }

  @JsonIgnore
  public String getCwlVersion() {
    return cwlVersion;
  }
  
  public void setCwlVersion(String cwlVersion) {
    this.cwlVersion = cwlVersion;
  }

  @JsonIgnore
  public String getContext() {
    return (String) getProperty("@context");
  }

  @JsonIgnore
  public String getDescription() {
    return (String) getProperty("description");
  }

  public List<Draft2InputPort> getInputs() {
    return inputs;
  }

  public List<Draft2OutputPort> getOutputs() {
    return outputs;
  }

  public List<Draft2Resource> getRequirements() {
    return requirements;
  }

  public List<Draft2Resource> getHints() {
    return hints;
  }

  @JsonIgnore
  public String getLabel() {
    return (String) getProperty("label");
  }

  public List<String> getContributor() {
    return contributor;
  }

  public List<String> getOwner() {
    return owner;
  }

  @JsonIgnore
  public boolean isWorkflow() {
    return Draft2JobAppType.WORKFLOW.equals(getType());
  }

  @JsonIgnore
  public boolean isCommandLineTool() {
    return Draft2JobAppType.COMMAND_LINE_TOOL.equals(getType());
  }
  
  @JsonIgnore
  public boolean isEmbedded() {
    return Draft2JobAppType.EMBEDDED.equals(getType());
  }
  
  @JsonIgnore
  public boolean isExpressionTool() {
    return Draft2JobAppType.EXPRESSION_TOOL.equals(getType());
  }
  
  @Override
  public String serialize() {
    return BeanSerializer.serializePartial(this);
  }
  
  public abstract Draft2JobAppType getType();

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((getContext() == null) ? 0 : getContext().hashCode());
    result = prime * result + ((getDescription() == null) ? 0 : getDescription().hashCode());
    result = prime * result + ((hints == null) ? 0 : hints.hashCode());
    result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
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
    Draft2JobApp other = (Draft2JobApp) obj;
    if (getContext() == null) {
      if (other.getContext() != null)
        return false;
    } else if (!getContext().equals(other.getContext()))
      return false;
    if (getDescription()== null) {
      if (other.getDescription() != null)
        return false;
    } else if (!getDescription().equals(other.getDescription()))
      return false;
    if (hints == null) {
      if (other.hints != null)
        return false;
    } else if (!hints.equals(other.hints))
      return false;
    if (getId() == null) {
      if (other.getId() != null)
        return false;
    } else if (!getId().equals(other.getId()))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "JobApp [id=" + getId() + ", context=" + getContext() + ", description=" + getDescription() + ", label=" + getLabel()
        + ", contributor=" + contributor + ", owner=" + owner + ", hints=" + hints + ", inputs=" + inputs + ", outputs="
        + outputs + ", requirements=" + requirements + "]";
  }

}
