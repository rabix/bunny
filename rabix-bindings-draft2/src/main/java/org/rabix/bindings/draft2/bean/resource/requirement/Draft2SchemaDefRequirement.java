package org.rabix.bindings.draft2.bean.resource.requirement;

import java.util.List;
import java.util.Map;

import org.rabix.bindings.draft2.bean.resource.Draft2Resource;
import org.rabix.bindings.draft2.bean.resource.Draft2ResourceType;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Draft2SchemaDefRequirement extends Draft2Resource {

  public final static String KEY_SCHEMA_DEFS = "types";

  @JsonIgnore
  public List<Map<String, Object>> getSchemaDefs() {
    return this.<List<Map<String, Object>>> getValue(KEY_SCHEMA_DEFS);
  }
  
  @Override
  @JsonIgnore
  public Draft2ResourceType getTypeEnum() {
    return Draft2ResourceType.SCHEMA_DEF_REQUIREMENT;
  }
  
  @Override
  public String toString() {
    return "SchemaDefRequirement [" + raw + "]";
  }
  
}
