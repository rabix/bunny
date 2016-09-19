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

  @JsonProperty("outputBinding")
  protected Object outputBinding;
  @JsonProperty("source")
  protected Object source;

  @JsonCreator
  public Draft3OutputPort(@JsonProperty("id") String id, @JsonProperty("default") Object defaultValue,
      @JsonProperty("type") Object schema, @JsonProperty("outputBinding") Object outputBinding,
      @JsonProperty("scatter") Boolean scatter, @JsonProperty("source") Object source, @JsonProperty("linkMerge") String linkMerge) {
    super(id, defaultValue, schema, scatter, linkMerge);
    this.outputBinding = outputBinding;
    this.source = source;
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

  @Override
  public String toString() {
    return "OutputPort [outputBinding=" + outputBinding + ", id=" + getId() + ", schema=" + getSchema() + ", scatter="
        + getScatter() + ", source=" + source + "]";
  }

}
