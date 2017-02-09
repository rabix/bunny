package org.rabix.bindings.helper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.model.dag.DAGContainer;
import org.rabix.bindings.model.dag.DAGLink;
import org.rabix.bindings.model.dag.DAGNode;

public class DAGValidationHelper {

  public static void detectLoop(final DAGNode dagNode) throws BindingException {
    if (!(dagNode instanceof DAGContainer)) {
      return;
    }
    DAGContainer containerNode = (DAGContainer) dagNode;
    Set<DAGNode> marked = new HashSet<DAGNode>();
    Set<DAGNode> stack = new HashSet<DAGNode>();
    List<DAGNode> root = getRootNodes(containerNode);
    for (DAGNode node : root) {
      if (!marked.contains(node)) {
        checkForLoop(containerNode, node, marked, stack);
      }
    }
  }

  private static void checkForLoop(DAGContainer containerNode, DAGNode dagNode, Set<DAGNode> marked, Set<DAGNode> stack) throws BindingException {
    marked.add(dagNode);
    stack.add(dagNode);
    List<DAGNode> adjacentNodes = getAdjacentNodes(containerNode, dagNode);
    for (DAGNode s : adjacentNodes) {
      if (!marked.contains(s)) {
        checkForLoop(containerNode, s, marked, stack);
      } else if (stack.contains(s)) {
        throw new BindingException("Container node contains loop");
      }
    }
    stack.remove(dagNode);
  }

  private static List<DAGNode> getRootNodes(DAGContainer containerNode) {
    List<DAGNode> rootNodes = new ArrayList<DAGNode>();
    for (DAGNode node : containerNode.getChildren()) {
      for (DAGLink dataLink : containerNode.getLinks()) {
        if (dataLink.getDestination().getDagNodeId().equals(node.getId()) && dataLink.getSource().getDagNodeId().equals(containerNode.getId())) {
          rootNodes.add(node);
          break;
        }
      }
    }
    return rootNodes;
  }

  private static List<DAGNode> getAdjacentNodes(DAGContainer containerNode, DAGNode dagNode) throws BindingException {
    List<DAGNode> adjacentNodes = new ArrayList<DAGNode>();
    for (DAGLink dataLink : containerNode.getLinks()) {
      String nodeId = dataLink.getSource().getDagNodeId();
      if (nodeId.equals(dagNode.getId()) && dataLink.getDestination().getDagNodeId() != containerNode.getId()) {
        if (dataLink.getDestination().getDagNodeId().equals(containerNode.getId())) {
          continue;
        }
        adjacentNodes.add(getNodeFromId(containerNode, dataLink.getDestination().getDagNodeId()));
      }
    }
    return adjacentNodes;
  }

  private static DAGNode getNodeFromId(DAGContainer containerNode, String id) throws BindingException {
    for (DAGNode node : containerNode.getChildren()) {
      if (node.getId().equals(id)) {
        return node;
      }
    }
    throw new BindingException(String.format("Can't find DAGNode with id %s", id));
  }
}
