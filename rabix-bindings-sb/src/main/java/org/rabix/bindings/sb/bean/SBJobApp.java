package org.rabix.bindings.sb.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.rabix.bindings.model.Application;
import org.rabix.bindings.model.ApplicationPort;
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
import org.rabix.common.json.BeanSerializer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "class", defaultImpl = SBEmbeddedApp.class)
@JsonSubTypes({ 
	@Type(value = SBCommandLineTool.class, name = "CommandLineTool"),
	@Type(value = SBExpressionTool.class, name = "ExpressionTool"),
    @Type(value = SBWorkflow.class, name = "Workflow"),
    @Type(value = SBPythonTool.class, name = "PythonTool")})
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class SBJobApp extends Application {

  @JsonProperty("id")
  protected String id;
  @JsonProperty("@context")
  protected String context;
  @JsonProperty("cwlVersion")
  protected String cwlVersion;
  @JsonProperty("description")
  protected String description;
  @JsonProperty("label")
  protected String label;
  @JsonProperty("contributor")
  protected List<String> contributor = new ArrayList<>();
  @JsonProperty("owner")
  protected List<String> owner = new ArrayList<>();

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

  public String getId() {
    return id;
  }
  
  public List<Integer> getSuccessCodes() {
    return successCodes;
  }
  
  public String getCwlVersion() {
    return cwlVersion;
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

  public String getContext() {
    return context;
  }

  public String getDescription() {
    return description;
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

  public String getLabel() {
    return label;
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
    return BeanSerializer.serializeFull(this);
  }
  
  public abstract SBJobAppType getType();

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((context == null) ? 0 : context.hashCode());
    result = prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + ((hints == null) ? 0 : hints.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
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
    if (context == null) {
      if (other.context != null)
        return false;
    } else if (!context.equals(other.context))
      return false;
    if (description == null) {
      if (other.description != null)
        return false;
    } else if (!description.equals(other.description))
      return false;
    if (hints == null) {
      if (other.hints != null)
        return false;
    } else if (!hints.equals(other.hints))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "JobApp [id=" + id + ", context=" + context + ", description=" + description + ", label=" + label
        + ", contributor=" + contributor + ", owner=" + owner + ", hints=" + hints + ", inputs=" + inputs + ", outputs="
        + outputs + ", requirements=" + requirements + "]";
  }

}
