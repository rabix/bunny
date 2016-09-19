package org.rabix.bindings.draft3.resolver;

import com.fasterxml.jackson.databind.JsonNode;

public class Draft3DocumentResolverReference {

  private boolean isResolving;
  private JsonNode resolvedNode;

  public Draft3DocumentResolverReference() {
  }
  
  public Draft3DocumentResolverReference(boolean isResolving, JsonNode resolvedNode) {
    this.isResolving = isResolving;
    this.resolvedNode = resolvedNode;
  }

  public boolean isResolving() {
    return isResolving;
  }

  public void setResolving(boolean isResolving) {
    this.isResolving = isResolving;
  }

  public JsonNode getResolvedNode() {
    return resolvedNode;
  }

  public void setResolvedNode(JsonNode resolvedNode) {
    this.resolvedNode = resolvedNode;
  }

  @Override
  public String toString() {
    return "Draft3DocumentResolverReference [isResolving=" + isResolving + ", resolvedNode=" + resolvedNode + "]";
  }
  
}
