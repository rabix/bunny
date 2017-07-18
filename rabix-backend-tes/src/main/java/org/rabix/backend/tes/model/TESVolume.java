package org.rabix.backend.tes.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TESVolume {

  @JsonProperty("name")
  private String name;
  @JsonProperty("sizeGb")
  private Double sizeGb;
  @JsonProperty("source")
  private String source;
  @JsonProperty("mountPoint")
  private String mountPoint;
  @JsonProperty("readonly")
  private boolean readonly;
  
  @JsonCreator
  public TESVolume(@JsonProperty("name") String name, @JsonProperty("sizeGb") Double sizeGb, @JsonProperty("source")  String source, @JsonProperty("mountPoint") String mountPoint, @JsonProperty("readonly") boolean readonly) {
    this.name = name;
    this.sizeGb = sizeGb;
    this.source = source;
    this.mountPoint = mountPoint;
    this.readonly = readonly;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Double getSizeGb() {
    return sizeGb;
  }

  public void setSizeGb(Double sizeGb) {
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

  public boolean getReadonly() {
    return readonly;
  }

  public void setReadonly(boolean readonly) {
    this.readonly = readonly;
  }

  @Override
  public String toString() {
    return "TESVolume [name=" + name + ", sizeGb=" + sizeGb + ", source=" + source + ", mountPoint=" + mountPoint + ", readonly=" + readonly + "]";
  }
 
}
