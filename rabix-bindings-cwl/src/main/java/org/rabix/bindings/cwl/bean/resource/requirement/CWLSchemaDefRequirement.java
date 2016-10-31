package org.rabix.bindings.cwl.bean.resource.requirement;

import java.util.List;
import java.util.Map;

import org.rabix.bindings.cwl.bean.resource.CWLResource;
import org.rabix.bindings.cwl.bean.resource.CWLResourceType;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CWLSchemaDefRequirement extends CWLResource {

  public final static String KEY_SCHEMA_DEFS = "types";

  @JsonIgnore
  public List<Map<String, Object>> getSchemaDefs() {
    return this.<List<Map<String, Object>>> getValue(KEY_SCHEMA_DEFS);
  }
  
  @Override
  @JsonIgnore
  public CWLResourceType getTypeEnum() {
    return CWLResourceType.SCHEMA_DEF_REQUIREMENT;
  }
  
  @Override
  public String toString() {
    return "SchemaDefRequirement [" + raw + "]";
  }
  
}
