package org.rabix.bindings.cwl.helper;

import java.util.Map;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.cwl.bean.CWLJob;
import org.rabix.bindings.cwl.bean.CWLRuntime;
import org.rabix.bindings.cwl.bean.resource.requirement.CWLResourceRequirement;
import org.rabix.bindings.cwl.expression.CWLExpressionException;
import org.rabix.bindings.mapper.FileMappingException;
import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.bindings.model.Resources;


public class CWLRuntimeHelper {

  public static CWLRuntime createRuntime(CWLJob cwlJob, Resources resources) throws CWLExpressionException {
    CWLRuntime runtime;
    CWLResourceRequirement resourceRequirement = cwlJob.getApp().getResourceRequirement();
    if (resourceRequirement != null) {
      runtime = resourceRequirement.build(cwlJob, resources);
    }
    else if (resources != null) {
      runtime = new CWLRuntime(resources.getCpu(), resources.getMemMB(), resources.getWorkingDir(), resources.getTmpDir(), resources.getOutDirSize(), resources.getTmpDirSize());
    }
    else {
      runtime = new CWLRuntime(null, null, null, null, null, null);
    }
    return runtime;
  }
  
  public static CWLRuntime remapTmpAndOutDir(CWLRuntime runtime, FilePathMapper filePathMapper, Map<String, Object> config) throws BindingException {
    String outdir = null;
    String tmpdir = null;
    try {
      if(runtime.getOutdir() != null) {
        outdir = filePathMapper.map(runtime.getOutdir(), config);
      }
      if(runtime.getTmpdir() != null) {
        tmpdir = filePathMapper.map(runtime.getTmpdir(), config);
      }
    } catch (FileMappingException e) {
      throw new BindingException(e);
    }
    return new CWLRuntime(runtime.getCores(), runtime.getRam(), outdir, tmpdir, runtime.getOutdirSize(), runtime.getTmpdirSize());
  }
  
  public static CWLRuntime setOutdir(CWLRuntime runtime, String outdir) {
    return new CWLRuntime(runtime.getCores(), runtime.getRam(), outdir, runtime.getTmpdir(), runtime.getOutdirSize(), runtime.getTmpdirSize());
  }
  
  public static CWLRuntime setTmpdir(CWLRuntime runtime, String tmpdir) {
    return new CWLRuntime(runtime.getCores(), runtime.getRam(), runtime.getOutdir(), tmpdir, runtime.getOutdirSize(), runtime.getTmpdirSize());
  }
  
  public static Resources convertToResources(CWLRuntime runtime) {
    return new Resources(runtime.getCores() != null ? runtime.getCores() : null, runtime.getRam() != null ? runtime.getRam() : null, null, false, runtime.getOutdir(), runtime.getTmpdir(), runtime.getOutdirSize(), runtime.getTmpdirSize());
  }

}
