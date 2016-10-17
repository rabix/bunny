package org.rabix.bindings.draft2.bean.resource;

import org.rabix.bindings.draft2.bean.Draft2Job;
import org.rabix.bindings.draft2.expression.Draft2ExpressionException;
import org.rabix.bindings.draft2.expression.helper.Draft2ExpressionBeanHelper;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Draft2CpuResource extends Draft2Resource {

  public final static int DEFAULT_VALUE = 0;
  
  public final static String KEY_VALUE = "value";

  @JsonIgnore
  public Integer getCpu(Draft2Job job) throws Draft2ExpressionException {
    Object cpu = getValue(KEY_VALUE);
    if (Draft2ExpressionBeanHelper.isExpression(cpu)) {
      cpu = Draft2ExpressionBeanHelper.<Integer> evaluate(job, cpu);
    }
    if (cpu == null) {
      return DEFAULT_VALUE;
    }
    if (!(cpu instanceof Integer)) {
      throw new IllegalArgumentException("Invalid CPU value " + cpu);
    }
    return (Integer) cpu;
  }
  
  @Override
  @JsonIgnore
  public Draft2ResourceType getTypeEnum() {
    return Draft2ResourceType.CPU_RESOURCE;
  }
  
  @Override
  public String toString() {
    return "CpuResource [" + raw + "]";
  }
  
}
