package org.rabix.bindings.draft3.bean;

import org.rabix.bindings.draft3.helper.Draft3SchemaHelper;
import org.rabix.bindings.model.ApplicationPort;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Draft3OutputPort extends ApplicationPort {

  @JsonProperty("format")
  protected Object format;
  @JsonProperty("outputBinding")
  protected Object outputBinding;
  @JsonProperty("secondaryFiles")
  protected Object secondaryFiles;
  @JsonProperty("source")
  protected Object source;

  @JsonCreator
  public Draft3OutputPort(@JsonProperty("id") String id, @JsonProperty("format") Object format, @JsonProperty("default") Object defaultValue,
      @JsonProperty("type") Object schema, @JsonProperty("outputBinding") Object outputBinding,
      @JsonProperty("scatter") Boolean scatter, @JsonProperty("source") Object source, @JsonProperty("secondaryFiles") Object secondaryFiles, @JsonProperty("linkMerge") String linkMerge, @JsonProperty("description") String description) {
    super(id, defaultValue, schema, scatter, linkMerge, description);
    this.format = format;
    this.outputBinding = outputBinding;
    this.source = source;
    this.secondaryFiles = secondaryFiles;
  }

  @Override
  @JsonIgnore
  public boolean isList() {
    return Draft3SchemaHelper.isArrayFromSchema(schema);
  }
  
  public Object getOutputBinding() {
    return outputBinding;
  }

  public Object getSource() {
    return source;
  }
  
  public Object getSecondaryFiles() {
    return secondaryFiles;
  }
  
  public Object getFormat() {
    return format;
  }

  @Override
  public String toString() {
    return "OutputPort [outputBinding=" + outputBinding + ", id=" + getId() + ", schema=" + getSchema() + ", scatter="
        + getScatter() + ", source=" + source + "]";
  }
  @JsonIgnore
  @Override
  public Object getBinding() {
    return outputBinding;
  }

}
