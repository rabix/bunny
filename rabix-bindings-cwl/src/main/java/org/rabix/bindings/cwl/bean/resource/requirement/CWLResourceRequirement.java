package org.rabix.bindings.cwl.bean.resource.requirement;

import org.rabix.bindings.cwl.bean.CWLJob;
import org.rabix.bindings.cwl.bean.CWLRuntime;
import org.rabix.bindings.cwl.bean.resource.CWLResource;
import org.rabix.bindings.cwl.bean.resource.CWLResourceType;
import org.rabix.bindings.cwl.expression.CWLExpressionException;
import org.rabix.bindings.cwl.expression.CWLExpressionResolver;
import org.rabix.bindings.model.Resources;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CWLResourceRequirement extends CWLResource {
  public final static Long CORES_MIN_DEFAULT = 1L;
  public final static Long CORES_MAX_DEFAULT = 1L;
  public final static Long RAM_MIN_DEFAULT = 1024L;
  public final static Long RAM_MAX_DEFAULT = 1024L;
  public final static Long TMPDIR_MIN_DEFAULT = 1024L;
  public final static Long TMPDIR_MAX_DEFAULT = 1024L;
  public final static Long OUTDIR_MIN_DEFAULT = 1024L;
  public final static Long OUTDIR_MAX_DEFAULT = 1024L;
  
  public final static String KEY_CORES_MIN = "coresMin";
  public final static String KEY_CORES_MAX = "coresMax";
  public final static String KEY_RAM_MIN = "ramMin";
  public final static String KEY_RAM_MAX = "ramMax";
  public final static String KEY_TMPDIR_MIN = "tmpdirMin";
  public final static String KEY_TMPDIR_MAX = "tmpdirMax";
  public final static String KEY_OUTDIR_MIN = "outdirMin";
  public final static String KEY_OUTDIR_MAX = "outdirMax";

  @JsonIgnore
  public Long getCoresMin(CWLJob job) throws CWLExpressionException {
    return getValue(job, KEY_CORES_MIN);
  }

  @JsonIgnore
  public Long getCoresMax(CWLJob job) throws CWLExpressionException {
    return getValue(job, KEY_CORES_MAX);
  }

  @JsonIgnore
  public Long getRamMin(CWLJob job) throws CWLExpressionException {
    return getValue(job, KEY_RAM_MIN);
  }

  @JsonIgnore
  public Long getRamMax(CWLJob job) throws CWLExpressionException {
    return getValue(job, KEY_RAM_MAX);
  }

  @JsonIgnore
  public Long getTmpdirMin(CWLJob job) throws CWLExpressionException {
    return getValue(job, KEY_TMPDIR_MIN);
  }

  @JsonIgnore
  public Long getTmpdirMax(CWLJob job) throws CWLExpressionException {
    return getValue(job, KEY_TMPDIR_MAX);
  }
  
  @JsonIgnore
  public Long getOutdirMin(CWLJob job) throws CWLExpressionException {
    return getValue(job, KEY_OUTDIR_MIN);
  }
  
  @JsonIgnore
  public Long getOutdirMax(CWLJob job) throws CWLExpressionException {
    return getValue(job, KEY_OUTDIR_MAX);
  }

  @JsonIgnore
  public Long getValue(CWLJob job, String key) throws CWLExpressionException {
    Object value = getValue(key);
    value = CWLExpressionResolver.resolve(value, job, null);
    if (value == null) {
      return null;
    }
    if (value instanceof Integer) {
      return Long.parseLong(Integer.toString((int) value));
    }
    return (Long) value;
  }
  
  @JsonIgnore
  public CWLRuntime build(CWLJob job, Resources resources) throws CWLExpressionException {
    Long coresMin = getCoresMin(job);
    Long coresMax = getCoresMax(job);

    Long cores = coresMin != null ? coresMin : coresMax;
    if (cores == null) {
      cores = resources != null ? resources.getCpu() : CORES_MIN_DEFAULT;
    }

    Long ramMin = getRamMin(job);
    Long ramMax = getRamMax(job);

    Long ram = ramMin != null ? ramMin : ramMax;
    if (ram == null) {
      ram = resources != null? resources.getMemMB() : RAM_MIN_DEFAULT;
    }

    Long tmpdirMin = getTmpdirMin(job);
    Long tmpdirMax = getTmpdirMax(job);

    Long tmpDir = tmpdirMin != null ? tmpdirMin : tmpdirMax;
    if (tmpDir == null) {
      tmpDir = resources != null ? resources.getTmpDirSize() : TMPDIR_MIN_DEFAULT;
    }

    Long outdirMin = getOutdirMin(job);
    Long outdirMax = getOutdirMax(job);

    Long outDir = outdirMin != null ? outdirMin : outdirMax;
    if (outDir == null) {
      outDir = resources != null ? resources.getOutDirSize() : OUTDIR_MIN_DEFAULT;
    }
    return new CWLRuntime(cores, ram, null, null, tmpDir, outDir);
  }
  
  @Override
  public CWLResourceType getTypeEnum() {
    return CWLResourceType.RESOURCE_REQUIREMENT;
  }

}
