package org.rabix.bindings.model.requirement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ResourceRequirement extends Requirement {

  @JsonProperty("cpuMin")
  private final Long cpuMin;
  @JsonProperty("cpuRecommended")
  private final Long cpuRecommended;
  @JsonProperty("memMinMB")
  private final Long memMinMB;
  @JsonProperty("memRecommendedMB")
  private final Long memRecommendedMB;
  @JsonProperty("diskSpaceMinMB")
  private final Long diskSpaceMinMB;
  @JsonProperty("diskSpaceRecommendedMB")
  private final Long diskSpaceRecommendedMB;
  @JsonProperty("networkAccess")
  private final Boolean networkAccess;

  @JsonCreator
  public ResourceRequirement(@JsonProperty("cpuMin") Long cpuMin,
      @JsonProperty("cpuRecommended") Long cpuRecommended, @JsonProperty("memMinMB") Long memMinMB,
      @JsonProperty("memRecommendedMB") Long memRecommendedMB,
      @JsonProperty("diskSpaceMinMB") Long diskSpaceMinMB,
      @JsonProperty("diskSpaceRecommendedMB") Long diskSpaceRecommendedMB,
      @JsonProperty("networkAccess") Boolean networkAccess) {
    this.cpuMin = cpuMin;
    this.cpuRecommended = cpuRecommended;
    this.memMinMB = memMinMB;
    this.memRecommendedMB = memRecommendedMB;
    this.diskSpaceMinMB = diskSpaceMinMB;
    this.diskSpaceRecommendedMB = diskSpaceRecommendedMB;
    this.networkAccess = networkAccess;
  }

  public Long getCpuMin() {
    return cpuMin;
  }

  public Long getCpuRecommended() {
    return cpuRecommended;
  }

  public Long getMemMinMB() {
    return memMinMB;
  }

  public Long getMemRecommendedMB() {
    return memRecommendedMB;
  }

  public Long getDiskSpaceMinMB() {
    return diskSpaceMinMB;
  }

  public Long getDiskSpaceRecommendedMB() {
    return diskSpaceRecommendedMB;
  }

  public Boolean getNetworkAccess() {
    return networkAccess;
  }

  @Override
  public boolean isCustom() {
    return false;
  }

  @Override
  public Object getData() {
    return null;
  }

  @Override
  public String getType() {
    return RESOURCE_REQUIREMENT_TYPE;
  }

  @Override
  public String toString() {
    return "ResourceRequirement [cpuMin=" + cpuMin + ", cpuRecommended=" + cpuRecommended + ", memMinMB=" + memMinMB
        + ", memRecommendedMB=" + memRecommendedMB + ", diskSpaceMinMB=" + diskSpaceMinMB + ", diskSpaceRecommendedMB="
        + diskSpaceRecommendedMB + ", networkAccess=" + networkAccess + "]";
  }
}
