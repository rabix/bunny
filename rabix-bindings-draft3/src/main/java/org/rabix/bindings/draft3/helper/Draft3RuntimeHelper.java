package org.rabix.bindings.draft3.helper;

import org.rabix.bindings.draft3.bean.Draft3Job;
import org.rabix.bindings.draft3.bean.Draft3Runtime;
import org.rabix.bindings.draft3.bean.resource.requirement.Draft3ResourceRequirement;
import org.rabix.bindings.draft3.expression.Draft3ExpressionException;
import org.rabix.bindings.model.Resources;

public class Draft3RuntimeHelper {
  
  public static Draft3Runtime createRuntime(Draft3Job job) throws Draft3ExpressionException {
    Draft3Runtime runtime = job.getRuntime();
    if (runtime == null) {
      Draft3ResourceRequirement resourceRequirement = job.getApp().getResourceRequirement();
      if(resourceRequirement != null) {
        runtime = resourceRequirement.build(job);
      }
    }    
    if(runtime == null) {
      runtime = new Draft3Runtime(null, null, null, null, null, null);
    }
    return runtime;
  }
  
  public static Draft3Runtime setOutdir(Draft3Runtime runtime, String outdir) {
    return new Draft3Runtime(runtime.getCores(), runtime.getRam(), outdir, runtime.getTmpdir(), runtime.getOutdirSize(), runtime.getTmpdirSize());
  }
  
  public static Draft3Runtime setTmpdir(Draft3Runtime runtime, String tmpdir) {
    return new Draft3Runtime(runtime.getCores(), runtime.getRam(), runtime.getOutdir(), tmpdir, runtime.getOutdirSize(), runtime.getTmpdirSize());
  }
  
  public static Resources convertToResources(Draft3Runtime runtime) {
    return new Resources(runtime.getCores() != null ? runtime.getCores() : null, runtime.getRam() != null ? runtime.getRam() : null, null, false, runtime.getOutdir(), runtime.getTmpdir());
  }

}