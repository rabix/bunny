package org.rabix.bindings.cwl.bean;

import java.util.ArrayList;
import java.util.Collections;
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
import org.rabix.bindings.cwl.json.CWLJobAppDeserializer;
import org.rabix.bindings.cwl.json.CWLOutputPortsDeserializer;
import org.rabix.bindings.cwl.json.CWLResourcesDeserializer;
import org.rabix.bindings.model.Application;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.common.json.BeanPropertyView;
import org.rabix.common.json.BeanSerializer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using=CWLJobAppDeserializer.class)
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class CWLJobApp extends Application {

  public static final String CWL_VERSION = "v1.0";
  
  @JsonProperty("cwlVersion")
  protected String cwlVersion;

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

  @JsonProperty("appFileLocation")
  @JsonView(BeanPropertyView.Full.class)
  protected String appFileLocation;
  
  @JsonIgnore
  public String getId() {
    return (String) getProperty("id");
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
  
  public List<Integer> getSuccessCodes() {
    return successCodes;
  }
  
  public String getAppFileLocation() {
    return appFileLocation;
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
  protected void validateDockerRequirement(CWLDockerResource requirement) {
    List<String> res = checkDockerRequirement(requirement);
    if (! res.isEmpty()) {
      throw new IllegalArgumentException(String.join("\n", res));
    }
  }

  protected List<String> checkDockerRequirement(CWLDockerResource requirement) {
    String imageId = requirement.getImageId();
    String dockerPull = requirement.getDockerPull();

    if (StringUtils.isEmpty(dockerPull) && StringUtils.isEmpty(imageId)) {
      return Collections.singletonList("Docker requirement contains neither 'dockerPull' nor 'imageId'.");
    }
    return Collections.emptyList();
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
  protected <T extends CWLResource> T lookForResource(CWLResourceType type, Class<T> clazz) {
    List<T> resources = lookForResources(type, clazz);
    return resources != null && resources.size() > 0 ? resources.get(0) : null;
  }
  
  /**
   * Find all resources by type 
   */
  protected <T extends CWLResource> List<T> lookForResources(CWLResourceType type, Class<T> clazz) {
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

  @JsonIgnore
  public String getContext() {
    return (String) getProperty("@context");
  }

  @JsonIgnore
  public String getDescription() {
    return (String) getProperty("description");
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

  @JsonIgnore
  public String getLabel() {
    return (String) getProperty("label");
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
    return BeanSerializer.serializePartial(this);
  }
  
  public abstract CWLJobAppType getType();

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
    CWLJobApp other = (CWLJobApp) obj;
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
    return "JobApp [id=" + getId() + ", context=" + getContext() + ", description=" + getDescription() + ", label=" + getLabel() + ", hints=" + hints + ", inputs=" + inputs + ", outputs=" + outputs + ", requirements=" + requirements + "]";
  }
  
}
