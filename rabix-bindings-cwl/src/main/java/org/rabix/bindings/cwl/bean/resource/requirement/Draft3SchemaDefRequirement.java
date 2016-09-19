package org.rabix.bindings.draft3.bean.resource.requirement;

import java.util.List;
import java.util.Map;

import org.rabix.bindings.draft3.bean.resource.Draft3Resource;
import org.rabix.bindings.draft3.bean.resource.Draft3ResourceType;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Draft3SchemaDefRequirement extends Draft3Resource {

  public final static String KEY_SCHEMA_DEFS = "types";

  @JsonIgnore
  public List<Map<String, Object>> getSchemaDefs() {
    return this.<List<Map<String, Object>>> getValue(KEY_SCHEMA_DEFS);
  }
  
  @Override
  @JsonIgnore
  public Draft3ResourceType getType() {
    return Draft3ResourceType.SCHEMA_DEF_REQUIREMENT;
  }
  
  @Override
  public String toString() {
    return "SchemaDefRequirement [" + raw + "]";
  }
  
}
