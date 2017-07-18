package org.rabix.bindings.sb.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.rabix.bindings.model.Application;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.ValidationReport;
import org.rabix.bindings.sb.bean.resource.SBCpuResource;
import org.rabix.bindings.sb.bean.resource.SBMemoryResource;
import org.rabix.bindings.sb.bean.resource.SBResource;
import org.rabix.bindings.sb.bean.resource.SBResourceType;
import org.rabix.bindings.sb.bean.resource.requirement.SBCreateFileRequirement;
import org.rabix.bindings.sb.bean.resource.requirement.SBDockerResource;
import org.rabix.bindings.sb.bean.resource.requirement.SBEnvVarRequirement;
import org.rabix.bindings.sb.bean.resource.requirement.SBExpressionEngineRequirement;
import org.rabix.bindings.sb.bean.resource.requirement.SBIORequirement;
import org.rabix.bindings.sb.bean.resource.requirement.SBSchemaDefRequirement;
import org.rabix.bindings.sb.json.SBJobAppDeserializer;
import org.rabix.common.json.BeanSerializer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;


@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using=SBJobAppDeserializer.class)
public abstract class SBJobApp extends Application {

  @JsonProperty("contributor")
  protected List<String> contributor = new ArrayList<>();
  @JsonProperty("owner")
  protected List<String> owner = new ArrayList<>();
  @JsonProperty("cwlVersion")
  protected String cwlVersion;

  @JsonProperty("inputs")
  protected List<SBInputPort> inputs = new ArrayList<>();
  @JsonProperty("outputs")
  protected List<SBOutputPort> outputs = new ArrayList<>();

  @JsonProperty("hints")
  protected List<SBResource> hints = new ArrayList<>();
  @JsonProperty("requirements")
  protected List<SBResource> requirements = new ArrayList<>();
  
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
  public String getCwlVersion() {
    return cwlVersion;
  }
  
  public void setCwlVersion(String cwlVersion) {
    this.cwlVersion = cwlVersion;
  }
  
  @Override
  @JsonIgnore
  public String getVersion() {
    return getCwlVersion();
  }

  @JsonIgnore
  public List<Map<String, Object>> getSchemaDefs() {
    SBSchemaDefRequirement schemaDefRequirement = lookForResource(SBResourceType.SCHEMA_DEF_REQUIREMENT, SBSchemaDefRequirement.class);
    return schemaDefRequirement != null ? schemaDefRequirement.getSchemaDefs() : null;
  }

  @JsonIgnore
  public SBDockerResource getContainerResource() throws IllegalArgumentException {
    SBDockerResource dockerResource = lookForResource(SBResourceType.DOCKER_RESOURCE, SBDockerResource.class);
    if (dockerResource != null) {
      validateDockerRequirement(dockerResource);
    }
    return dockerResource;
  }

  /**
   * Do some basic validation
   */
  private void validateDockerRequirement(SBDockerResource requirement) {
    String imageId = requirement.getImageId();
    String dockerPull = requirement.getDockerPull();

    if (StringUtils.isEmpty(dockerPull) && StringUtils.isEmpty(imageId)) {
      throw new IllegalArgumentException("Docker requirements are empty.");
    }
  }

  @JsonIgnore
  public SBCpuResource getCpuRequirement() {
    return lookForResource(SBResourceType.CPU_RESOURCE, SBCpuResource.class);
  }

  @JsonIgnore
  public SBMemoryResource getMemoryRequirement() {
    return lookForResource(SBResourceType.MEMORY_RESOURCE, SBMemoryResource.class);
  }

  @JsonIgnore
  public SBIORequirement getIORequirement() {
    return lookForResource(SBResourceType.IO_REQUIREMENT, SBIORequirement.class);
  }

  @JsonIgnore
  public List<SBExpressionEngineRequirement> getExpressionEngineRequirements() {
    return lookForResources(SBResourceType.EXPRESSION_ENGINE_REQUIREMENT, SBExpressionEngineRequirement.class);
  }
  
