package org.rabix.bindings.sb.bean.resource;

import java.util.HashMap;
import java.util.Map;

import org.rabix.bindings.sb.bean.resource.requirement.SBCreateFileRequirement;
import org.rabix.bindings.sb.bean.resource.requirement.SBDockerResource;
import org.rabix.bindings.sb.bean.resource.requirement.SBEnvVarRequirement;
import org.rabix.bindings.sb.bean.resource.requirement.SBExpressionEngineRequirement;
import org.rabix.bindings.sb.bean.resource.requirement.SBIORequirement;
import org.rabix.bindings.sb.bean.resource.requirement.SBSchemaDefRequirement;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeId;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "class", defaultImpl = SBResource.class, visible = true)
@JsonSubTypes({ @Type(value = SBDockerResource.class, name = "DockerRequirement"),
    @Type(value = SBExpressionEngineRequirement.class, name = "ExpressionEngineRequirement"),
    @Type(value = SBIORequirement.class, name = "IORequirement"),
    @Type(value = SBSchemaDefRequirement.class, name = "SchemaDefRequirement"),
    @Type(value = SBCreateFileRequirement.class, name = "CreateFileRequirement"),
    @Type(value = SBEnvVarRequirement.class, name = "EnvVarRequirement"),
    @Type(value = SBMemoryResource.class, name = "sbg:MemRequirement"),
    @Type(value = SBCpuResource.class, name = "sbg:CPURequirement") })
@JsonInclude(Include.NON_NULL)
public class SBResource {
  
  @JsonProperty("class")
  protected String type;
  protected Map<String, Object> raw = new HashMap<>();

  public SBResource() {
  }

  @SuppressWarnings("unchecked")
  @JsonIgnore
  public <T> T getValue(String key) {
    if (raw == null) {
      return null;
    }

    Object value = raw.get(key);
    return value != null ? (T) value : null;
  }

  @JsonAnySetter
  public void add(String key, Object value) {
    raw.put(key, value);
  }

  @JsonAnyGetter
  public Map<String, Object> getRaw() {
    return raw;
  }

  @JsonIgnore
  public SBResourceType getTypeEnum() {
    return SBResourceType.OTHER;
  }

  @JsonTypeId
  public String getType() {
    return type;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((raw == null) ? 0 : raw.hashCode());
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
    SBResource other = (SBResource) obj;
    if (raw == null) {
      if (other.raw != null)
        return false;
    } else if (!raw.equals(other.raw))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "SBResource [type=" + type + ", raw=" + raw + "]";
  }
  
}
