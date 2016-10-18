package org.rabix.bindings.draft2.bean.resource;

import java.util.HashMap;
import java.util.Map;

import org.rabix.bindings.draft2.bean.resource.requirement.Draft2CreateFileRequirement;
import org.rabix.bindings.draft2.bean.resource.requirement.Draft2DockerResource;
import org.rabix.bindings.draft2.bean.resource.requirement.Draft2EnvVarRequirement;
import org.rabix.bindings.draft2.bean.resource.requirement.Draft2ExpressionEngineRequirement;
import org.rabix.bindings.draft2.bean.resource.requirement.Draft2IORequirement;
import org.rabix.bindings.draft2.bean.resource.requirement.Draft2SchemaDefRequirement;

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

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "class", defaultImpl = Draft2Resource.class, visible = true)
@JsonSubTypes({ @Type(value = Draft2DockerResource.class, name = "DockerRequirement"),
    @Type(value = Draft2ExpressionEngineRequirement.class, name = "ExpressionEngineRequirement"),
    @Type(value = Draft2IORequirement.class, name = "IORequirement"),
    @Type(value = Draft2SchemaDefRequirement.class, name = "SchemaDefRequirement"),
    @Type(value = Draft2CreateFileRequirement.class, name = "CreateFileRequirement"),
    @Type(value = Draft2EnvVarRequirement.class, name = "EnvVarRequirement"),
    @Type(value = Draft2MemoryResource.class, name = "sbg:MemRequirement"),
    @Type(value = Draft2CpuResource.class, name = "sbg:CPURequirement") })
@JsonInclude(Include.NON_NULL)
public class Draft2Resource {
  
  @JsonProperty("class")
  protected String type;
  protected Map<String, Object> raw = new HashMap<>();

  public Draft2Resource() {
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
  public Draft2ResourceType getTypeEnum() {
    return Draft2ResourceType.OTHER;
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
    Draft2Resource other = (Draft2Resource) obj;
    if (raw == null) {
      if (other.raw != null)
        return false;
    } else if (!raw.equals(other.raw))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Draft2Resource [type=" + type + ", raw=" + raw + "]";
  }
  
}
