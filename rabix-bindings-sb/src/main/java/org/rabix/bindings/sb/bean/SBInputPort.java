package org.rabix.bindings.sb.bean;

import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.StageInput;
import org.rabix.bindings.sb.helper.SBSchemaHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SBInputPort extends ApplicationPort {

  @JsonProperty("inputBinding")
  protected final Object inputBinding;
  @JsonProperty("sbg:stageInput")
  protected final StageInput stageInput;

  @JsonCreator
  public SBInputPort(@JsonProperty("id") String id, @JsonProperty("default") Object defaultValue, @JsonProperty("type") Object schema,
      @JsonProperty("inputBinding") Object inputBinding, @JsonProperty("scatter") Boolean scatter, @JsonProperty("sbg:stageInput") StageInput stageInput, @JsonProperty("linkMerge") String linkMerge,
                     @JsonProperty("description") String description) {
    super(id, defaultValue, schema, scatter, linkMerge, description);
    this.stageInput = stageInput;
    this.inputBinding = inputBinding;
  }

  @Override
  @JsonIgnore
  public boolean isList() {
    return SBSchemaHelper.isArrayFromSchema(schema);
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
    dataType = SBSchemaHelper.readDataType(schema);
  }

  @Override
  public boolean isRequired() {
    return SBSchemaHelper.isRequired(schema);
  }

  @JsonIgnore
  @Override
  public Object getBinding() {
    return inputBinding;
  }
}
