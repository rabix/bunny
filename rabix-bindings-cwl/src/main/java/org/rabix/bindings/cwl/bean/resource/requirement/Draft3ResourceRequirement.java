package org.rabix.bindings.draft3.bean.resource.requirement;

import java.util.HashMap;
import java.util.Map;

import org.rabix.bindings.draft3.bean.Draft3Job;
import org.rabix.bindings.draft3.bean.resource.Draft3Resource;
import org.rabix.bindings.draft3.bean.resource.Draft3ResourceType;
import org.rabix.bindings.draft3.expression.Draft3ExpressionException;
import org.rabix.bindings.draft3.expression.Draft3ExpressionResolver;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Draft3ResourceRequirement extends Draft3Resource {

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
  public Long getCoresMin(Draft3Job job) throws Draft3ExpressionException {
    return getValue(job, KEY_CORES_MIN);
  }

  @JsonIgnore
  public Long getCoresMax(Draft3Job job) throws Draft3ExpressionException {
    return getValue(job, KEY_CORES_MAX);
  }

  @JsonIgnore
  public Long getRamMin(Draft3Job job) throws Draft3ExpressionException {
    return getValue(job, KEY_RAM_MIN);
  }

  @JsonIgnore
  public Long getRamMax(Draft3Job job) throws Draft3ExpressionException {
    return getValue(job, KEY_RAM_MAX);
  }

  @JsonIgnore
  public Long getTmpdirMin(Draft3Job job) throws Draft3ExpressionException {
    return getValue(job, KEY_TMPDIR_MIN);
  }

  @JsonIgnore
  public Long getTmpdirMax(Draft3Job job) throws Draft3ExpressionException {
    return getValue(job, KEY_TMPDIR_MAX);
  }
  
  @JsonIgnore
  public Long getOutdirMin(Draft3Job job) throws Draft3ExpressionException {
    return getValue(job, KEY_OUTDIR_MIN);
  }
  
  @JsonIgnore
  public Long getOutdirMax(Draft3Job job) throws Draft3ExpressionException {
    return getValue(job, KEY_OUTDIR_MAX);
  }

  @JsonIgnore
  public Long getValue(Draft3Job job, String key) throws Draft3ExpressionException {
    Object value = getValue(key);
    value = Draft3ExpressionResolver.resolve(value, job, null);
    if (value == null) {
      return null;
    }
    if (value instanceof Integer) {
      return Long.parseLong(Integer.toString((int) value));
    }
    return (Long) value;
  }
  
  @JsonIgnore
  public Resources build(Draft3Job job) throws Draft3ExpressionException {
    Long coresMin = getCoresMin(job);
    Long coresMax = getCoresMax(job);

    Long cores = coresMin != null ? coresMin : coresMax;
    if (cores == null) {
      cores = CORES_MIN_DEFAULT;
    }

    Long ramMin = getRamMin(job);
    Long ramMax = getRamMax(job);

    Long ram = ramMin != null ? ramMin : ramMax;
    if (ram == null) {
      ram = RAM_MIN_DEFAULT;
    }

    Long tmpdirMin = getTmpdirMin(job);
    Long tmpdirMax = getTmpdirMax(job);

    Long tmpDir = tmpdirMin != null ? tmpdirMin : tmpdirMax;
    if (tmpDir == null) {
      tmpDir = TMPDIR_MIN_DEFAULT;
    }

    Long outdirMin = getOutdirMin(job);
    Long outdirMax = getOutdirMax(job);

    Long outDir = outdirMin != null ? outdirMin : outdirMax;
    if (outDir == null) {
      outDir = OUTDIR_MIN_DEFAULT;
    }
    return new Resources(cores, ram, tmpDir, outDir);
  }
  
  public static class Resources {
    @JsonProperty("cores")
    private final Long cores;
    @JsonProperty("ram")
    private final Long ram;
    @JsonProperty("tmpdir")
    private final Long tmpdir;
    @JsonProperty("outdir")
    private final Long outdir;
    
    public Resources(Long cores, Long ram, Long tmpdir, Long outdir) {
      this.cores = cores;
      this.ram = ram;
      this.tmpdir = tmpdir;
      this.outdir = outdir;
    }
    
    public Long getCores() {
      return cores;
    }
    
    public Long getRam() {
      return ram;
    }

    public Long getTmpdir() {
      return tmpdir;
    }

    public Long getOutdir() {
      return outdir;
    }
    
    @JsonIgnore
    public Map<String, Object> toMap() {
      Map<String, Object> map = new HashMap<>();
      map.put("cores", cores);
      map.put("ram", ram);
      map.put("tmpdir", tmpdir);
      map.put("outdir", outdir);
      return map;
    }
    
  }
  
  
  @Override
  public Draft3ResourceType getType() {
    return Draft3ResourceType.RESOURCE_REQUIREMENT;
  }

}
