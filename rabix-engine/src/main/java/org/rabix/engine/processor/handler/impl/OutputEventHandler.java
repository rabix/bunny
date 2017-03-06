package org.rabix.engine.processor.handler.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.common.helper.CloneHelper;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.engine.JobHelper;
import org.rabix.engine.db.AppDB;
import org.rabix.engine.db.DAGNodeDB;
import org.rabix.engine.event.Event;
import org.rabix.engine.event.impl.InputUpdateEvent;
import org.rabix.engine.event.impl.JobStatusEvent;
import org.rabix.engine.event.impl.OutputUpdateEvent;
import org.rabix.engine.model.JobRecord;
import org.rabix.engine.model.LinkRecord;
import org.rabix.engine.model.VariableRecord;
import org.rabix.engine.model.scatter.ScatterStrategy;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.handler.EventHandler;
import org.rabix.engine.processor.handler.EventHandlerException;
import org.rabix.engine.service.ContextRecordService;
import org.rabix.engine.service.JobRecordService;
import org.rabix.engine.service.JobService;
import org.rabix.engine.service.LinkRecordService;
import org.rabix.engine.service.VariableRecordService;
import org.rabix.engine.service.impl.JobRecordServiceImpl.JobState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Handles {@link OutputUpdateEvent} events.
 */
public class OutputEventHandler implements EventHandler<OutputUpdateEvent> {

  private final static Logger logger = LoggerFactory.getLogger(OutputEventHandler.class);
  
  private JobRecordService jobRecordService;
  private LinkRecordService linkService;
  private VariableRecordService variableService;
  private ContextRecordService contextService;
    
  private final EventProcessor eventProcessor;
  
  private DAGNodeDB dagNodeDB;
  private AppDB appDB;
  private JobService jobService;
  
  @Inject
  public OutputEventHandler(EventProcessor eventProcessor, JobRecordService jobRecordService, VariableRecordService variableService, LinkRecordService linkService, ContextRecordService contextService, DAGNodeDB dagNodeDB, AppDB appDB, JobService jobService) {
    this.dagNodeDB = dagNodeDB;
    this.appDB = appDB;
    this.jobRecordService = jobRecordService;
    this.linkService = linkService;
    this.contextService = contextService;
    this.variableService = variableService;
    this.eventProcessor = eventProcessor;
    this.jobService = jobService;
  }
  
