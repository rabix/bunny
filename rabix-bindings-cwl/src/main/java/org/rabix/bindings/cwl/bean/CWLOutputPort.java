package org.rabix.bindings.cwl.bean;

import org.rabix.bindings.cwl.helper.CWLSchemaHelper;
import org.rabix.bindings.model.ApplicationPort;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CWLOutputPort extends ApplicationPort {

  @JsonProperty("format")
  protected Object format;
  @JsonProperty("outputBinding")
  protected Object outputBinding;
  @JsonProperty("outputSource")
  protected Object source;
  @JsonProperty("secondaryFiles")
  protected Object secondaryFiles;
  
  @JsonCreator
  public CWLOutputPort(@JsonProperty("id") String id, @JsonProperty("format") Object format,  @JsonProperty("default") Object defaultValue,
      @JsonProperty("type") Object schema, @JsonProperty("outputBinding") Object outputBinding, @JsonProperty("scatter") Boolean scatter, 
      @JsonProperty("outputSource") Object source, @JsonProperty("linkMerge") String linkMerge, @JsonProperty("secondaryFiles") Object secondaryFiles, @JsonProperty("description") String description) {
    super(id, defaultValue, schema, scatter, linkMerge, description);
    this.format = format;
    this.outputBinding = outputBinding;
    this.source = source;
    this.secondaryFiles = secondaryFiles;
  }

  @Override
  @JsonIgnore
  public boolean isList() {
    return CWLSchemaHelper.isArrayFromSchema(schema);
  }
  
  public Object getFormat() {
    return format;
  }
  
  public Object getOutputBinding() {
    return outputBinding;
  }

  public Object getSecondaryFiles() {
    return secondaryFiles;
  }
  
  public Object getSource() {
    return source;
  }

  @Override
  public String toString() {
    return "OutputPort [outputBinding=" + outputBinding + ", id=" + getId() + ", schema=" + getSchema() + ", scatter=" + getScatter() + ", source=" + source + "]";
  }

  @JsonIgnore
  @Override
  public Object getBinding() {
    return outputBinding;
  }
}
