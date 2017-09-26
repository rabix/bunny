package org.rabix.backend.tes.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TESResources {

  @JsonProperty("cpu_cores")
  private Integer minimumCpuCores;
  @JsonProperty("preemptible")
  private boolean preemptible;
  @JsonProperty("ram_gb")
  private Double minimumRamGb;
  @JsonProperty("size_gb")
  private Double disk;
  // @JsonProperty("volumes")
  // private List<TESVolume> volumes;
  @JsonProperty("zones")
  private String zones;

  @JsonCreator
  public TESResources(@JsonProperty("cpu_cores") Integer minimumCpuCores, @JsonProperty("preemptible") boolean preemptible,
      @JsonProperty("ram_gb") Double minimumRamGb, @JsonProperty("size_gb") Double disk, @JsonProperty("zones") String zones) {
    this.minimumCpuCores = minimumCpuCores;
    this.preemptible = preemptible;
    this.minimumRamGb = minimumRamGb;
    this.zones = zones;
  }

  public Double getDisk() {
    return disk;
  }

  public void setDisk(Double disk) {
    this.disk = disk;
  }

  public Integer getMinimumCpuCores() {
    return minimumCpuCores;
  }

  public void setMinimumCpuCores(Integer minimumCpuCores) {
    this.minimumCpuCores = minimumCpuCores;
  }

  public boolean isPreemptible() {
    return preemptible;
  }

  public void setPreemptible(boolean preemptible) {
    this.preemptible = preemptible;
  }

  public Double getMinimumRamGb() {
    return minimumRamGb;
  }

  public void setMinimumRamGb(Double minimumRamGb) {
    this.minimumRamGb = minimumRamGb;
  }

  public String getZones() {
    return zones;
  }

  public void setZones(String zones) {
    this.zones = zones;
  }

  @Override
  public String toString() {
    return "TESResources [minimumCpuCores=" + minimumCpuCores + ", preemptible=" + preemptible + ", minimumRamGb=" + minimumRamGb + ", zones=" + zones + "]";
  }

}
