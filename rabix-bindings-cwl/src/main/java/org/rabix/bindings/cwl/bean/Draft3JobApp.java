package org.rabix.bindings.draft3.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.rabix.bindings.draft3.bean.resource.Draft3Resource;
import org.rabix.bindings.draft3.bean.resource.Draft3ResourceType;
import org.rabix.bindings.draft3.bean.resource.requirement.Draft3CreateFileRequirement;
import org.rabix.bindings.draft3.bean.resource.requirement.Draft3DockerResource;
import org.rabix.bindings.draft3.bean.resource.requirement.Draft3EnvVarRequirement;
import org.rabix.bindings.draft3.bean.resource.requirement.Draft3InlineJavascriptRequirement;
import org.rabix.bindings.draft3.bean.resource.requirement.Draft3ResourceRequirement;
import org.rabix.bindings.draft3.bean.resource.requirement.Draft3SchemaDefRequirement;
import org.rabix.bindings.draft3.bean.resource.requirement.Draft3ShellCommandRequirement;
import org.rabix.bindings.model.Application;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.common.json.BeanSerializer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "class", defaultImpl = Draft3EmbeddedApp.class)
@JsonSubTypes({ 
	@Type(value = Draft3CommandLineTool.class, name = "CommandLineTool"),
	@Type(value = Draft3ExpressionTool.class, name = "ExpressionTool"),
    @Type(value = Draft3Workflow.class, name = "Workflow"),
    @Type(value = Draft3WagnerPythonTool.class, name = "WagnerPythonTool")})
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Draft3JobApp implements Application {

  public static final String DRAFT_3_VERSION = "cwl:draft-3";
  
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
  protected List<Draft3InputPort> inputs = new ArrayList<>();
  @JsonProperty("outputs")
  protected List<Draft3OutputPort> outputs = new ArrayList<>();

  @JsonProperty("hints")
  protected List<Draft3Resource> hints = new ArrayList<>();
  @JsonProperty("requirements")
  protected List<Draft3Resource> requirements = new ArrayList<>();
  
  @JsonProperty("successCodes")
  protected List<Integer> successCodes = new ArrayList<>();

  public String getId() {
    return id;
  }
  
  public String getCwlVersion() {
    return cwlVersion;
  }
  
  public List<Integer> getSuccessCodes() {
    return successCodes;
  }

  @JsonIgnore
  public List<Map<String, Object>> getSchemaDefs() {
    Draft3SchemaDefRequirement schemaDefRequirement = lookForResource(Draft3ResourceType.SCHEMA_DEF_REQUIREMENT, Draft3SchemaDefRequirement.class);
    return schemaDefRequirement != null ? schemaDefRequirement.getSchemaDefs() : null;
  }

  @JsonIgnore
  public Draft3DockerResource getContainerResource() throws IllegalArgumentException {
    Draft3DockerResource dockerResource = lookForResource(Draft3ResourceType.DOCKER_RESOURCE, Draft3DockerResource.class);
    if (dockerResource != null) {
      validateDockerRequirement(dockerResource);
    }
    return dockerResource;
  }

  /**
   * Do some basic validation
   */
  private void validateDockerRequirement(Draft3DockerResource requirement) {
    String imageId = requirement.getImageId();
    String dockerPull = requirement.getDockerPull();

    if (StringUtils.isEmpty(dockerPull) && StringUtils.isEmpty(imageId)) {
      throw new IllegalArgumentException("Docker requirements are empty.");
    }
  }

  @JsonIgnore
  public Draft3ResourceRequirement getResourceRequirement() {
    return lookForResource(Draft3ResourceType.RESOURCE_REQUIREMENT, Draft3ResourceRequirement.class);
  }
  
  @JsonIgnore
  public Draft3InlineJavascriptRequirement getInlineJavascriptRequirement() {
    return lookForResource(Draft3ResourceType.INLINE_JAVASCRIPT_REQUIREMENT, Draft3InlineJavascriptRequirement.class);
  }
  
  @JsonIgnore
  public Draft3ShellCommandRequirement getShellCommandRequirement() {
    return lookForResource(Draft3ResourceType.SHELL_COMMAND_REQUIREMENT, Draft3ShellCommandRequirement.class);
  }
  
  @JsonIgnore
  public Draft3EnvVarRequirement getEnvVarRequirement() {
    return lookForResource(Draft3ResourceType.ENV_VAR_REQUIREMENT, Draft3EnvVarRequirement.class);
  }

  @JsonIgnore
  public Draft3CreateFileRequirement getCreateFileRequirement() {
    return lookForResource(Draft3ResourceType.CREATE_FILE_REQUIREMENT, Draft3CreateFileRequirement.class);
  }

  /**
   * Find one resource by type 
   */
  private <T extends Draft3Resource> T lookForResource(Draft3ResourceType type, Class<T> clazz) {
    List<T> resources = lookForResources(type, clazz);
    return resources != null && resources.size() > 0 ? resources.get(0) : null;
  }
  
  /**
   * Find all resources by type 
   */
  private <T extends Draft3Resource> List<T> lookForResources(Draft3ResourceType type, Class<T> clazz) {
    List<T> resources = getRequirements(type, clazz);
    if (resources == null || resources.size() == 0) {
      resources = getHints(type, clazz);
    }
    return resources;
  }
  
  @JsonIgnore
  private <T extends Draft3Resource> List<T> getRequirements(Draft3ResourceType type, Class<T> clazz) {
    if (requirements == null) {
      return null;
    }
    List<T> result = new ArrayList<>();
    for (Draft3Resource requirement : requirements) {
      if (type.equals(requirement.getType())) {
        result.add(clazz.cast(requirement));
      }
    }
    return result;
  }

  @JsonIgnore
  private <T extends Draft3Resource> List<T> getHints(Draft3ResourceType type, Class<T> clazz) {
    if (hints == null) {
      return null;
    }
    List<T> result = new ArrayList<>();
    for (Draft3Resource hint : hints) {
      if (type.equals(hint.getType())) {
        result.add(clazz.cast(hint));
      }
    }
    return result;
  }

  public ApplicationPort getPort(String id, Class<? extends ApplicationPort> clazz) {
    if (Draft3InputPort.class.equals(clazz)) {
      return getInput(id);
    }
    if (Draft3OutputPort.class.equals(clazz)) {
      return getOutput(id);
    }
    return null;
  }

  @JsonIgnore
  public Draft3InputPort getInput(String id) {
    if (getInputs() == null) {
      return null;
    }
    for (Draft3InputPort input : getInputs()) {
      if (input.getId().toString().equals(id) || input.getId().equals(id)) {
        return input;
      }
    }
    return null;
  }

  @JsonIgnore
  public Draft3OutputPort getOutput(String id) {
    if (getOutputs() == null) {
      return null;
    }
    for (Draft3OutputPort output : getOutputs()) {
      if (output.getId().toString().equals(id) || output.getId().equals(id)) {
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

  public List<Draft3InputPort> getInputs() {
    return inputs;
  }

  public List<Draft3OutputPort> getOutputs() {
    return outputs;
  }

  public List<Draft3Resource> getRequirements() {
    return requirements;
  }

  public List<Draft3Resource> getHints() {
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
    return Draft3JobAppType.WORKFLOW.equals(getType());
  }

  @JsonIgnore
  public boolean isCommandLineTool() {
    return Draft3JobAppType.COMMAND_LINE_TOOL.equals(getType());
  }
  
  @JsonIgnore
  public boolean isEmbedded() {
    return Draft3JobAppType.EMBEDDED.equals(getType());
  }
  
  @JsonIgnore
  public boolean isExpressionTool() {
    return Draft3JobAppType.EXPRESSION_TOOL.equals(getType());
  }
  
  @Override
  public String serialize() {
    return BeanSerializer.serializeFull(this);
  }
  
  public abstract Draft3JobAppType getType();

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
    Draft3JobApp other = (Draft3JobApp) obj;
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
