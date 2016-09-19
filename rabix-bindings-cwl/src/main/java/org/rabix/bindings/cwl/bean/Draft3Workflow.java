package org.rabix.bindings.draft3.bean;

import java.util.ArrayList;
import java.util.List;

import org.rabix.common.json.BeanPropertyView;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

public class Draft3Workflow extends Draft3JobApp {

  @JsonProperty("steps")
  private List<Draft3Step> steps;

  @JsonProperty("dataLinks")
  @JsonView(BeanPropertyView.Full.class)
  private List<Draft3DataLink> dataLinks;

  public Draft3Workflow() {
    this.steps = new ArrayList<>();
    this.dataLinks = new ArrayList<>();
  }

  public Draft3Workflow(List<Draft3Step> steps, List<Draft3DataLink> dataLinks) {
    this.steps = steps;
    this.dataLinks = dataLinks;
  }

  @JsonIgnore
  public void addDataLink(Draft3DataLink dataLink) {
    this.dataLinks.add(dataLink);
  }

  @JsonIgnore
  public void addDataLinks(List<Draft3DataLink> dataLinks) {
    this.dataLinks.addAll(dataLinks);
  }

  public List<Draft3Step> getSteps() {
    return steps;
  }

  public List<Draft3DataLink> getDataLinks() {
    return dataLinks;
  }

  @Override
  public String toString() {
    return "Workflow [steps=" + steps + ", dataLinks=" + dataLinks + ", id=" + id + ", context=" + context
        + ", description=" + description + ", inputs=" + getInputs() + ", outputs=" + getOutputs() + ", requirements="
        + requirements + "]";
  }

  @Override
  @JsonIgnore
  public Draft3JobAppType getType() {
    return Draft3JobAppType.WORKFLOW;
  }

}
