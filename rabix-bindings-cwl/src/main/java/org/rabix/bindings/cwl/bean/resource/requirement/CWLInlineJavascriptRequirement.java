package org.rabix.bindings.cwl.bean.resource.requirement;

import java.util.List;

import org.rabix.bindings.cwl.bean.resource.CWLResource;
import org.rabix.bindings.cwl.bean.resource.CWLResourceType;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CWLInlineJavascriptRequirement extends CWLResource {

  public final static String KEY_EXPRESSION_LIB = "expressionLib";

  @JsonIgnore
  public List<String> getExpressionLib() {
    return getValue(KEY_EXPRESSION_LIB);
  }

  @Override
  public CWLResourceType getTypeEnum() {
    return CWLResourceType.INLINE_JAVASCRIPT_REQUIREMENT;
  }

}
