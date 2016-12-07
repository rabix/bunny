package org.rabix.bindings.draft2.bean;

import java.util.ArrayList;
import java.util.List;

import org.rabix.common.json.BeanPropertyView;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = Draft2Workflow.class)
public class Draft2Workflow extends Draft2JobApp {

  @JsonProperty("steps")
  private List<Draft2Step> steps;

  @JsonProperty("dataLinks")
  @JsonView(BeanPropertyView.Full.class)
  private List<Draft2DataLink> dataLinks;

  public Draft2Workflow() {
    this.steps = new ArrayList<>();
    this.dataLinks = new ArrayList<>();
  }

  public Draft2Workflow(List<Draft2Step> steps, List<Draft2DataLink> dataLinks) {
    this.steps = steps;
    this.dataLinks = dataLinks;
  }

  @JsonIgnore
  public void addDataLink(Draft2DataLink dataLink) {
    this.dataLinks.add(dataLink);
  }

  @JsonIgnore
  public void addDataLinks(List<Draft2DataLink> dataLinks) {
    this.dataLinks.addAll(dataLinks);
  }

  public List<Draft2Step> getSteps() {
    return steps;
  }

  public List<Draft2DataLink> getDataLinks() {
    return dataLinks;
  }

  @Override
  public String toString() {
    return "Workflow [steps=" + steps + ", dataLinks=" + dataLinks + ", id=" + getId() + ", context=" + getContext()
        + ", description=" + getDescription() + ", inputs=" + getInputs() + ", outputs=" + getOutputs() + ", requirements="
        + requirements + "]";
  }

  @Override
  @JsonIgnore
  public Draft2JobAppType getType() {
    return Draft2JobAppType.WORKFLOW;
  }

}
