package org.rabix.backend.tes.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TESResources {

  @JsonProperty("cpu_cores")
  private Integer cpuCores;
  @JsonProperty("preemptible")
  private boolean preemptible;
  @JsonProperty("ram_gb")
  private Double ramGb;
  @JsonProperty("disk_gb")
  private Double diskGb;
  @JsonProperty("zones")
  private String zones;

  @JsonCreator
  public TESResources(@JsonProperty("cpu_cores") Integer cpuCores,
                      @JsonProperty("preemptible") boolean preemptible,
                      @JsonProperty("ram_gb") Double ramGb,
                      @JsonProperty("disk_gb") Double diskGb,
                      @JsonProperty("zones") String zones) {
    this.cpuCores = cpuCores;
    this.preemptible = preemptible;
    this.ramGb = ramGb;
    this.zones = zones;
  }

  public Integer getCpuCores() {
    return cpuCores;
  }

  public void setCpuCores(Integer cpuCores) {
    this.cpuCores = cpuCores;
  }

  public boolean isPreemptible() {
    return preemptible;
  }

  public void setPreemptible(boolean preemptible) {
    this.preemptible = preemptible;
  }

  public Double getRamGb() {
    return ramGb;
  }

  public void setRamGb(Double ramGb) {
    this.ramGb = ramGb;
  }

  public Double getDiskGb() {
    return diskGb;
  }

  public void setDiskGb(Double diskGb) {
    this.diskGb = diskGb;
  }

  public String getZones() {
    return zones;
  }

  public void setZones(String zones) {
    this.zones = zones;
  }

  @Override
  public String toString() {
    return "TESResources [cpuCores=" + cpuCores + ", ramGb=" + ramGb + ", diskGb=" + diskGb + ", preemptible=" + preemptible + ", zones=" + zones + "]";
  }

}
