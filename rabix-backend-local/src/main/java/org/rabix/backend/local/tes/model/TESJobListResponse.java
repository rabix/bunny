package org.rabix.backend.local.tes.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TESJobListResponse {

  @JsonProperty("jobs")
  private List<TESJob> jobs;
  @JsonProperty("nextPageToken")
  private String nextPageToken;
  
  @JsonCreator
  public TESJobListResponse(@JsonProperty("jobs") List<TESJob> jobs, @JsonProperty("nextPageToken") String nextPageToken) {
    super();
    this.jobs = jobs;
    this.nextPageToken = nextPageToken;
  }

  public List<TESJob> getJobs() {
    return jobs;
  }

  public void setJobs(List<TESJob> jobs) {
    this.jobs = jobs;
  }

  public String getNextPageToken() {
    return nextPageToken;
  }

  public void setNextPageToken(String nextPageToken) {
    this.nextPageToken = nextPageToken;
  }

  @Override
  public String toString() {
    return "TESJobListResponse [jobs=" + jobs + ", nextPageToken=" + nextPageToken + "]";
  }

}
