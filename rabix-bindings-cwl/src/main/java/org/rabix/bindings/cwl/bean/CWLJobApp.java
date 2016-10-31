package org.rabix.bindings.cwl.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.rabix.bindings.cwl.bean.resource.CWLResource;
import org.rabix.bindings.cwl.bean.resource.CWLResourceType;
import org.rabix.bindings.cwl.bean.resource.requirement.CWLCreateFileRequirement;
import org.rabix.bindings.cwl.bean.resource.requirement.CWLDockerResource;
import org.rabix.bindings.cwl.bean.resource.requirement.CWLEnvVarRequirement;
import org.rabix.bindings.cwl.bean.resource.requirement.CWLInitialWorkDirRequirement;
import org.rabix.bindings.cwl.bean.resource.requirement.CWLInlineJavascriptRequirement;
import org.rabix.bindings.cwl.bean.resource.requirement.CWLResourceRequirement;
import org.rabix.bindings.cwl.bean.resource.requirement.CWLSchemaDefRequirement;
import org.rabix.bindings.cwl.bean.resource.requirement.CWLShellCommandRequirement;
import org.rabix.bindings.cwl.json.CWLInputPortsDeserializer;
import org.rabix.bindings.cwl.json.CWLOutputPortsDeserializer;
import org.rabix.bindings.cwl.json.CWLResourcesDeserializer;
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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "class", defaultImpl = CWLEmbeddedApp.class)
@JsonSubTypes({ 
	@Type(value = CWLCommandLineTool.class, name = "CommandLineTool"),
	@Type(value = CWLExpressionTool.class, name = "ExpressionTool"),
    @Type(value = CWLWorkflow.class, name = "Workflow"),
    @Type(value = CWLPythonTool.class, name = "PythonTool")})
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class CWLJobApp extends Application {

  public static final String CWL_VERSION = "v1.0";
  
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
  @JsonDeserialize(using = CWLInputPortsDeserializer.class)
  protected List<CWLInputPort> inputs = new ArrayList<>();
  
  @JsonProperty("outputs")
  @JsonDeserialize(using = CWLOutputPortsDeserializer.class)
  protected List<CWLOutputPort> outputs = new ArrayList<>();

  @JsonProperty("hints")
  @JsonDeserialize(using = CWLResourcesDeserializer.class)
  protected List<CWLResource> hints = new ArrayList<>();
  @JsonProperty("requirements")
  @JsonDeserialize(using = CWLResourcesDeserializer.class)
  protected List<CWLResource> requirements = new ArrayList<>();
  
  @JsonProperty("successCodes")
  protected List<Integer> successCodes = new ArrayList<>();

  public String getId() {
    return id;
  }
  
  public String getCwlVersion() {
    return cwlVersion;
  }
  
  @Override
  @JsonIgnore
  public String getVersion() {
    return getCwlVersion();
  }
  
  public List<Integer> getSuccessCodes() {
    return successCodes;
  }

  @JsonIgnore
  public List<Map<String, Object>> getSchemaDefs() {
    CWLSchemaDefRequirement schemaDefRequirement = lookForResource(CWLResourceType.SCHEMA_DEF_REQUIREMENT, CWLSchemaDefRequirement.class);
    return schemaDefRequirement != null ? schemaDefRequirement.getSchemaDefs() : null;
  }

  @JsonIgnore
  public CWLDockerResource getContainerResource() throws IllegalArgumentException {
    CWLDockerResource dockerResource = lookForResource(CWLResourceType.DOCKER_RESOURCE, CWLDockerResource.class);
    if (dockerResource != null) {
      validateDockerRequirement(dockerResource);
    }
    return dockerResource;
  }

  /**
   * Do some basic validation
   */
  private void validateDockerRequirement(CWLDockerResource requirement) {
    String imageId = requirement.getImageId();
    String dockerPull = requirement.getDockerPull();

    if (StringUtils.isEmpty(dockerPull) && StringUtils.isEmpty(imageId)) {
      throw new IllegalArgumentException("Docker requirements are empty.");
    }
  }

  @JsonIgnore
  public CWLResourceRequirement getResourceRequirement() {
    return lookForResource(CWLResourceType.RESOURCE_REQUIREMENT, CWLResourceRequirement.class);
  }
  
  @JsonIgnore
  public CWLInlineJavascriptRequirement getInlineJavascriptRequirement() {
    return lookForResource(CWLResourceType.INLINE_JAVASCRIPT_REQUIREMENT, CWLInlineJavascriptRequirement.class);
  }
  
  @JsonIgnore
  public CWLInitialWorkDirRequirement getInitialWorkDirRequirement() {
    return lookForResource(CWLResourceType.INITIAL_WORK_DIR_REQUIREMENT, CWLInitialWorkDirRequirement.class);
  }
  
  @JsonIgnore
  public CWLShellCommandRequirement getShellCommandRequirement() {
    return lookForResource(CWLResourceType.SHELL_COMMAND_REQUIREMENT, CWLShellCommandRequirement.class);
  }
  
  @JsonIgnore
  public CWLEnvVarRequirement getEnvVarRequirement() {
    return lookForResource(CWLResourceType.ENV_VAR_REQUIREMENT, CWLEnvVarRequirement.class);
  }

  @JsonIgnore
  public CWLCreateFileRequirement getCreateFileRequirement() {
    return lookForResource(CWLResourceType.CREATE_FILE_REQUIREMENT, CWLCreateFileRequirement.class);
  }
  
  @JsonIgnore
  public void setHint(CWLResource resource) {
    boolean add = true;
    for (CWLResource hint : hints) {
      if (resource.getType().equals(hint.getType())) {
        add = false;
        break;
      }
    }
    if(add) {
      hints.add(resource);
    }
  }
  
  @JsonIgnore
  public void setRequirement(CWLResource resource) {
    boolean add = true;
    for (CWLResource requirement : requirements) {
      if (resource.getType().equals(requirement.getType())) {
        add = false;
        break;
      }
    }
    if(add) {
      requirements.add(resource);
      for (CWLResource hint : hints) {
        if (resource.getType().equals(hint.getType())) {
          hints.remove(hint);
          break;
        }
      }
    }
  }

  /**
   * Find one resource by type 
   */
  private <T extends CWLResource> T lookForResource(CWLResourceType type, Class<T> clazz) {
    List<T> resources = lookForResources(type, clazz);
    return resources != null && resources.size() > 0 ? resources.get(0) : null;
  }
  
  /**
   * Find all resources by type 
   */
  private <T extends CWLResource> List<T> lookForResources(CWLResourceType type, Class<T> clazz) {
    List<T> resources = getRequirements(type, clazz);
    if (resources == null || resources.size() == 0) {
      resources = getHints(type, clazz);
    }
    return resources;
  }
  
  @JsonIgnore
  private <T extends CWLResource> List<T> getRequirements(CWLResourceType type, Class<T> clazz) {
    if (requirements == null) {
      return null;
    }
    List<T> result = new ArrayList<>();
    for (CWLResource requirement : requirements) {
      if (type.equals(requirement.getTypeEnum())) {
        result.add(clazz.cast(requirement));
      }
    }
    return result;
  }

  @JsonIgnore
  private <T extends CWLResource> List<T> getHints(CWLResourceType type, Class<T> clazz) {
    if (hints == null) {
      return null;
    }
    List<T> result = new ArrayList<>();
    for (CWLResource hint : hints) {
      if (type.equals(hint.getTypeEnum())) {
        result.add(clazz.cast(hint));
      }
    }
    return result;
  }

  public ApplicationPort getPort(String id, Class<? extends ApplicationPort> clazz) {
    if (CWLInputPort.class.equals(clazz)) {
      return getInput(id);
    }
    if (CWLOutputPort.class.equals(clazz)) {
      return getOutput(id);
    }
    return null;
  }

  @JsonIgnore
  public CWLInputPort getInput(String id) {
    if (getInputs() == null) {
      return null;
    }
    for (CWLInputPort input : getInputs()) {
      if (input.getId().toString().equals(id) || input.getId().equals(id)) {
        return input;
      }
    }
    return null;
  }

  @JsonIgnore
  public CWLOutputPort getOutput(String id) {
    if (getOutputs() == null) {
      return null;
    }
    for (CWLOutputPort output : getOutputs()) {
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

  public List<CWLInputPort> getInputs() {
    return inputs;
  }

  public List<CWLOutputPort> getOutputs() {
    return outputs;
  }

  public List<CWLResource> getRequirements() {
    return requirements;
  }

  public List<CWLResource> getHints() {
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
    return CWLJobAppType.WORKFLOW.equals(getType());
  }

  @JsonIgnore
  public boolean isCommandLineTool() {
    return CWLJobAppType.COMMAND_LINE_TOOL.equals(getType());
  }
  
  @JsonIgnore
  public boolean isEmbedded() {
    return CWLJobAppType.EMBEDDED.equals(getType());
  }
  
  @JsonIgnore
  public boolean isExpressionTool() {
    return CWLJobAppType.EXPRESSION_TOOL.equals(getType());
  }
  
  @Override
  public String serialize() {
    return BeanSerializer.serializeFull(this);
  }
  
  public abstract CWLJobAppType getType();

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
    CWLJobApp other = (CWLJobApp) obj;
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
    return "JobApp [id=" + id + ", context=" + context + ", description=" + description + ", label=" + label + ", contributor=" + contributor + ", owner=" + owner + ", hints=" + hints + ", inputs=" + inputs + ", outputs=" + outputs + ", requirements=" + requirements + "]";
  }
  
}
