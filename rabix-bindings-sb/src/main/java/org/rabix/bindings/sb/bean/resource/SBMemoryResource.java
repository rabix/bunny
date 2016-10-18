package org.rabix.bindings.sb.bean.resource;

import org.rabix.bindings.sb.bean.SBJob;
import org.rabix.bindings.sb.expression.SBExpressionException;
import org.rabix.bindings.sb.expression.helper.SBExpressionBeanHelper;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SBMemoryResource extends SBResource {

  public final static int DEFAULT_VALUE = 0;

  public final static String KEY_VALUE = "value";

  @JsonIgnore
  public Integer getMemory(SBJob job) throws SBExpressionException {
    Object memory = getValue(KEY_VALUE);
    if (SBExpressionBeanHelper.isExpression(memory)) {
      memory = SBExpressionBeanHelper.<Integer> evaluate(job, memory);
    }
    if (memory == null) {
      return DEFAULT_VALUE;
    }
    if (!(memory instanceof Integer)) {
      throw new IllegalArgumentException("Invalid memory value " + memory);
    }
    return (Integer) memory;
  }

  @Override
  @JsonIgnore
  public SBResourceType getTypeEnum() {
    return SBResourceType.MEMORY_RESOURCE;
  }

  @Override
  public String toString() {
    return "MemoryResource [" + raw + "]";
  }
}
