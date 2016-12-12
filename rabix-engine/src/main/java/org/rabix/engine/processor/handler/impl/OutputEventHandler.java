package org.rabix.engine.processor.handler.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.common.helper.CloneHelper;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.engine.JobHelper;
import org.rabix.engine.db.DAGNodeDB;
import org.rabix.engine.db.ReadyJobGroupsDB;
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
import org.rabix.engine.service.JobRecordService.JobState;
import org.rabix.engine.service.LinkRecordService;
import org.rabix.engine.service.VariableRecordService;
import org.rabix.engine.status.EngineStatusCallback;
import org.rabix.engine.status.EngineStatusCallbackException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Handles {@link OutputUpdateEvent} events.
 */
public class OutputEventHandler implements EventHandler<OutputUpdateEvent> {

  private final static Logger logger = LoggerFactory.getLogger(OutputEventHandler.class);
  
  private JobRecordService jobService;
  private LinkRecordService linkService;
  private VariableRecordService variableService;
  private ContextRecordService contextService;
    
  private final EventProcessor eventProcessor;
  
  private DAGNodeDB dagNodeDB;
  private ReadyJobGroupsDB jobGroupsDB;
  private EngineStatusCallback engineStatusCallback;
  
  @Inject
  public OutputEventHandler(EventProcessor eventProcessor, JobRecordService jobService, VariableRecordService variableService, LinkRecordService linkService, ContextRecordService contextService, DAGNodeDB dagNodeDB, ReadyJobGroupsDB jobGroupsDB) {
    this.dagNodeDB = dagNodeDB;
    this.jobGroupsDB = jobGroupsDB;
    this.jobService = jobService;
    this.linkService = linkService;
    this.contextService = contextService;
    this.variableService = variableService;
    this.eventProcessor = eventProcessor;
  }

  public void initialize(EngineStatusCallback engineStatusCallback) {
    this.engineStatusCallback = engineStatusCallback;
  }
  
