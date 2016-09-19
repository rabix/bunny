package org.rabix.bindings.draft3.bean;

import org.rabix.bindings.model.LinkMerge;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Draft3DataLink {

  @JsonProperty("source")
  private String source;

  @JsonProperty("destination")
  private String destination;

  @JsonProperty("position")
  private Integer position;

  @JsonIgnore
  private Boolean scattered;
  
  @JsonProperty("linkMerge")
  private LinkMerge linkMerge;

  @JsonCreator
  public Draft3DataLink(@JsonProperty("source") String source, @JsonProperty("destination") String destination, @JsonProperty("linkMerge") LinkMerge linkMerge,
      @JsonProperty("position") Integer position) {
    this.source = source;
    this.destination = destination;
    this.source = source;
    this.destination = destination;
    this.position = position;
    this.linkMerge = linkMerge;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }
  
  public LinkMerge getLinkMerge() {
    return linkMerge;
  }
  
  public void setLinkMerge(LinkMerge linkMerge) {
    this.linkMerge = linkMerge;
  }

  public String getDestination() {
    return destination;
  }

  public void setDestination(String destination) {
    this.destination = destination;
  }

  public Integer getPosition() {
    return position;
  }

  public Boolean getScattered() {
    return scattered;
  }

  public void setScattered(Boolean scattered) {
    this.scattered = scattered;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((destination == null) ? 0 : destination.hashCode());
    result = prime * result + ((position == null) ? 0 : position.hashCode());
    result = prime * result + ((source == null) ? 0 : source.hashCode());
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
    Draft3DataLink other = (Draft3DataLink) obj;
    if (destination == null) {
      if (other.destination != null)
        return false;
    } else if (!destination.equals(other.destination))
      return false;
    if (position == null) {
      if (other.position != null)
        return false;
    } else if (!position.equals(other.position))
      return false;
    if (source == null) {
      if (other.source != null)
        return false;
    } else if (!source.equals(other.source))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Draft3DataLink [source=" + source + ", destination=" + destination + ", position=" + position + ", scattered=" + scattered + ", linkMerge=" + linkMerge + "]";
  }

}
