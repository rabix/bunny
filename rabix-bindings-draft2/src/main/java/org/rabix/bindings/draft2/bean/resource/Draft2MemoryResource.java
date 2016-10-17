package org.rabix.bindings.draft2.bean.resource;

import org.rabix.bindings.draft2.bean.Draft2Job;
import org.rabix.bindings.draft2.expression.Draft2ExpressionException;
import org.rabix.bindings.draft2.expression.helper.Draft2ExpressionBeanHelper;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Draft2MemoryResource extends Draft2Resource {

  public final static int DEFAULT_VALUE = 0;

  public final static String KEY_VALUE = "value";

  @JsonIgnore
  public Integer getMemory(Draft2Job job) throws Draft2ExpressionException {
    Object memory = getValue(KEY_VALUE);
    if (Draft2ExpressionBeanHelper.isExpression(memory)) {
      memory = Draft2ExpressionBeanHelper.<Integer> evaluate(job, memory);
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
  public Draft2ResourceType getTypeEnum() {
    return Draft2ResourceType.MEMORY_RESOURCE;
  }

  @Override
  public String toString() {
    return "MemoryResource [" + raw + "]";
  }
}
