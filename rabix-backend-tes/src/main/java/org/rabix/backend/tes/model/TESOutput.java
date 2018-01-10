package org.rabix.backend.tes.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TESOutput {

  @JsonProperty("name")
  private String name;
  @JsonProperty("description")
  private String description;
  @JsonProperty("url")
  private String location;
  @JsonProperty("path")
  private String path;
  @JsonProperty("type")
  private TESFileType type;

  @JsonCreator
  public TESOutput(@JsonProperty("name") String name,
                          @JsonProperty("description") String description,
                          @JsonProperty("url") String location,
                          @JsonProperty("path") String path,
                          @JsonProperty("type") TESFileType type) {
    this.name = name;
    this.description = description;
    this.location = location;
    this.path = path;
    this.type = type;
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

  public TESFileType getType() {
    return type;
  }

  public void setType(TESFileType type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return "Output [name=" + name + ", description=" + description + ", location=" + location + ", path=" + path + ", type=" + type + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((type == null) ? 0 : type.hashCode());
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
    TESOutput other = (TESOutput) obj;
    if (type == null) {
      if (other.type != null)
        return false;
    } else if (!type.equals(other.type))
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
