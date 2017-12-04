package org.rabix.backend.tes.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TESGetTaskRequest {

  @JsonProperty("id")
  private String id;
  @JsonProperty("view")
  private TESView view;

  @JsonCreator
  public TESGetTaskRequest(@JsonProperty("id") String id, @JsonProperty("view") TESView view) {
    this.id = id;
    this.view = view;
  }
  
  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }

  public TESView getView() {
    return view;
  }

  public void setView(TESView view) {
    this.view = view;
  }

  @Override
  public String toString() {
    return "TESGetTaskRequest [id=" + id + ", view=" + view + "]";
  }
  
}
