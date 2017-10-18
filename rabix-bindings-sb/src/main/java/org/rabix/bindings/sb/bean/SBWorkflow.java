package org.rabix.bindings.sb.bean;

import java.util.ArrayList;
import java.util.List;

import org.rabix.common.json.BeanPropertyView;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = SBWorkflow.class)
public class SBWorkflow extends SBJobApp {

  @JsonProperty("steps")
  private List<SBStep> steps;

  @JsonProperty("dataLinks")
  @JsonView(BeanPropertyView.Full.class)
  private List<SBDataLink> dataLinks;

  public SBWorkflow() {
    super();
    this.steps = new ArrayList<>();
    this.dataLinks = new ArrayList<>();
  }

  public SBWorkflow(List<SBStep> steps, List<SBDataLink> dataLinks) {
    this.steps = steps;
    this.dataLinks = dataLinks;
  }

  @JsonIgnore
  public void addDataLink(SBDataLink dataLink) {
    this.dataLinks.add(dataLink);
  }

  @JsonIgnore
  public void addDataLinks(List<SBDataLink> dataLinks) {
    this.dataLinks.addAll(dataLinks);
  }

  public List<SBStep> getSteps() {
    return steps;
  }

  public List<SBDataLink> getDataLinks() {
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
  public SBJobAppType getType() {
    return SBJobAppType.WORKFLOW;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((dataLinks == null) ? 0 : dataLinks.hashCode());
    result = prime * result + ((steps == null) ? 0 : steps.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    SBWorkflow other = (SBWorkflow) obj;
    if (dataLinks == null) {
      if (other.dataLinks != null)
        return false;
    } else if (!dataLinks.equals(other.dataLinks))
      return false;
    if (steps == null) {
      if (other.steps != null)
        return false;
    } else if (!steps.equals(other.steps))
      return false;
    return true;
  }

}
