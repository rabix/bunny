package org.rabix.bindings.draft2.bean.resource.requirement;

import java.util.List;

import org.rabix.bindings.draft2.bean.resource.Draft2Resource;
import org.rabix.bindings.draft2.bean.resource.Draft2ResourceType;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Draft2ExpressionEngineRequirement extends Draft2Resource {

  public final static int DEFAULT_VALUE = 0;

  public final static String KEY_ID = "id";
  public final static String KEY_ENGINE_CONFIG = "engineConfig";
  
  @JsonIgnore
  public String getId() {
    return getValue(KEY_ID);
  }  
  
  @JsonIgnore
  public List<String> getEngineConfigs(String engineId) {
    if (engineId.equals(getId())) {
      return getValue(KEY_ENGINE_CONFIG);
    }
    return null;
  }
  
  @Override
  @JsonIgnore
  public Draft2ResourceType getTypeEnum() {
    return Draft2ResourceType.EXPRESSION_ENGINE_REQUIREMENT;
  }

  @Override
  public String toString() {
    return "ExpressionEngineRequirement [" + raw + "]";
  }

}
