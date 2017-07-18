package org.rabix.bindings.model;

import java.util.HashMap;
import java.util.Map;

import org.rabix.bindings.helper.FileValueHelper;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class ApplicationPort {

  public static final String KEY_SCHEMA = "type";

  @JsonProperty("id")
  protected String id;
  
  @JsonProperty("default")
  protected Object defaultValue;
  
  @JsonProperty("type")
  protected Object schema;
  
  @JsonProperty("scatter")
  protected Boolean scatter;
  
  @JsonProperty("linkMerge")
  protected String linkMerge;

  @JsonProperty("description")
  protected String description;

  protected Map<String, Object> raw = new HashMap<>();

  @JsonIgnore
  protected DataType dataType;

  @JsonCreator
  public ApplicationPort(@JsonProperty("id") String id, @JsonProperty("default") Object defaultValue, @JsonProperty("type") Object schema,
                         @JsonProperty("scatter") Boolean scatter, @JsonProperty("linkMerge") String linkMerge, @JsonProperty("description") String description) {
    this.id = id;
    this.schema = schema;
    this.scatter = scatter;
    this.linkMerge = linkMerge;
    this.defaultValue = defaultValue;
    this.description = description;
  }

  @JsonAnySetter
  public void add(String key, Object value) {
    raw.put(key, value);
  }

  @JsonAnyGetter
  public Map<String, Object> getRaw() {
    return raw;
  }

  @JsonIgnore
  public abstract boolean isList();

  @JsonIgnore
  protected void readDataType() {
    dataType = new DataType(DataType.Type.ANY);
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public Object getSchema() {
    return schema;
  }

  public Boolean getScatter() {
    return scatter;
  }

  public void setScatter(Boolean scatter) {
    this.scatter = scatter;
  }

  public Object getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(Object defaultValue) {
    this.defaultValue = defaultValue;
  }
  
  public String getLinkMerge() {
    return linkMerge;
  }
  
  public void setLinkMerge(String linkMerge) {
    this.linkMerge = linkMerge;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @JsonIgnore
  public DataType getDataType() {
    if (dataType==null)
      readDataType();
    return dataType;
  }

  @JsonIgnore
  public boolean isRequired() {
    return false;
  }
  
  public abstract Object getBinding();
  /**
   * Checks if supplied value is valid for this ApplicationPort
   * @param in Potential input value
   * @return null if input is valid else description why input is not valid
   */
  @JsonIgnore
  public String validate(Object in) {
    if (in==null)
      return null;

    DataType inDataType = FileValueHelper.getDataTypeFromValue(in);
    if (!getDataType().isCompatible(inDataType))
      return "Invalid Value for " + id + ".\n Expected: " + getDataType() + ".\n Received: " + inDataType;
    return null;
  }
}
