package org.rabix.backend.tes.model;

import org.rabix.bindings.model.FileValue.FileType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TESTaskParameter {

  @JsonProperty("name")
  private String name;
  @JsonProperty("description")
  private String description;
  @JsonProperty("url")
  private String location;
  @JsonProperty("path")
  private String path;
  @JsonProperty("type")
  private String clazz;
  @JsonProperty("create")
  private Boolean create;
  
  @JsonCreator
  public TESTaskParameter(@JsonProperty("name") String name, @JsonProperty("description") String description, @JsonProperty("url") String location, @JsonProperty("path") String path, @JsonProperty("type") String clazz, @JsonProperty("create") Boolean create) {
    this.name = name;
    this.description = description;
    this.location = location;
    this.path = path;
    this.clazz = clazz;
    this.create = create;
  }

  public TESTaskParameter(String name, String description, String location, String path, FileType type, boolean create) {
    this(name, description, location, path, type.name().toUpperCase(), create);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  @JsonIgnore
  public String getClazz() {
    return clazz;
  }

  @JsonIgnore
  public void setClazz(String clazz) {
    this.clazz = clazz;
  }

  public Boolean isCreate() {
    return create;
  }

  public void setCreate(Boolean create) {
    this.create = create;
  }

  @Override
  public String toString() {
    return "TESTaskParameter [name=" + name + ", description=" + description + ", location=" + location + ", path=" + path + ", clazz=" + clazz + ", create=" + create + "]";
  }
  
}
