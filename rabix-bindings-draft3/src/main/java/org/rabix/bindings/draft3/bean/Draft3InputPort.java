package org.rabix.bindings.draft3.bean;

import org.rabix.bindings.draft3.helper.Draft3SchemaHelper;
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
public class Draft3InputPort extends ApplicationPort {

  @JsonProperty("format")
  protected Object format;
  @JsonProperty("streamable")
  protected Boolean streamable;
  @JsonProperty("sbg:stageInput")
  protected final StageInput stageInput;
  
  @JsonProperty("inputBinding")
  protected final Object inputBinding;

  @JsonCreator
  public Draft3InputPort(@JsonProperty("id") String id, @JsonProperty("default") Object defaultValue, @JsonProperty("type") Object schema, 
      @JsonProperty("inputBinding") Object inputBinding, @JsonProperty("streamable") Boolean streamable, @JsonProperty("format") Object format, @JsonProperty("scatter") Boolean scatter, @JsonProperty("linkMerge") String linkMerge, @JsonProperty("description") String description, 
      @JsonProperty("sbg:stageInput") StageInput stageInput) {
    super(id, defaultValue, schema, scatter, linkMerge, description);
    this.format = format;
    this.streamable = streamable;
    this.inputBinding = inputBinding;
    this.stageInput = stageInput;
  }

  @Override
  @JsonIgnore
  public boolean isList() {
    return Draft3SchemaHelper.isArrayFromSchema(schema);
  }
  
  public Object getInputBinding() {
    return inputBinding;
  }
  
  public Boolean getStreamable() {
    return streamable;
  }
  
  public Object getFormat() {
    return format;
  }
  
  @Override
  public String toString() {
    return "Draft3InputPort [inputBinding=" + inputBinding + ", id=" + getId() + ", schema=" + getSchema() + ", scatter=" + getScatter() + "]";
  }

  @Override
  protected void readDataType() {
    dataType = Draft3SchemaHelper.readDataType(schema);
  }

  public StageInput getStageInput() {
    return stageInput;
  }
  
  @Override
  public boolean isRequired() {
    return Draft3SchemaHelper.isRequired(schema);
  }

  @JsonIgnore
  @Override
  public Object getBinding() {
    return inputBinding;
  }
}
