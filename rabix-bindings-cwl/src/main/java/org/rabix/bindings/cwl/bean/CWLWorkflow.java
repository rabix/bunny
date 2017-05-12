package org.rabix.bindings.cwl.bean;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.rabix.bindings.cwl.json.CWLStepsDeserializer;
import org.rabix.bindings.model.ValidationReport;
import org.rabix.common.json.BeanPropertyView;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = CWLWorkflow.class)
public class CWLWorkflow extends CWLJobApp {

  @JsonProperty("steps")
  @JsonDeserialize(using = CWLStepsDeserializer.class)
  private List<CWLStep> steps;

  @JsonProperty("dataLinks")
  @JsonView(BeanPropertyView.Full.class)
  private List<CWLDataLink> dataLinks;

  public CWLWorkflow() {
    this.steps = new ArrayList<>();
    this.dataLinks = new ArrayList<>();
  }

  public CWLWorkflow(List<CWLStep> steps, List<CWLDataLink> dataLinks) {
    this.steps = steps;
    this.dataLinks = dataLinks;
  }

  @JsonIgnore
  public void addDataLink(CWLDataLink dataLink) {
    this.dataLinks.add(dataLink);
  }

  @JsonIgnore
  public void addDataLinks(List<CWLDataLink> dataLinks) {
    this.dataLinks.addAll(dataLinks);
  }

  public List<CWLStep> getSteps() {
    return steps;
  }

  public List<CWLDataLink> getDataLinks() {
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
  public CWLJobAppType getType() {
    return CWLJobAppType.WORKFLOW;
  }

  private Set<String> checkStepDuplicates() {
    Set<String> duplicates = new HashSet<>();
    Set<String> ids = new HashSet<>();
    for (CWLStep step : steps) {
      if (!ids.add(step.getId())) {
        duplicates.add(step.getId());
      }
    }
    return duplicates;
  }

  @Override
  public ValidationReport validate() {
    List<String> errors = new ArrayList<>();
    List<String> warnings = new ArrayList<>();
    errors.addAll(validatePortUniqueness());
    for (String duplicate : checkStepDuplicates()) {
      errors.add("Duplicate step id: " + duplicate);
    }
    for (CWLStep step : steps) {
      for (String msg : step.getApp().validate().getErrors()) {
        errors.add("Invalid app in step '" + step.getId() + "': " + msg);
      }
      for (String msg : step.getApp().validate().getErrors()) {
        warnings.add("Warning from app in step '" + step.getId() + "': " + msg);
      }
    }

    return new ValidationReport(errors, warnings);
  }
}
