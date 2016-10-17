package org.rabix.bindings.sb.bean.resource.requirement;

import org.rabix.bindings.sb.bean.SBJob;
import org.rabix.bindings.sb.bean.resource.SBResource;
import org.rabix.bindings.sb.bean.resource.SBResourceType;
import org.rabix.bindings.sb.expression.SBExpressionException;
import org.rabix.bindings.sb.expression.helper.SBExpressionBeanHelper;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SBIORequirement extends SBResource {

  public final static int DEFAULT_VALUE = 0;
  
  public final static String KEY_VALUE = "value";

  @JsonIgnore
  public Integer getIO(SBJob job) throws SBExpressionException {
    Object io = getValue(KEY_VALUE);
    if (SBExpressionBeanHelper.isExpression(io)) {
      io = SBExpressionBeanHelper.<Integer> evaluate(job, io);
    }
    if (io == null) {
      return DEFAULT_VALUE;
    }
    if (!(io instanceof Integer)) {
      throw new IllegalArgumentException("Invalid IO value " + io);
    }
    return (Integer) io;
  }
  
  @Override
  @JsonIgnore
  public SBResourceType getTypeEnum() {
    return SBResourceType.IO_REQUIREMENT;
  }
  
  @Override
  public String toString() {
    return "IORequirement [" + raw + "]";
  }
  
}
