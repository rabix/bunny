package org.rabix.bindings.draft2.bean.resource.requirement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.draft2.bean.resource.Draft2Resource;
import org.rabix.bindings.draft2.bean.resource.Draft2ResourceType;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Requirement that defines environment variables needed for execution 
 */
public class Draft2EnvVarRequirement extends Draft2Resource {

  public final static String KEY_ENV_DEF = "envDef";

  @JsonIgnore
  public List<EnvironmentDef> getEnvironmentDefinitions() {
    List<EnvironmentDef> definitions = new ArrayList<>();

    List<Map<String, Object>> envDefObjs = getValue(KEY_ENV_DEF);
    if (envDefObjs != null) {
      for (Map<String, Object> envDefObj : envDefObjs) {
        String name = (String) envDefObj.get(EnvironmentDef.KEY_NAME);
        Object value = envDefObj.get(EnvironmentDef.KEY_VALUE);
        definitions.add(new EnvironmentDef(name, value));
      }
    }
    return definitions;
  }

  @Override
  public Draft2ResourceType getTypeEnum() {
    return Draft2ResourceType.ENV_VAR_REQUIREMENT;
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
