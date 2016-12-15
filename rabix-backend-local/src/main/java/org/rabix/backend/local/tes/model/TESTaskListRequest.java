package org.rabix.backend.local.tes.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TESTaskListRequest {

  @JsonProperty("projectID")
  private String projectId;
  @JsonProperty("namePrefix")
  private String namePrefix;
  @JsonProperty("namePrefix")
  private Integer pageSize;
  @JsonProperty("pageToken")
  private String pageToken;
  
  @JsonCreator
  public TESTaskListRequest(@JsonProperty("projectID") String projectId, @JsonProperty("namePrefix") String namePrefix,
      @JsonProperty("pageSize") Integer pageSize, @JsonProperty("pageToken") String pageToken) {
    this.projectId = projectId;
    this.namePrefix = namePrefix;
    this.pageSize = pageSize;
    this.pageToken = pageToken;
  }

  public String getProjectId() {
    return projectId;
  }

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  public String getNamePrefix() {
    return namePrefix;
  }

  public void setNamePrefix(String namePrefix) {
    this.namePrefix = namePrefix;
  }

  public Integer getPageSize() {
    return pageSize;
  }

  public void setPageSize(Integer pageSize) {
    this.pageSize = pageSize;
  }

  public String getPageToken() {
    return pageToken;
  }

  public void setPageToken(String pageToken) {
    this.pageToken = pageToken;
  }

  @Override
  public String toString() {
    return "TESTaskListRequest [projectId=" + projectId + ", namePrefix=" + namePrefix + ", pageSize=" + pageSize + ", pageToken=" + pageToken + "]";
  }
  
}
