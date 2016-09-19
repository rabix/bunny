package org.rabix.bindings.draft3.bean;

import org.rabix.bindings.draft3.helper.Draft3SchemaHelper;
import org.rabix.bindings.model.ApplicationPort;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Draft3InputPort extends ApplicationPort {

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
  
  @JsonProperty("format")
  protected Object format;
  @JsonProperty("streamable")
  protected Boolean streamable;
  
  @JsonProperty("sbg:stageInput")
  protected final String stageInput;
  @JsonProperty("inputBinding")
  protected final Object inputBinding;

  @JsonCreator
  public Draft3InputPort(@JsonProperty("id") String id, @JsonProperty("default") Object defaultValue, @JsonProperty("type") Object schema, 
      @JsonProperty("inputBinding") Object inputBinding, @JsonProperty("streamable") Boolean streamable, @JsonProperty("format") Object format, @JsonProperty("scatter") Boolean scatter, @JsonProperty("sbg:stageInput") String stageInput, @JsonProperty("linkMerge") String linkMerge) {
    super(id, defaultValue, schema, scatter, linkMerge);
    this.format = format;
    this.streamable = streamable;
    this.stageInput = stageInput;
    this.inputBinding = inputBinding;
  }

  @Override
  @JsonIgnore
  public boolean isList() {
    return Draft3SchemaHelper.isArrayFromSchema(schema);
  }
  
  public Object getInputBinding() {
    return inputBinding;
  }

  public String getStageInput() {
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
    return "Draft3InputPort [inputBinding=" + inputBinding + ", id=" + getId() + ", schema=" + getSchema() + ", scatter=" + getScatter() + "]";
  }

}
