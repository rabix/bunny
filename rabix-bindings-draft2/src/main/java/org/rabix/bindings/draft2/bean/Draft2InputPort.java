package org.rabix.bindings.draft2.bean;

import org.rabix.bindings.draft2.helper.Draft2SchemaHelper;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.StageInput;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Draft2InputPort extends ApplicationPort {

  @JsonProperty("inputBinding")
  protected final Object inputBinding;
  @JsonProperty("sbg:stageInput")
  protected final StageInput stageInput;

  @JsonCreator
  public Draft2InputPort(@JsonProperty("id") String id, @JsonProperty("default") Object defaultValue, @JsonProperty("type") Object schema, 
      @JsonProperty("inputBinding") Object inputBinding, @JsonProperty("scatter") Boolean scatter, @JsonProperty("sbg:stageInput") StageInput stageInput, @JsonProperty("linkMerge") String linkMerge,
                         @JsonProperty("description") String description) {
    super(id, defaultValue, schema, scatter, linkMerge, description);
    this.stageInput = stageInput;
    this.inputBinding = inputBinding;
  }

  @Override
  @JsonIgnore
  public boolean isList() {
    return Draft2SchemaHelper.isArrayFromSchema(schema);
  }
  
  public Object getInputBinding() {
    return inputBinding;
  }

  public StageInput getStageInput() {
    return stageInput;
  }
  
  @Override
  public String toString() {
    return "InputPort [inputBinding=" + inputBinding + ", id=" + getId() + ", schema=" + getSchema() + ", scatter=" + getScatter() + "]";
  }

  @Override
  protected void readDataType() {
    dataType = Draft2SchemaHelper.readDataType(schema);
  }

  @Override
  public boolean isRequired() {
    return Draft2SchemaHelper.isRequired(schema);
  }
  
  @JsonIgnore
  @Override
  public Object getBinding() {
   return inputBinding;
  }
}
