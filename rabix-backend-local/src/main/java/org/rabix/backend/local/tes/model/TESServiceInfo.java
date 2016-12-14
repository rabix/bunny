package org.rabix.backend.local.tes.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TESServiceInfo {

  @JsonProperty("storageConfig")
  private Map<String, String> storageConfig;

  @JsonCreator
  public TESServiceInfo(@JsonProperty("storageConfig") Map<String, String> storageConfig) {
    this.storageConfig = storageConfig;
  }
  
  public Map<String, String> getStorageConfig() {
    return storageConfig;
  }
  
  public void setStorageConfig(Map<String, String> storageConfig) {
    this.storageConfig = storageConfig;
  }

  @Override
  public String toString() {
    return "TESServiceInfo [storageConfig=" + storageConfig + "]";
  }
  
}
