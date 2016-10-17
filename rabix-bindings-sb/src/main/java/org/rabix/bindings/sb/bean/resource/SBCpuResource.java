package org.rabix.bindings.sb.bean.resource;

import org.rabix.bindings.sb.bean.SBJob;
import org.rabix.bindings.sb.expression.SBExpressionException;
import org.rabix.bindings.sb.expression.helper.SBExpressionBeanHelper;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SBCpuResource extends SBResource {

  public final static int DEFAULT_VALUE = 0;
  
  public final static String KEY_VALUE = "value";

  @JsonIgnore
  public Integer getCpu(SBJob job) throws SBExpressionException {
    Object cpu = getValue(KEY_VALUE);
    if (SBExpressionBeanHelper.isExpression(cpu)) {
      cpu = SBExpressionBeanHelper.<Integer> evaluate(job, cpu);
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
  public SBResourceType getTypeEnum() {
    return SBResourceType.CPU_RESOURCE;
  }
  
  @Override
  public String toString() {
    return "CpuResource [" + raw + "]";
  }
  
}
