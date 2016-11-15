package org.rabix.bindings.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Resources {

  @JsonProperty("cpu")
  private final Long cpu;
  @JsonProperty("memMB")
  private final Long memMB;
  @JsonProperty("diskSpaceMB")
  private final Long diskSpaceMB;
  @JsonProperty("networkAccess")
  private final Boolean networkAccess;
  @JsonProperty("workingDir")
  private final String workingDir;
  @JsonProperty("tmpDir")
  private final String tmpDir;
  @JsonProperty("outDirSize")
  private final Long outDirSize;
  @JsonProperty("tmpDirSize")
  private final Long tmpDirSize;


  @JsonCreator
  public Resources(@JsonProperty("cpu") Long cpu, @JsonProperty("memMB") Long memMB,
      @JsonProperty("diskSpaceMB") Long diskSpaceMB, @JsonProperty("networkAccess") Boolean networkAccess,
      @JsonProperty("workingDir") String workingDir, @JsonProperty("tmpDir") String tmpDir,
      @JsonProperty("outDirSize") Long outDirSize, @JsonProperty("tmpDirSize") Long tmpDirSize) {
    this.cpu = cpu;
    this.memMB = memMB;
    this.diskSpaceMB = diskSpaceMB;
    this.networkAccess = networkAccess;
    this.workingDir = workingDir;
    this.tmpDir = tmpDir;
    this.outDirSize = outDirSize;
    this.tmpDirSize = tmpDirSize;
  }

  public Long getCpu() {
    return cpu;
  }

  public Long getMemMB() {
    return memMB;
  }

  public Long getDiskSpaceMB() {
    return diskSpaceMB;
  }

  public Boolean getNetworkAccess() {
    return networkAccess;
  }

  public String getWorkingDir() {
    return workingDir;
  }

  public String getTmpDir() {
    return tmpDir;
  }

  public Long getOutDirSize() {
    return outDirSize;
  }

  public Long getTmpDirSize() {
    return tmpDirSize;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((cpu == null) ? 0 : cpu.hashCode());
    result = prime * result + ((diskSpaceMB == null) ? 0 : diskSpaceMB.hashCode());
    result = prime * result + ((memMB == null) ? 0 : memMB.hashCode());
    result = prime * result + ((networkAccess == null) ? 0 : networkAccess.hashCode());
    result = prime * result + ((tmpDir == null) ? 0 : tmpDir.hashCode());
    result = prime * result + ((workingDir == null) ? 0 : workingDir.hashCode());
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
    Resources other = (Resources) obj;
    if (cpu == null) {
      if (other.cpu != null)
        return false;
    } else if (!cpu.equals(other.cpu))
      return false;
    if (diskSpaceMB == null) {
      if (other.diskSpaceMB != null)
        return false;
    } else if (!diskSpaceMB.equals(other.diskSpaceMB))
      return false;
    if (memMB == null) {
      if (other.memMB != null)
        return false;
    } else if (!memMB.equals(other.memMB))
      return false;
    if (networkAccess == null) {
      if (other.networkAccess != null)
        return false;
    } else if (!networkAccess.equals(other.networkAccess))
      return false;
    if (tmpDir == null) {
      if (other.tmpDir != null)
        return false;
    } else if (!tmpDir.equals(other.tmpDir))
      return false;
    if (workingDir == null) {
      if (other.workingDir != null)
        return false;
    } else if (!workingDir.equals(other.workingDir))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Resources [cpu=" + cpu + ", memMB=" + memMB + ", diskSpaceMB=" + diskSpaceMB + ", networkAccess="
        + networkAccess + ", workingDir=" + workingDir + ", tmpDir=" + tmpDir + "]";
  }

}
