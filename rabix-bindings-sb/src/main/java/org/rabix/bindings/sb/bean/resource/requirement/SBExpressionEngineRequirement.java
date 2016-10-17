package org.rabix.bindings.sb.bean.resource.requirement;

import java.util.List;

import org.rabix.bindings.sb.bean.resource.SBResource;
import org.rabix.bindings.sb.bean.resource.SBResourceType;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SBExpressionEngineRequirement extends SBResource {

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
  public SBResourceType getTypeEnum() {
    return SBResourceType.EXPRESSION_ENGINE_REQUIREMENT;
  }

  @Override
  public String toString() {
    return "ExpressionEngineRequirement [" + raw + "]";
  }

}
