package org.rabix.bindings.cwl.bean;

import org.rabix.bindings.cwl.helper.CWLSchemaHelper;
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
public class CWLInputPort extends ApplicationPort {
 
  @JsonProperty("format")
  protected Object format;
  @JsonProperty("streamable")
  protected Boolean streamable;
  @JsonProperty("sbg:stageInput")
  protected final StageInput stageInput;
  @JsonProperty("inputBinding")
  protected final Object inputBinding;
  @JsonProperty("secondaryFiles")
  protected Object secondaryFiles;

  @JsonCreator
  public CWLInputPort(@JsonProperty("id") String id, @JsonProperty("default") Object defaultValue, @JsonProperty("type") Object schema, 
      @JsonProperty("inputBinding") Object inputBinding, @JsonProperty("streamable") Boolean streamable, @JsonProperty("format") Object format,
      @JsonProperty("scatter") Boolean scatter, @JsonProperty("sbg:stageInput") StageInput stageInput, @JsonProperty("linkMerge") String linkMerge,
                      @JsonProperty("description") String description, @JsonProperty("secondaryFiles") Object secondaryFiles) {
    super(id, defaultValue, schema, scatter, linkMerge, description);
    this.format = format;
    this.streamable = streamable;
    this.stageInput = stageInput;
    this.inputBinding = inputBinding;
    this.secondaryFiles = secondaryFiles;
  }

  @Override
  @JsonIgnore
  public boolean isList() {
    return CWLSchemaHelper.isArrayFromSchema(schema);
  }
  
  public Object getInputBinding() {
    return inputBinding;
  }

  public Object getSecondaryFiles() {
    return secondaryFiles;
  }

  public StageInput getStageInput() {
    return stageInput;
  }
  
  public Boolean getStreamable() {
    return streamable;
  }
  
  public Object getFormat() {
    return format;
  }
  
  @Override
  public String toString() {
    return "CWLInputPort [inputBinding=" + inputBinding + ", id=" + getId() + ", schema=" + getSchema() + ", scatter=" + getScatter() + "]";
  }
  @Override
  protected void readDataType() {
    dataType = CWLSchemaHelper.readDataType(schema);
  }

  @Override
  public boolean isRequired() {
    return CWLSchemaHelper.isRequired(schema);
  }

  @Override
  @JsonIgnore
  public Object getBinding() {
    return inputBinding;
  }

}
