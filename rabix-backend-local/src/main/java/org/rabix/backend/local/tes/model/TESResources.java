package org.rabix.backend.local.tes.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TESResources {

  @JsonProperty("minimumCpuCores")
  private Integer minimumCpuCores;
  @JsonProperty("preemptible")
  private boolean preemptible;
  @JsonProperty("minimumRamGb")
  private Integer minimumRamGb;
  @JsonProperty("volumes")
  private List<TESVolume> volumes;
  @JsonProperty("zones")
  private String zones;

  @JsonCreator
  public TESResources(@JsonProperty("minimumCpuCores") Integer minimumCpuCores,
      @JsonProperty("preemptible") boolean preemptible, @JsonProperty("minimumRamGb") Integer minimumRamGb,
      @JsonProperty("volumes") List<TESVolume> volumes, @JsonProperty("zones") String zones) {
    this.minimumCpuCores = minimumCpuCores;
    this.preemptible = preemptible;
    this.minimumRamGb = minimumRamGb;
    this.volumes = volumes;
    this.zones = zones;
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

  public Integer getMinimumRamGb() {
    return minimumRamGb;
  }

  public void setMinimumRamGb(Integer minimumRamGb) {
    this.minimumRamGb = minimumRamGb;
  }

  public List<TESVolume> getVolumes() {
    return volumes;
  }

  public void setVolumes(List<TESVolume> volumes) {
    this.volumes = volumes;
  }

  public String getZones() {
    return zones;
  }

  public void setZones(String zones) {
    this.zones = zones;
  }

  @Override
  public String toString() {
    return "TESResources [minimumCpuCores=" + minimumCpuCores + ", preemptible=" + preemptible + ", minimumRamGb=" + minimumRamGb + ", volumes=" + volumes + ", zones=" + zones + "]";
  }

}