  public void handle(final OutputUpdateEvent event) throws EventHandlerException {
    JobRecord sourceJob = jobRecordService.find(event.getJobId(), event.getContextId());
    if (event.isFromScatter()) {
      jobRecordService.resetOutputPortCounter(sourceJob, event.getNumberOfScattered(), event.getPortId());
    }
    VariableRecord sourceVariable = variableService.find(event.getJobId(), event.getPortId(), LinkPortType.OUTPUT, event.getContextId());
    jobRecordService.decrementPortCounter(sourceJob, event.getPortId(), LinkPortType.OUTPUT);
    variableService.addValue(sourceVariable, event.getValue(), event.getPosition(), sourceJob.isScatterWrapper() || event.isFromScatter());
    variableService.update(sourceVariable); // TODO wha?
    jobRecordService.update(sourceJob);
    
    if (sourceJob.isCompleted()) {
      try {
        Job completedJob = JobHelper.createCompletedJob(sourceJob, JobStatus.COMPLETED, jobRecordService, variableService, linkService, contextService, dagNodeDB, appDB);
        jobService.handleJobCompleted(completedJob);
      } catch (BindingException e) {
        logger.error("Failed to create Job " + sourceJob.getId(), e);
      }
      
      if (sourceJob.isRoot()) {
        Map<String, Object> outputs = new HashMap<>();
        List<VariableRecord> outputVariables = variableService.find(sourceJob.getId(), LinkPortType.OUTPUT, sourceJob.getRootId());
        for (VariableRecord outputVariable : outputVariables) {
          Object value = CloneHelper.deepCopy(variableService.getValue(outputVariable));
          outputs.put(outputVariable.getPortId(), value);
        }
        if(sourceJob.isRoot() && sourceJob.isContainer()) {
          // if root job is CommandLineTool OutputUpdateEvents are created from JobStatusEvent
          eventProcessor.send(new JobStatusEvent(sourceJob.getId(), event.getContextId(), JobState.COMPLETED, outputs, event.getEventGroupId(), event.getProducedByNode()));
        }
        return;
      }
    }
    
    if (sourceJob.isRoot()) {
        jobService.handleJobRootPartiallyCompleted(createRootJob(sourceJob, JobHelper.transformStatus(sourceJob.getState())));
    }
    
    Object value = null;
    
    if (sourceJob.isScatterWrapper()) {
      ScatterStrategy scatterStrategy = sourceJob.getScatterStrategy();
      
      boolean isValueFromScatterStrategy = false;
      if (scatterStrategy.isBlocking()) {
        if (sourceJob.isOutputPortReady(event.getPortId())) {
          isValueFromScatterStrategy = true;
          value = scatterStrategy.values(variableService, sourceJob.getId(), event.getPortId(), event.getContextId());
        } else {
          return;
        }
      }
      
      List<LinkRecord> links = linkService.findBySource(sourceVariable.getJobId(), sourceVariable.getPortId(), event.getContextId());
      for (LinkRecord link : links) {
        if (!isValueFromScatterStrategy) {
          value = null; // reset
        }
        List<VariableRecord> destinationVariables = variableService.find(link.getDestinationJobId(), link.getDestinationJobPort(), event.getContextId());

        JobRecord destinationJob = null;
        boolean isDestinationPortScatterable = false;
        for (VariableRecord destinationVariable : destinationVariables) {
          switch (destinationVariable.getType()) {
          case INPUT:
            destinationJob = jobRecordService.find(destinationVariable.getJobId(), destinationVariable.getRootId());
            isDestinationPortScatterable = destinationJob.isScatterPort(destinationVariable.getPortId());
            if (isDestinationPortScatterable && !destinationJob.isBlocking() && !(destinationJob.getInputPortIncoming(event.getPortId()) > 1)) {
              value = value != null ? value : event.getValue();
              int numberOfScattered = sourceJob.getNumberOfGlobalOutputs();
              Event updateInputEvent = new InputUpdateEvent(event.getContextId(), destinationVariable.getJobId(), destinationVariable.getPortId(), value, true, numberOfScattered, event.getPosition(), event.getEventGroupId(), event.getProducedByNode());
              eventProcessor.send(updateInputEvent);
            } else {
              if (sourceJob.isOutputPortReady(event.getPortId())) {
                value = value != null ? value : variableService.getValue(sourceVariable);
                Event updateInputEvent = new InputUpdateEvent(event.getContextId(), destinationVariable.getJobId(), destinationVariable.getPortId(), value, link.getPosition(), event.getEventGroupId(), event.getProducedByNode());
                eventProcessor.send(updateInputEvent);
              }
            }
            break;
          case OUTPUT:
            destinationJob = jobRecordService.find(destinationVariable.getJobId(), destinationVariable.getRootId());
            if (destinationJob.getOutputPortIncoming(event.getPortId()) > 1) {
              if (sourceJob.isOutputPortReady(event.getPortId())) {
                value = value != null? value : variableService.getValue(sourceVariable);
                Event updateInputEvent = new OutputUpdateEvent(event.getContextId(), destinationVariable.getJobId(), destinationVariable.getPortId(), value, link.getPosition(), event.getEventGroupId(), event.getProducedByNode());
                eventProcessor.send(updateInputEvent);
              }
            } else {
              value = value != null? value : event.getValue();
              if (isValueFromScatterStrategy) {
                Event updateOutputEvent = new OutputUpdateEvent(event.getContextId(), destinationVariable.getJobId(), destinationVariable.getPortId(), value, false, 1, 1, event.getEventGroupId(), event.getProducedByNode());
                eventProcessor.send(updateOutputEvent);
              } else {
                int numberOfScattered = sourceJob.getNumberOfGlobalOutputs();
                Event updateOutputEvent = new OutputUpdateEvent(event.getContextId(), destinationVariable.getJobId(), destinationVariable.getPortId(), value, true, numberOfScattered, event.getPosition(), event.getEventGroupId(), event.getProducedByNode());
                eventProcessor.send(updateOutputEvent);
              }
            }
            break;
          }
        }
      }
      return;
    }
    
    if (sourceJob.isOutputPortReady(event.getPortId())) {
      List<LinkRecord> links = linkService.findBySource(event.getJobId(), event.getPortId(), event.getContextId());
      for (LinkRecord link : links) {
        List<VariableRecord> destinationVariables = variableService.find(link.getDestinationJobId(), link.getDestinationJobPort(), event.getContextId());
        
        value = variableService.getValue(sourceVariable);
        for (VariableRecord destinationVariable : destinationVariables) {
          switch (destinationVariable.getType()) {
          case INPUT:
            Event updateInputEvent = new InputUpdateEvent(event.getContextId(), destinationVariable.getJobId(), destinationVariable.getPortId(), value, link.getPosition(), event.getEventGroupId(), event.getProducedByNode());
            eventProcessor.send(updateInputEvent);
            break;
          case OUTPUT:
            if (sourceJob.isScattered()) {
              int numberOfScattered = sourceJob.getNumberOfGlobalOutputs();
              int position = InternalSchemaHelper.getScatteredNumber(sourceJob.getId());
              Event updateOutputEvent = new OutputUpdateEvent(event.getContextId(), destinationVariable.getJobId(), destinationVariable.getPortId(), value, true, numberOfScattered, position, event.getEventGroupId(), event.getProducedByNode());
              eventProcessor.send(updateOutputEvent);
            } else if (InternalSchemaHelper.getParentId(sourceJob.getId()).equals(destinationVariable.getJobId())) {
              Event updateOutputEvent = new OutputUpdateEvent(event.getContextId(), destinationVariable.getJobId(), destinationVariable.getPortId(), value, link.getPosition(), event.getEventGroupId(), event.getProducedByNode());
              eventProcessor.send(updateOutputEvent);
            }
            break;
          }
        }
      }
    }
  }
  
  private Job createRootJob(JobRecord jobRecord, JobStatus status) {
    Map<String, Object> outputs = new HashMap<>();
    List<VariableRecord> outputVariables = variableService.find(jobRecord.getId(), LinkPortType.OUTPUT, jobRecord.getRootId());
    for (VariableRecord outputVariable : outputVariables) {
      Object value = CloneHelper.deepCopy(variableService.getValue(outputVariable));
      outputs.put(outputVariable.getPortId(), value);
    }
    return JobHelper.createRootJob(jobRecord, status, jobRecordService, variableService, linkService, contextService, dagNodeDB, appDB, outputs);
  }
  
}