  public void handle(final OutputUpdateEvent event) throws EventHandlerException {
    JobRecord sourceJob = jobService.find(event.getJobId(), event.getContextId());
    if (event.isFromScatter()) {
      sourceJob.resetOutputPortCounter(event.getNumberOfScattered(), event.getPortId());
    }
    VariableRecord sourceVariable = variableService.find(event.getJobId(), event.getPortId(), LinkPortType.OUTPUT, event.getContextId());
    sourceJob.decrementPortCounter(event.getPortId(), LinkPortType.OUTPUT);
    sourceVariable.addValue(event.getValue(), event.getPosition());
    jobService.update(sourceJob);
    
    if (sourceJob.isCompleted()) {
      sourceJob.setState(JobState.COMPLETED);
      jobService.update(sourceJob);
      
      try {
        Job completedJob = JobHelper.createCompletedJob(sourceJob, JobStatus.COMPLETED, jobService, variableService, linkService, contextService, dagNodeDB);
        engineStatusCallback.onJobCompleted(completedJob);
      } catch (BindingException e) {
        logger.error("Failed to create Job " + sourceJob.getId(), e);
      } catch (EngineStatusCallbackException e) {
        logger.error("Failed to call onJobCompleted callback for Job " + sourceJob.getId(), e);
      }
      
      if (sourceJob.isRoot()) {
        Map<String, Object> outputs = new HashMap<>();
        List<VariableRecord> outputVariables = variableService.find(sourceJob.getId(), LinkPortType.OUTPUT, sourceJob.getRootId());
        for (VariableRecord outputVariable : outputVariables) {
          Object value = CloneHelper.deepCopy(outputVariable.getValue());
          outputs.put(outputVariable.getPortId(), value);
        }
        if(sourceJob.isRoot() && sourceJob.isContainer()) {
          // if root job is CommandLineTool OutputUpdateEvents are created from JobStatusEvent
          eventProcessor.send(new JobStatusEvent(sourceJob.getId(), event.getContextId(), JobState.COMPLETED, outputs, event.getEventGroupId()));
        }
        return;
      }
    }
    
    if (sourceJob.isRoot()) {
      try {
        engineStatusCallback.onJobRootPartiallyCompleted(createRootJob(sourceJob, JobHelper.transformStatus(sourceJob.getState())));
      } catch (EngineStatusCallbackException e) {
        logger.error("Failed to call onReady callback for Job " + sourceJob.getId(), e);
        throw new EventHandlerException("Failed to call onJobRootPartiallyCompleted callback for Job " + sourceJob.getId(), e);
      }
    }
    
    Object value = null;
    
    if (sourceJob.isScatterWrapper()) {
      ScatterStrategy scatterStrategy = sourceJob.getScatterStrategy();
      
      boolean isValueFromScatterStrategy = false;
      if (scatterStrategy.isBlocking()) {
        if (sourceJob.isOutputPortReady(event.getPortId())) {
          isValueFromScatterStrategy = true;
          value = scatterStrategy.values(sourceJob.getId(), event.getPortId(), event.getContextId());
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
            destinationJob = jobService.find(destinationVariable.getJobId(), destinationVariable.getContextId());
            isDestinationPortScatterable = destinationJob.isScatterPort(destinationVariable.getPortId());
            if (isDestinationPortScatterable && !destinationJob.isBlocking() && !(destinationJob.getInputPortIncoming(event.getPortId()) > 1)) {
              value = value != null ? value : event.getValue();
              int numberOfScattered = sourceJob.getNumberOfGlobalOutputs();
              Event updateInputEvent = new InputUpdateEvent(event.getContextId(), destinationVariable.getJobId(), destinationVariable.getPortId(), value, true, numberOfScattered, event.getPosition(), event.getEventGroupId());
              eventProcessor.send(updateInputEvent);
            } else {
              if (sourceJob.isOutputPortReady(event.getPortId())) {
                value = value != null ? value : sourceVariable.getValue();
                Event updateInputEvent = new InputUpdateEvent(event.getContextId(), destinationVariable.getJobId(), destinationVariable.getPortId(), value, link.getPosition(), event.getEventGroupId());
                eventProcessor.send(updateInputEvent);
              }
            }
            break;
          case OUTPUT:
            destinationJob = jobService.find(destinationVariable.getJobId(), destinationVariable.getContextId());
            if (destinationJob.getOutputPortIncoming(event.getPortId()) > 1) {
              if (sourceJob.isOutputPortReady(event.getPortId())) {
                value = value != null? value : sourceVariable.getValue();
                Event updateInputEvent = new OutputUpdateEvent(event.getContextId(), destinationVariable.getJobId(), destinationVariable.getPortId(), value, link.getPosition(), event.getEventGroupId());
                eventProcessor.send(updateInputEvent);
              }
            } else {
              value = value != null? value : event.getValue();
              if (isValueFromScatterStrategy) {
                Event updateOutputEvent = new OutputUpdateEvent(event.getContextId(), destinationVariable.getJobId(), destinationVariable.getPortId(), value, false, 1, 1, event.getEventGroupId());
                eventProcessor.send(updateOutputEvent);
              } else {
                int numberOfScattered = sourceJob.getNumberOfGlobalOutputs();
                Event updateOutputEvent = new OutputUpdateEvent(event.getContextId(), destinationVariable.getJobId(), destinationVariable.getPortId(), value, true, numberOfScattered, event.getPosition(), event.getEventGroupId());
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
        
        value = sourceVariable.getValue();
        for (VariableRecord destinationVariable : destinationVariables) {
          switch (destinationVariable.getType()) {
          case INPUT:
            Event updateInputEvent = new InputUpdateEvent(event.getContextId(), destinationVariable.getJobId(), destinationVariable.getPortId(), value, link.getPosition(), event.getEventGroupId());
            eventProcessor.send(updateInputEvent);
            break;
          case OUTPUT:
            if (sourceJob.isScattered()) {
              int numberOfScattered = sourceJob.getNumberOfGlobalOutputs();
              int position = InternalSchemaHelper.getScatteredNumber(sourceJob.getId());
              Event updateOutputEvent = new OutputUpdateEvent(event.getContextId(), destinationVariable.getJobId(), destinationVariable.getPortId(), value, true, numberOfScattered, position, event.getEventGroupId());
              eventProcessor.send(updateOutputEvent);
            } else {
              Event updateOutputEvent = new OutputUpdateEvent(event.getContextId(), destinationVariable.getJobId(), destinationVariable.getPortId(), value, link.getPosition(), event.getEventGroupId());
              eventProcessor.send(updateOutputEvent);
            }
            break;
          }
        }
      }
      
      if (event.getEventGroupId() != null && event.getEventGroupId().equals(sourceJob.getExternalId()) && sourceJob.isCompleted()) {
        Set<Job> readyJobs = jobGroupsDB.get(event.getContextId(), event.getEventGroupId());
        try {
          engineStatusCallback.onJobsReady(readyJobs);
        } catch (EngineStatusCallbackException e) {
          logger.error("Failed to call onJobsReady() callback", e);
          throw new EventHandlerException(e);
        }
      }
    }
  }
  
  private Job createRootJob(JobRecord jobRecord, JobStatus status) {
    Map<String, Object> outputs = new HashMap<>();
    List<VariableRecord> outputVariables = variableService.find(jobRecord.getId(), LinkPortType.OUTPUT, jobRecord.getRootId());
    for (VariableRecord outputVariable : outputVariables) {
      Object value = CloneHelper.deepCopy(outputVariable.getValue());
      outputs.put(outputVariable.getPortId(), value);
    }
    return JobHelper.createRootJob(jobRecord, status, jobService, variableService, linkService, contextService, dagNodeDB, outputs);
  }
  
}
