package org.rabix.engine.processor.handler.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.bindings.model.LinkMerge;
import org.rabix.bindings.model.dag.DAGContainer;
import org.rabix.bindings.model.dag.DAGLink;
import org.rabix.bindings.model.dag.DAGLinkPort;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.engine.JobHelper;
import org.rabix.engine.db.DAGNodeDB;
import org.rabix.engine.db.ReadyJobGroupsDB;
import org.rabix.engine.event.Event;
import org.rabix.engine.event.impl.ContextStatusEvent;
import org.rabix.engine.event.impl.InputUpdateEvent;
import org.rabix.engine.event.impl.JobStatusEvent;
import org.rabix.engine.event.impl.OutputUpdateEvent;
import org.rabix.engine.model.ContextRecord.ContextStatus;
import org.rabix.engine.model.JobRecord;
import org.rabix.engine.model.JobRecord.PortCounter;
import org.rabix.engine.model.LinkRecord;
import org.rabix.engine.model.VariableRecord;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.handler.EventHandler;
import org.rabix.engine.processor.handler.EventHandlerException;
import org.rabix.engine.service.ContextRecordService;
import org.rabix.engine.service.JobRecordService;
import org.rabix.engine.service.JobRecordService.JobState;
import org.rabix.engine.service.LinkRecordService;
import org.rabix.engine.service.VariableRecordService;
import org.rabix.engine.status.EngineStatusCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class JobStatusEventHandler implements EventHandler<JobStatusEvent> {

  private final Logger logger = LoggerFactory.getLogger(JobStatusEventHandler.class);
  
  private final DAGNodeDB dagNodeDB;
  private final ScatterHandler scatterHelper;
  private final EventProcessor eventProcessor;
  
  private final ReadyJobGroupsDB jobGroupsDB;
  
  private final JobRecordService jobRecordService;
  private final LinkRecordService linkRecordService;
  private final VariableRecordService variableRecordService;
  private final ContextRecordService contextRecordService;
  
  private EngineStatusCallback engineStatusCallback;

  @Inject
  public JobStatusEventHandler(final DAGNodeDB dagNodeDB, final JobRecordService jobRecordService, final LinkRecordService linkRecordService, final VariableRecordService variableRecordService, final ContextRecordService contextRecordService, final EventProcessor eventProcessor, final ScatterHandler scatterHelper, final ReadyJobGroupsDB jobGroupsDB) {
    this.dagNodeDB = dagNodeDB;
    this.scatterHelper = scatterHelper;
    this.eventProcessor = eventProcessor;
    this.jobGroupsDB = jobGroupsDB;
    this.jobRecordService = jobRecordService;
    this.linkRecordService = linkRecordService;
    this.contextRecordService = contextRecordService;
    this.variableRecordService = variableRecordService;
  }

  public void initialize(EngineStatusCallback engineStatusCallback) {
    this.engineStatusCallback = engineStatusCallback;
  }

  @Override
  public void handle(JobStatusEvent event) throws EventHandlerException {
    JobRecord jobRecord = jobRecordService.find(event.getJobId(), event.getContextId());

    switch (event.getState()) {
    case READY:
      jobRecord.setState(JobState.READY);
      jobRecordService.update(jobRecord);
      ready(jobRecord, event);
      
      if (!jobRecord.isContainer() && !jobRecord.isScatterWrapper()) {
        Job job = null;
        try {
          job = JobHelper.createReadyJob(jobRecord, JobStatus.READY, jobRecordService, variableRecordService, linkRecordService, contextRecordService, dagNodeDB);
          if (!StringUtils.isEmpty(event.getEventGroupId())) {
            jobGroupsDB.add(event.getEventGroupId(), job);
          } else {
            try {
              engineStatusCallback.onJobReady(job);
            } catch (Exception e) {
              logger.error("Failed to call onReady callback for Job " + job.getId(), e);
              throw new EventHandlerException("Failed to call onReady callback for Job " + job.getId(), e);
            }
          }
        } catch (BindingException e1) {
          logger.info("Failed to create job", e1);
        }
      }
      break;
    case RUNNING:
      jobRecord.setState(JobState.RUNNING);
      jobRecordService.update(jobRecord);
      break;
    case COMPLETED:
      if (jobRecord.isRoot()) {
        try {
          if(!jobRecord.isContainer()) {
            // if root is CommandLineTool create OutputUpdateEvents
            for (PortCounter portCounter : jobRecord.getOutputCounters()) {
              Object output = event.getResult().get(portCounter.getPort());
              eventProcessor.send(new OutputUpdateEvent(jobRecord.getRootId(), jobRecord.getId(), portCounter.getPort(), output, 1, event.getEventGroupId()));
            }
          }
          eventProcessor.send(new ContextStatusEvent(event.getContextId(), ContextStatus.COMPLETED));
          Job rootJob = JobHelper.createRootJob(jobRecord, JobStatus.COMPLETED, jobRecordService, variableRecordService, linkRecordService, contextRecordService, dagNodeDB, event.getResult());
          engineStatusCallback.onJobRootCompleted(rootJob);
          deleteRecords(rootJob.getId());
        } catch (Exception e) {
          logger.error("Failed to call onRootCompleted callback for Job " + jobRecord.getRootId(), e);
          throw new EventHandlerException("Failed to call onRootCompleted callback for Job " + jobRecord.getRootId(), e);
        }
      } else {
        for (PortCounter portCounter : jobRecord.getOutputCounters()) {
          Object output = event.getResult().get(portCounter.getPort());
          eventProcessor.addToQueue(new OutputUpdateEvent(jobRecord.getRootId(), jobRecord.getId(), portCounter.getPort(), output, 1, event.getEventGroupId()));
        }
      }
      break;
    case FAILED:
      if (jobRecord.isRoot()) {
        try {
          Job rootJob = JobHelper.createRootJob(jobRecord, JobStatus.FAILED, jobRecordService, variableRecordService, linkRecordService, contextRecordService, dagNodeDB, null);
          engineStatusCallback.onJobRootFailed(rootJob);
          
          eventProcessor.send(new ContextStatusEvent(event.getContextId(), ContextStatus.FAILED));
          deleteRecords(rootJob.getId());
        } catch (Exception e) {
          logger.error("Failed to call onRootFailed callback for Job " + jobRecord.getRootId(), e);
          throw new EventHandlerException("Failed to call onRootFailed callback for Job " + jobRecord.getRootId(), e);
        }
      } else {
        try {
          Job failedJob = JobHelper.createCompletedJob(jobRecord, JobStatus.FAILED, jobRecordService, variableRecordService, linkRecordService, contextRecordService, dagNodeDB);
          engineStatusCallback.onJobFailed(failedJob);
          
          eventProcessor.send(new JobStatusEvent("root", event.getContextId(), JobState.FAILED, null, event.getEventGroupId())); // TODO remove hardcoded 'root' value
        } catch (Exception e) {
          logger.error("Failed to call onFailed callback for Job " + jobRecord.getId(), e);
          throw new EventHandlerException("Failed to call onFailed callback for Job " + jobRecord.getId(), e);
        }
      }
      break;
    default:
      break;
    }
  }
  
  private void deleteRecords(String rootId) {
    jobRecordService.delete(rootId);
    variableRecordService.delete(rootId);
    linkRecordService.delete(rootId);
    jobGroupsDB.delete(rootId);
  }
  
  /**
   * Job is ready
   */
  public void ready(JobRecord job, Event event) throws EventHandlerException {
    job.setState(JobState.READY);
    
    String contextId = event.getContextId();
    DAGNode node = dagNodeDB.get(InternalSchemaHelper.normalizeId(job.getId()), contextId);

    StringBuilder readyJobLogging = new StringBuilder(" --- JobRecord ").append(job.getId()).append(" is ready.").append(" Job isBlocking=").append(job.isBlocking()).append("\n");
    for (PortCounter portCounter : job.getInputCounters()) {
      readyJobLogging.append(" --- Input port ").append(portCounter.getPort()).append(", isScatter=").append(portCounter.isScatter()).append(", isBlocking ").append(job.isInputPortBlocking(node, portCounter.getPort())).append("\n");
    }
    readyJobLogging.append(" --- All scatter ports ").append(job.getScatterPorts()).append("\n");
    logger.debug(readyJobLogging.toString());
    
    if (job.isContainer()) {
      job.setState(JobState.RUNNING);

      DAGContainer containerNode;
      if (job.isScattered()) {
        containerNode = (DAGContainer) node;
      } else {
        containerNode = (DAGContainer) node;
      }
      rollOutContainer(job, containerNode, contextId);

      List<LinkRecord> containerLinks = linkRecordService.findBySourceAndSourceType(job.getId(), LinkPortType.INPUT, contextId);
      if (containerLinks.isEmpty()) {
        Set<String> immediateReadyNodeIds = findImmediateReadyNodes(containerNode);
        for (String readyNodeId : immediateReadyNodeIds) {
          JobRecord childJobRecord = jobRecordService.find(readyNodeId, contextId);
          if(childJobRecord.isContainer() || childJobRecord.isScatterWrapper()) {
        	ready(childJobRecord, event);  
          }
          else {
            JobStatusEvent jobStatusEvent = new JobStatusEvent(childJobRecord.getId(), contextId, JobState.READY, null, event.getEventGroupId());
            eventProcessor.send(jobStatusEvent);
          }
        }
      } else {
        for (LinkRecord link : containerLinks) {
          VariableRecord sourceVariable = variableRecordService.find(link.getSourceJobId(), link.getSourceJobPort(), LinkPortType.INPUT, contextId);
          VariableRecord destinationVariable = variableRecordService.find(link.getDestinationJobId(), link.getDestinationJobPort(), LinkPortType.INPUT, contextId);
          if(destinationVariable == null) {
            VariableRecord stepVariable = new VariableRecord(contextId, link.getDestinationJobId(), sourceVariable.getPortId(), LinkPortType.INPUT, sourceVariable.getValue(), null);
            variableRecordService.create(stepVariable);
          }
          Event updateEvent = new InputUpdateEvent(contextId, link.getDestinationJobId(), link.getDestinationJobPort(), sourceVariable.getValue(), link.getPosition(), event.getEventGroupId());
          eventProcessor.send(updateEvent);
        }
      }
    } else if (!job.isScattered() && job.getScatterPorts().size() > 0) {
      job.setState(JobState.RUNNING);
      
      for (String port : job.getScatterPorts()) {
        VariableRecord variable = variableRecordService.find(job.getId(), port, LinkPortType.INPUT, contextId);
        scatterHelper.scatterPort(job, event, port, variable.getValue(), 1, null, false, false);
      }
    }
  }
  
  private Set<String> findImmediateReadyNodes(DAGNode node) {
    if (node instanceof DAGContainer) {
      Set<String> nodesWithoutDestination = new HashSet<>();
      for (DAGNode child : ((DAGContainer) node).getChildren()) {
        nodesWithoutDestination.add(child.getId());
      }
      
      for (DAGLink link : ((DAGContainer) node).getLinks()) {
        nodesWithoutDestination.remove(link.getDestination().getDagNodeId());
      }
      return nodesWithoutDestination;
    }
    return Collections.<String>emptySet();
  }
  
  /**
   * Unwraps {@link DAGContainer}
   */
  private void rollOutContainer(JobRecord job, DAGContainer containerNode, String contextId) {
    for (DAGNode node : containerNode.getChildren()) {
      String newJobId = InternalSchemaHelper.concatenateIds(job.getId(), InternalSchemaHelper.getLastPart(node.getId()));
      
      JobRecord childJob = scatterHelper.createJobRecord(newJobId, job.getExternalId(), node, false, contextId);
      jobRecordService.create(childJob);

      StringBuilder childJobLogBuilder = new StringBuilder("\n -- JobRecord ").append(newJobId).append(", isBlocking ").append(childJob.isBlocking()).append("\n");
      for (DAGLinkPort port : node.getInputPorts()) {
        if(port.getTransform() != null) {
          childJob.setBlocking(true);
        }
        VariableRecord childVariable = new VariableRecord(contextId, newJobId, port.getId(), LinkPortType.INPUT, port.getDefaultValue(), node.getLinkMerge(port.getId(), port.getType()));
        childJobLogBuilder.append(" -- Input port ").append(port.getId()).append(", isScatter ").append(port.isScatter()).append("\n");
        variableRecordService.create(childVariable);
      }

      for (DAGLinkPort port : node.getOutputPorts()) {
        childJobLogBuilder.append(" -- Output port ").append(port.getId()).append(", isScatter ").append(port.isScatter()).append("\n");
        VariableRecord childVariable = new VariableRecord(contextId, newJobId, port.getId(), LinkPortType.OUTPUT, null, node.getLinkMerge(port.getId(), port.getType()));
        variableRecordService.create(childVariable);
      }
      logger.debug(childJobLogBuilder.toString());
    }
    for (DAGLink link : containerNode.getLinks()) {
      String originalJobID = InternalSchemaHelper.normalizeId(job.getId());

      String sourceNodeId = originalJobID;
      String linkSourceNodeId = link.getSource().getDagNodeId();
      if (linkSourceNodeId.startsWith(originalJobID)) {
        if (linkSourceNodeId.equals(sourceNodeId)) {
          sourceNodeId = job.getId();
        } else {
          sourceNodeId = InternalSchemaHelper.concatenateIds(job.getId(), InternalSchemaHelper.getLastPart(linkSourceNodeId));
        }
      }
      String destinationNodeId = originalJobID;
      String linkDestinationNodeId = link.getDestination().getDagNodeId();
      if (linkDestinationNodeId.startsWith(originalJobID)) {
        if (linkDestinationNodeId.equals(destinationNodeId)) {
          destinationNodeId = job.getId();
        } else {
          destinationNodeId = InternalSchemaHelper.concatenateIds(job.getId(), InternalSchemaHelper.getLastPart(linkDestinationNodeId));
        }
      }
      LinkRecord childLink = new LinkRecord(contextId, sourceNodeId, link.getSource().getId(), LinkPortType.valueOf(link.getSource().getType().toString()), destinationNodeId, link.getDestination().getId(), LinkPortType.valueOf(link.getDestination().getType().toString()), link.getPosition());
      linkRecordService.create(childLink);

      handleLinkPort(jobRecordService.find(sourceNodeId, contextId), link.getSource(), true);
      handleLinkPort(jobRecordService.find(destinationNodeId, contextId), link.getDestination(), false);
    }
  }
  
  /**
   * Handle links for roll-out 
   */
  private void handleLinkPort(JobRecord job, DAGLinkPort linkPort, boolean isSource) {
    if (linkPort.getType().equals(LinkPortType.INPUT)) {
      if (job.getState().equals(JobState.PENDING)) {
        job.incrementPortCounter(linkPort, LinkPortType.INPUT);
        job.increaseInputPortIncoming(linkPort.getId());
        
        if (job.getInputPortIncoming(linkPort.getId()) > 1) {
          if (LinkMerge.isBlocking(linkPort.getLinkMerge())) {
            job.setBlocking(true);
          }
        }
      }
    } else {
      job.incrementPortCounter(linkPort, LinkPortType.OUTPUT);
      if (isSource) {
        job.getOutputCounter(linkPort.getId()).updatedAsSource(1);
      }
      job.increaseOutputPortIncoming(linkPort.getId());
    }
    jobRecordService.update(job);
  }

}
