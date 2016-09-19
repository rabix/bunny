package org.rabix.bindings.draft3.bean.resource.requirement;

import java.util.List;

import org.rabix.bindings.draft3.bean.resource.Draft3Resource;
import org.rabix.bindings.draft3.bean.resource.Draft3ResourceType;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Draft3InlineJavascriptRequirement extends Draft3Resource {

  public final static String KEY_EXPRESSION_LIB = "expressionLib";

  @JsonIgnore
  public List<String> getExpressionLib() {
    return getValue(KEY_EXPRESSION_LIB);
  }

  @Override
  public Draft3ResourceType getType() {
    return Draft3ResourceType.INLINE_JAVASCRIPT_REQUIREMENT;
  }

}
