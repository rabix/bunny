package org.rabix.bindings.draft2.bean;

import org.rabix.bindings.draft2.helper.Draft2SchemaHelper;
import org.rabix.bindings.model.ApplicationPort;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import org.rabix.bindings.model.DataType;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Draft2InputPort extends ApplicationPort {

  public static enum StageInput {
    COPY("copy"), LINK("link");
    
    private String value;
    
    private StageInput(String value) {
      this.value = value;
    }
    
    public static StageInput get(String value) {
      Preconditions.checkNotNull(value);
      for (StageInput stageInput : values()) {
        if (value.compareToIgnoreCase(stageInput.value) == 0) {
          return stageInput;
        }
      }
      throw new IllegalArgumentException("Wrong stageInput value " + value);
    }
  }
  
  @JsonProperty("inputBinding")
  protected final Object inputBinding;
  @JsonProperty("sbg:stageInput")
  protected final String stageInput;

  @JsonCreator
  public Draft2InputPort(@JsonProperty("id") String id, @JsonProperty("default") Object defaultValue, @JsonProperty("type") Object schema, 
      @JsonProperty("inputBinding") Object inputBinding, @JsonProperty("scatter") Boolean scatter, @JsonProperty("sbg:stageInput") String stageInput, @JsonProperty("linkMerge") String linkMerge,
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

  public String getStageInput() {
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

  @Override
  public DataType getDataTypeFromValue(Object input) {
    return Draft2SchemaHelper.getDataTypeFromValue(input);
  }
}
