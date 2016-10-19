package org.rabix.bindings.draft2.bean.resource.requirement;

import org.rabix.bindings.draft2.bean.Draft2Job;
import org.rabix.bindings.draft2.bean.resource.Draft2Resource;
import org.rabix.bindings.draft2.bean.resource.Draft2ResourceType;
import org.rabix.bindings.draft2.expression.Draft2ExpressionException;
import org.rabix.bindings.draft2.expression.helper.Draft2ExpressionBeanHelper;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Draft2IORequirement extends Draft2Resource {

  public final static int DEFAULT_VALUE = 0;
  
  public final static String KEY_VALUE = "value";

  @JsonIgnore
  public Integer getIO(Draft2Job job) throws Draft2ExpressionException {
    Object io = getValue(KEY_VALUE);
    if (Draft2ExpressionBeanHelper.isExpression(io)) {
      io = Draft2ExpressionBeanHelper.<Integer> evaluate(job, io);
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
  public Draft2ResourceType getTypeEnum() {
    return Draft2ResourceType.IO_REQUIREMENT;
  }
  
  @Override
  public String toString() {
    return "IORequirement [" + raw + "]";
  }
  
}
