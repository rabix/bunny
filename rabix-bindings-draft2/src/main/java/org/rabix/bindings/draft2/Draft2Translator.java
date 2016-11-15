package org.rabix.bindings.draft2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolTranslator;
import org.rabix.bindings.draft2.bean.Draft2DataLink;
import org.rabix.bindings.draft2.bean.Draft2Job;
import org.rabix.bindings.draft2.bean.Draft2Step;
import org.rabix.bindings.draft2.bean.Draft2Workflow;
import org.rabix.bindings.draft2.helper.Draft2JobHelper;
import org.rabix.bindings.draft2.helper.Draft2SchemaHelper;
import org.rabix.bindings.helper.DAGValidationHelper;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.LinkMerge;
import org.rabix.bindings.model.ScatterMethod;
import org.rabix.bindings.model.dag.DAGContainer;
import org.rabix.bindings.model.dag.DAGLink;
import org.rabix.bindings.model.dag.DAGLinkPort;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.helper.InternalSchemaHelper;

public class Draft2Translator implements ProtocolTranslator {

  @Override
  public DAGNode translateToDAG(Job job) throws BindingException {
    Draft2Job draft2Job = Draft2JobHelper.getDraft2Job(job);
    DAGNode dagNode = processBatchInfo(draft2Job, transformToGeneric(draft2Job.getId(), draft2Job));
    DAGValidationHelper.detectLoop(dagNode);
    processPorts(dagNode);
    return dagNode;
  }
  
  @SuppressWarnings("unchecked")
  private DAGNode processBatchInfo(Draft2Job job, DAGNode node) {
    Object batch = job.getScatter();

    if (batch != null) {
      List<String> scatterList = new ArrayList<>();
      if (batch instanceof List<?>) {
        for (String scatter : ((List<String>) batch)) {
          scatterList.add(Draft2SchemaHelper.normalizeId(scatter));
        }
      } else if (batch instanceof String) {
        scatterList.add(Draft2SchemaHelper.normalizeId((String) batch));
      } else {
        throw new RuntimeException("Failed to process batch properties. Invalid application structure.");
      }

      for (String scatter : scatterList) {
        for (DAGLinkPort inputPort : node.getInputPorts()) {
          if (inputPort.getId().equals(scatter)) {
            inputPort.setScatter(true);
          }
        }

        if (node instanceof DAGContainer) {
          DAGContainer container = (DAGContainer) node;
          for (DAGLink link : container.getLinks()) {
            if (link.getSource().getId().equals(scatter) && link.getSource().getType().equals(LinkPortType.INPUT)) {
              link.getSource().setScatter(true);
            }
          }
        }
      }
    }
    return node;
  }

