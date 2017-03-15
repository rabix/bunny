package org.rabix.bindings.model.dag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.model.Application;
import org.rabix.bindings.model.ScatterMethod;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DAGContainer extends DAGNode {

  @JsonProperty("links")
  private List<DAGLink> links = new ArrayList<>();
  @JsonProperty("children")
  private List<DAGNode> children = new ArrayList<>();

  @JsonCreator
  public DAGContainer(@JsonProperty("id") String id, @JsonProperty("inputPorts") List<DAGLinkPort> inputPorts, @JsonProperty("outputPorts") List<DAGLinkPort> outputPorts, @JsonProperty("app") Application app, @JsonProperty("scatterMethod") ScatterMethod scatterMethod, @JsonProperty("links") List<DAGLink> links, @JsonProperty("children") List<DAGNode> children, @JsonProperty("defaults") Map<String, Object> defaults) {
    super(id, inputPorts, outputPorts, scatterMethod, app, defaults);
    this.links = links;
    this.children = children;
  }

  public List<DAGNode> getChildren() {
    return children;
  }

  public List<DAGLink> getLinks() {
    return links;
  }

  @Override
  public DAGNodeType getType() {
    return DAGNodeType.CONTAINER;
  }
  
  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "DAGContainer [links=" + links + ", children=" + children + ", id=" + id + ", scatterMethod=" + scatterMethod + ", inputPorts=" + inputPorts + ", outputPorts=" + outputPorts + "]";
  }

}
