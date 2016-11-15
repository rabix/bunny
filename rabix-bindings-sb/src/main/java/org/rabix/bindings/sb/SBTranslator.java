package org.rabix.bindings.sb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolTranslator;
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
import org.rabix.bindings.sb.bean.SBDataLink;
import org.rabix.bindings.sb.bean.SBJob;
import org.rabix.bindings.sb.bean.SBStep;
import org.rabix.bindings.sb.bean.SBWorkflow;
import org.rabix.bindings.sb.helper.SBJobHelper;
import org.rabix.bindings.sb.helper.SBSchemaHelper;
import org.rabix.common.helper.InternalSchemaHelper;

public class SBTranslator implements ProtocolTranslator {

  @Override
  public DAGNode translateToDAG(Job job) throws BindingException {
    SBJob sbJob = SBJobHelper.getSBJob(job);
    DAGNode dagNode = processBatchInfo(sbJob, transformToGeneric(sbJob.getId(), sbJob));
    DAGValidationHelper.detectLoop(dagNode);
    processPorts(dagNode);
    return dagNode;
  }
  
  @SuppressWarnings("unchecked")
  private DAGNode processBatchInfo(SBJob job, DAGNode node) {
    Object batch = job.getScatter();

    if (batch != null) {
      List<String> scatterList = new ArrayList<>();
      if (batch instanceof List<?>) {
        for (String scatter : ((List<String>) batch)) {
          scatterList.add(SBSchemaHelper.normalizeId(scatter));
        }
      } else if (batch instanceof String) {
        scatterList.add(SBSchemaHelper.normalizeId((String) batch));
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

  private DAGNode transformToGeneric(String globalJobId, SBJob job) throws BindingException {
    List<DAGLinkPort> inputPorts = new ArrayList<>();
    
    for (ApplicationPort port : job.getApp().getInputs()) {
      Object defaultValue = job.getInputs().get(SBSchemaHelper.normalizeId(port.getId()));
      DAGLinkPort linkPort = new DAGLinkPort(SBSchemaHelper.normalizeId(port.getId()), job.getId(), LinkPortType.INPUT, LinkMerge.merge_nested, port.getScatter() != null ? port.getScatter() : false, defaultValue, null);
      inputPorts.add(linkPort);
    }
    List<DAGLinkPort> outputPorts = new ArrayList<>();
    for (ApplicationPort port : job.getApp().getOutputs()) {
      DAGLinkPort linkPort = new DAGLinkPort(SBSchemaHelper.normalizeId(port.getId()), job.getId(), LinkPortType.OUTPUT, LinkMerge.merge_nested, false, null, null);
      outputPorts.add(linkPort);
    }
    
    ScatterMethod scatterMethod = job.getScatterMethod() != null? ScatterMethod.valueOf(job.getScatterMethod()) : ScatterMethod.dotproduct;
    if (!job.getApp().isWorkflow()) {
      @SuppressWarnings("unchecked")
      Map<String, Object> commonDefaults = (Map<String, Object>) SBValueTranslator.translateToCommon(job.getInputs());
      return new DAGNode(job.getId(), inputPorts, outputPorts, scatterMethod, job.getApp(), commonDefaults);
    }

    SBWorkflow workflow = (SBWorkflow) job.getApp();

    List<DAGNode> children = new ArrayList<>();
    for (SBStep step : workflow.getSteps()) {
      children.add(transformToGeneric(globalJobId, step.getJob()));
    }

    List<DAGLink> links = new ArrayList<>();
    for (SBDataLink dataLink : workflow.getDataLinks()) {
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
    Map<String, Object> commonDefaults = (Map<String, Object>) SBValueTranslator.translateToCommon(job.getInputs());
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