  private DAGNode transformToGeneric(String globalJobId, Draft2Job job) throws BindingException {
    List<DAGLinkPort> inputPorts = new ArrayList<>();
    
    for (ApplicationPort port : job.getApp().getInputs()) {
      Object defaultValue = job.getInputs().get(Draft2SchemaHelper.normalizeId(port.getId()));
      DAGLinkPort linkPort = new DAGLinkPort(Draft2SchemaHelper.normalizeId(port.getId()), job.getId(), LinkPortType.INPUT, LinkMerge.merge_nested, port.getScatter() != null ? port.getScatter() : false, defaultValue, null);
      inputPorts.add(linkPort);
    }
    List<DAGLinkPort> outputPorts = new ArrayList<>();
    for (ApplicationPort port : job.getApp().getOutputs()) {
      DAGLinkPort linkPort = new DAGLinkPort(Draft2SchemaHelper.normalizeId(port.getId()), job.getId(), LinkPortType.OUTPUT, LinkMerge.merge_nested, false, null, null);
      outputPorts.add(linkPort);
    }
    
    ScatterMethod scatterMethod = job.getScatterMethod() != null? ScatterMethod.valueOf(job.getScatterMethod()) : ScatterMethod.dotproduct;
    if (!job.getApp().isWorkflow()) {
      @SuppressWarnings("unchecked")
      Map<String, Object> commonDefaults = (Map<String, Object>) Draft2ValueTranslator.translateToCommon(job.getInputs());
      return new DAGNode(job.getId(), inputPorts, outputPorts, scatterMethod, job.getApp(), commonDefaults);
    }

    Draft2Workflow workflow = (Draft2Workflow) job.getApp();

    List<DAGNode> children = new ArrayList<>();
    for (Draft2Step step : workflow.getSteps()) {
      children.add(transformToGeneric(globalJobId, step.getJob()));
    }

    List<DAGLink> links = new ArrayList<>();
    for (Draft2DataLink dataLink : workflow.getDataLinks()) {
      String source = dataLink.getSource();
      String sourceNodeId = null;
      String sourcePortId = null;
      if (!source.contains(InternalSchemaHelper.SEPARATOR)) {
        sourceNodeId = job.getId();
        sourcePortId = source.substring(1);
      } else {
        sourceNodeId = job.getId() + InternalSchemaHelper.SEPARATOR + source.substring(1, source.indexOf(InternalSchemaHelper.SEPARATOR));
        sourcePortId = source.substring(source.indexOf(InternalSchemaHelper.SEPARATOR) + 1);
      }

      String destination = dataLink.getDestination();
      String destinationPortId = null;
      String destinationNodeId = null;
      if (!destination.contains(InternalSchemaHelper.SEPARATOR)) {
        destinationNodeId = job.getId();
        destinationPortId = destination.substring(1);
      } else {
        destinationNodeId = job.getId() + InternalSchemaHelper.SEPARATOR + destination.substring(1, destination.indexOf(InternalSchemaHelper.SEPARATOR));
        destinationPortId = destination.substring(destination.indexOf(InternalSchemaHelper.SEPARATOR) + 1);
      }
      boolean isSourceFromWorkflow = dataLink.getSource().contains(InternalSchemaHelper.SEPARATOR);
      boolean isDestinationFromWorkflow = dataLink.getDestination().contains(InternalSchemaHelper.SEPARATOR);

      DAGLinkPort sourceLinkPort = new DAGLinkPort(sourcePortId, sourceNodeId, isSourceFromWorkflow ? LinkPortType.OUTPUT : LinkPortType.INPUT, LinkMerge.merge_nested, false, null, null);
      DAGLinkPort destinationLinkPort = new DAGLinkPort(destinationPortId, destinationNodeId, isDestinationFromWorkflow? LinkPortType.INPUT : LinkPortType.OUTPUT, dataLink.getLinkMerge(), dataLink.getScattered() != null ? dataLink.getScattered() : false, null, null);

      int position = dataLink.getPosition() != null ? dataLink.getPosition() : 1;
      links.add(new DAGLink(sourceLinkPort, destinationLinkPort, dataLink.getLinkMerge(), position));
    }
    @SuppressWarnings("unchecked")
    Map<String, Object> commonDefaults = (Map<String, Object>) Draft2ValueTranslator.translateToCommon(job.getInputs());
    return new DAGContainer(job.getId(), inputPorts, outputPorts, job.getApp(), scatterMethod, links, children, commonDefaults);
  }
  
  private void processPorts(DAGNode dagNode) {
    if (dagNode instanceof DAGContainer) {
      DAGContainer dagContainer = (DAGContainer) dagNode;
      
      for (DAGLink dagLink : dagContainer.getLinks()) {
        dagLink.getDestination().setLinkMerge(dagLink.getLinkMerge());
        processPorts(dagLink, dagNode);
        
        for (DAGNode childNode : dagContainer.getChildren()) {
          processPorts(dagLink, childNode);
          if (childNode instanceof DAGContainer) {
            processPorts(childNode);
          }
        }
      }
    }
  }
  
  private void processPorts(DAGLink dagLink, DAGNode dagNode) {
    for (DAGLinkPort dagLinkPort : dagNode.getInputPorts()) {
      if (dagLinkPort.equals(dagLink.getDestination())) {
        dagLinkPort.setLinkMerge(dagLink.getLinkMerge());
      }
    }
    for (DAGLinkPort dagLinkPort : dagNode.getOutputPorts()) {
      if (dagLinkPort.equals(dagLink.getDestination())) {
        dagLinkPort.setLinkMerge(dagLink.getLinkMerge());
      }
    }
  }

}
