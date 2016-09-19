package org.rabix.bindings.cwl.resolver;

import com.fasterxml.jackson.databind.JsonNode;

public class CWLDocumentResolverReference {

  private boolean isResolving;
  private JsonNode resolvedNode;

  public CWLDocumentResolverReference() {
  }
  
  public CWLDocumentResolverReference(boolean isResolving, JsonNode resolvedNode) {
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
    return "CWLDocumentResolverReference [isResolving=" + isResolving + ", resolvedNode=" + resolvedNode + "]";
  }
  
}
