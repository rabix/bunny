package org.rabix.backend.local.tes.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TESVolume {

  @JsonProperty("name")
  private String name;
  @JsonProperty("sizeGb")
  private Integer sizeGb;
  @JsonProperty("source")
  private String source;
  @JsonProperty("mountPoint")
  private String mountPoint;
  
  @JsonCreator
  public TESVolume(@JsonProperty("name") String name, @JsonProperty("sizeGb") Integer sizeGb, @JsonProperty("source")  String source, @JsonProperty("mountPoint") String mountPoint) {
    this.name = name;
    this.sizeGb = sizeGb;
    this.source = source;
    this.mountPoint = mountPoint;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getSizeGb() {
    return sizeGb;
  }

  public void setSizeGb(Integer sizeGb) {
    this.sizeGb = sizeGb;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getMountPoint() {
    return mountPoint;
  }

  public void setMountPoint(String mountPoint) {
    this.mountPoint = mountPoint;
  }

  @Override
  public String toString() {
    return "TESVolume [name=" + name + ", sizeGb=" + sizeGb + ", source=" + source + ", mountPoint=" + mountPoint + "]";
  }
 
}
