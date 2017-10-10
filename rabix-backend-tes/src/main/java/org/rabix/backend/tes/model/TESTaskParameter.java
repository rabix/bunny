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
//  @JsonProperty("contents")
//  private String contents;
  
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
    result = prime * result + ((create == null) ? 0 : create.hashCode());
    result = prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + ((location == null) ? 0 : location.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((path == null) ? 0 : path.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    TESTaskParameter other = (TESTaskParameter) obj;
    if (clazz == null) {
      if (other.clazz != null)
        return false;
    } else if (!clazz.equals(other.clazz))
      return false;
    if (create == null) {
      if (other.create != null)
        return false;
    } else if (!create.equals(other.create))
      return false;
    if (description == null) {
      if (other.description != null)
        return false;
    } else if (!description.equals(other.description))
      return false;
    if (location == null) {
      if (other.location != null)
        return false;
    } else if (!location.equals(other.location))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (path == null) {
      if (other.path != null)
        return false;
    } else if (!path.equals(other.path))
      return false;
    return true;
  }
}
