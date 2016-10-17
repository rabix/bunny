package org.rabix.bindings.sb.bean.resource.requirement;

import java.util.List;
import java.util.Map;

import org.rabix.bindings.sb.bean.resource.SBResource;
import org.rabix.bindings.sb.bean.resource.SBResourceType;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SBSchemaDefRequirement extends SBResource {

  public final static String KEY_SCHEMA_DEFS = "types";

  @JsonIgnore
  public List<Map<String, Object>> getSchemaDefs() {
    return this.<List<Map<String, Object>>> getValue(KEY_SCHEMA_DEFS);
  }
  
  @Override
  @JsonIgnore
  public SBResourceType getTypeEnum() {
    return SBResourceType.SCHEMA_DEF_REQUIREMENT;
  }
  
  @Override
  public String toString() {
    return "SchemaDefRequirement [" + raw + "]";
  }
  
}
