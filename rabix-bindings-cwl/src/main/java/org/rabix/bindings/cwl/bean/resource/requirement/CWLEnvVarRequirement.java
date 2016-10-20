package org.rabix.bindings.cwl.bean.resource.requirement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.rabix.bindings.cwl.bean.resource.CWLResource;
import org.rabix.bindings.cwl.bean.resource.CWLResourceType;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Requirement that defines environment variables needed for execution 
 */
public class CWLEnvVarRequirement extends CWLResource {

  public final static String KEY_ENV_DEF = "envDef";

  @JsonIgnore
  @SuppressWarnings("unchecked")
  public List<EnvironmentDef> getEnvironmentDefinitions() {
    List<EnvironmentDef> definitions = new ArrayList<>();

    Object envDef = getValue(KEY_ENV_DEF);
    if (envDef instanceof List<?>) {    // handle lists
      List<Map<String, Object>> envDefObjs = (List<Map<String, Object>>) envDef;
      if (envDefObjs != null) {
        for (Map<String, Object> envDefObj : envDefObjs) {
          String name = (String) envDefObj.get(EnvironmentDef.KEY_NAME);
          Object value = envDefObj.get(EnvironmentDef.KEY_VALUE);
          definitions.add(new EnvironmentDef(name, value));
        }
      }
      return definitions;
    }
    if (envDef instanceof Map<?,?>) {   // handle maps
      Map<String, Object> envDefObjs = (Map<String, Object>) envDef;
      if (envDefObjs != null) {
        for (Entry<String, Object> envDefObj : envDefObjs.entrySet()) {
          definitions.add(new EnvironmentDef(envDefObj.getKey(), envDefObj.getValue()));
        }
      }
      return definitions;
    }
    return null;
  }
  
  @Override
  public CWLResourceType getTypeEnum() {
    return CWLResourceType.ENV_VAR_REQUIREMENT;
  }

  /**
   * Environment definition
   */
  public static class EnvironmentDef {

    public final static String KEY_NAME = "envName";
    public final static String KEY_VALUE = "envValue";

    private String name;
    private Object value;

    public EnvironmentDef(String name, Object value) {
      this.name = name;
      this.value = value;
    }

    public String getName() {
      return name;
    }

    public Object getValue() {
      return value;
    }

  }
}
