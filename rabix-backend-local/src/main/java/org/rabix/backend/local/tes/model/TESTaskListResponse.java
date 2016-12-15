package org.rabix.backend.local.tes.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TESTaskListResponse {

  @JsonProperty("tasks")
  private List<TESTask> tasks;
  @JsonProperty("nextPageToken")
  private String nextPageToken;
  
  public TESTaskListResponse(@JsonProperty("tasks") List<TESTask> tasks, @JsonProperty("nextPageToken") String nextPageToken) {
    this.tasks = tasks;
    this.nextPageToken = nextPageToken;
  }

  public List<TESTask> getTasks() {
    return tasks;
  }

  public void setTasks(List<TESTask> tasks) {
    this.tasks = tasks;
  }

  public String getNextPageToken() {
    return nextPageToken;
  }

  public void setNextPageToken(String nextPageToken) {
    this.nextPageToken = nextPageToken;
  }

  @Override
  public String toString() {
    return "TESTaskListResponse [tasks=" + tasks + ", nextPageToken=" + nextPageToken + "]";
  }
  
}