  @JsonIgnore
  public SBEnvVarRequirement getEnvVarRequirement() {
    return lookForResource(SBResourceType.ENV_VAR_REQUIREMENT, SBEnvVarRequirement.class);
  }

  @JsonIgnore
  public SBCreateFileRequirement getCreateFileRequirement() {
    return lookForResource(SBResourceType.CREATE_FILE_REQUIREMENT, SBCreateFileRequirement.class);
  }

  /**
   * Find one resource by type 
   */
  private <T extends SBResource> T lookForResource(SBResourceType type, Class<T> clazz) {
    List<T> resources = lookForResources(type, clazz);
    return resources != null && resources.size() > 0 ? resources.get(0) : null;
  }
  
  /**
   * Find all resources by type 
   */
  private <T extends SBResource> List<T> lookForResources(SBResourceType type, Class<T> clazz) {
    List<T> resources = getRequirements(type, clazz);
    if (resources == null || resources.size() == 0) {
      resources = getHints(type, clazz);
    }
    return resources;
  }
  
  @JsonIgnore
  private <T extends SBResource> List<T> getRequirements(SBResourceType type, Class<T> clazz) {
    if (requirements == null) {
      return null;
    }
    List<T> result = new ArrayList<>();
    for (SBResource requirement : requirements) {
      if (type.equals(requirement.getTypeEnum())) {
        result.add(clazz.cast(requirement));
      }
    }
    return result;
  }

  @JsonIgnore
  private <T extends SBResource> List<T> getHints(SBResourceType type, Class<T> clazz) {
    if (hints == null) {
      return null;
    }
    List<T> result = new ArrayList<>();
    for (SBResource hint : hints) {
      if (type.equals(hint.getTypeEnum())) {
        result.add(clazz.cast(hint));
      }
    }
    return result;
  }

  public ApplicationPort getPort(String id, Class<? extends ApplicationPort> clazz) {
    if (SBInputPort.class.equals(clazz)) {
      return getInput(id);
    }
    if (SBOutputPort.class.equals(clazz)) {
      return getOutput(id);
    }
    return null;
  }

  @JsonIgnore
  public SBInputPort getInput(String id) {
    if (getInputs() == null) {
      return null;
    }
    for (SBInputPort input : getInputs()) {
      if (input.getId().substring(1).equals(id) || input.getId().equals(id)) {
        return input;
      }
    }
    return null;
  }

  @JsonIgnore
  public SBOutputPort getOutput(String id) {
    if (getOutputs() == null) {
      return null;
    }
    for (SBOutputPort output : getOutputs()) {
      if (output.getId().substring(1).equals(id) || output.getId().equals(id)) {
        return output;
      }
    }
    return null;
  }

  @JsonIgnore
  public String getContext() {
    return (String) getProperty("@context");
  }

  @JsonIgnore
  public String getDescription() {
    return (String) getProperty("description");
  }

  @Override
  public ValidationReport validate() {
    return new ValidationReport();
  }

  public List<SBInputPort> getInputs() {
    return inputs;
  }

  public List<SBOutputPort> getOutputs() {
    return outputs;
  }

  public List<SBResource> getRequirements() {
    return requirements;
  }

  public List<SBResource> getHints() {
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
    return SBJobAppType.WORKFLOW.equals(getType());
  }

  @JsonIgnore
  public boolean isCommandLineTool() {
    return SBJobAppType.COMMAND_LINE_TOOL.equals(getType());
  }
  
  @JsonIgnore
  public boolean isEmbedded() {
    return SBJobAppType.EMBEDDED.equals(getType());
  }
  
  @JsonIgnore
  public boolean isExpressionTool() {
    return SBJobAppType.EXPRESSION_TOOL.equals(getType());
  }
  
  @Override
  public String serialize() {
    return BeanSerializer.serializePartial(this);
  }
  
  public abstract SBJobAppType getType();

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
    SBJobApp other = (SBJobApp) obj;
    if (getContext() == null) {
      if (other.getContext() != null)
        return false;
    } else if (!getContext().equals(other.getContext()))
      return false;
    if (getDescription() == null) {
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
