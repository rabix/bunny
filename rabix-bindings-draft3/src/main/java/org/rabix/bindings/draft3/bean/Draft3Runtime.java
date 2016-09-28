package org.rabix.bindings.draft3.bean;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class Draft3Runtime {

  @JsonProperty("cores")
  private final Long cores;
  @JsonProperty("ram")
  private final Long ram;
  @JsonProperty("outdir")
  private final String outdir;
  @JsonProperty("tmpdir")
  private final String tmpdir;
  @JsonProperty("outdirSize")
  private final Long outdirSize;
  @JsonProperty("tmpdirSize")
  private final Long tmpdirSize;
  
  
  @JsonCreator
  public Draft3Runtime(@JsonProperty("cores") Long cores, @JsonProperty("ram") Long ram,
      @JsonProperty("outdir") String outdir, @JsonProperty("tmpdir") String tmpdir,
      @JsonProperty("outdirSize") Long tmpDir, @JsonProperty("tmpdirSize") Long outDir) {
    this.cores = cores;
    this.ram = ram;
    this.outdir = outdir;
    this.tmpdir = tmpdir;
    this.outdirSize = tmpDir;
    this.tmpdirSize = outDir;
  }

  public Long getCores() {
    return cores;
  }

  public Long getRam() {
    return ram;
  }

  public String getOutdir() {
    return outdir;
  }

  public String getTmpdir() {
    return tmpdir;
  }

  public Long getOutdirSize() {
    return outdirSize;
  }

  public Long getTmpdirSize() {
    return tmpdirSize;
  }

  @JsonIgnore
  public Map<String, Object> toMap() {
    Map<String, Object> map = new HashMap<>();
    map.put("cores", cores);
    map.put("ram", ram);
    map.put("tmpdir", tmpdir);
    map.put("outdir", outdir);
    map.put("outdirSize", outdirSize);
    map.put("tmpdirSize", tmpdirSize);
    return map;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((cores == null) ? 0 : cores.hashCode());
    result = prime * result + ((ram == null) ? 0 : ram.hashCode());
    result = prime * result + ((outdir == null) ? 0 : outdir.hashCode());
    result = prime * result + ((outdirSize == null) ? 0 : outdirSize.hashCode());
    result = prime * result + ((tmpdir == null) ? 0 : tmpdir.hashCode());
    result = prime * result + ((tmpdirSize == null) ? 0 : tmpdirSize.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Draft3Runtime other = (Draft3Runtime) obj;
    if (cores == null) {
      if (other.cores != null)
        return false;
    } else if (!cores.equals(other.cores))
      return false;
    if (ram == null) {
      if (other.ram != null)
        return false;
    } else if (!ram.equals(other.ram))
      return false;
    if (outdir == null) {
      if (other.outdir != null)
        return false;
    } else if (!outdir.equals(other.outdir))
      return false;
    if (outdirSize == null) {
      if (other.outdirSize != null)
        return false;
    } else if (!outdirSize.equals(other.outdirSize))
      return false;
    if (tmpdir == null) {
      if (other.tmpdir != null)
        return false;
    } else if (!tmpdir.equals(other.tmpdir))
      return false;
    if (tmpdirSize == null) {
      if (other.tmpdirSize != null)
        return false;
    } else if (!tmpdirSize.equals(other.tmpdirSize))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Draft3Runtime [cores=" + cores + ", memMB=" + ram + ", outdir=" + outdir + ", tmpdir=" + tmpdir + ", outdirSize=" + outdirSize + ", tmpdirSize=" + tmpdirSize + "]";
  }

}