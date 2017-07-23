package org.rabix.engine.processor.handler.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.common.functional.FunctionalHelper.Recursive;
import org.rabix.common.helper.CloneHelper;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.engine.JobHelper;
import org.rabix.engine.event.Event;
import org.rabix.engine.event.impl.InputUpdateEvent;
import org.rabix.engine.event.impl.JobStatusEvent;
import org.rabix.engine.event.impl.OutputUpdateEvent;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.handler.EventHandler;
import org.rabix.engine.processor.handler.EventHandlerException;
import org.rabix.engine.service.AppService;
import org.rabix.engine.service.ContextRecordService;
import org.rabix.engine.service.DAGNodeService;
import org.rabix.engine.service.JobRecordService;
import org.rabix.engine.service.JobService;
import org.rabix.engine.service.JobStatsRecordService;
import org.rabix.engine.service.LinkRecordService;
import org.rabix.engine.service.VariableRecordService;
import org.rabix.engine.store.model.JobRecord;
import org.rabix.engine.store.model.JobStatsRecord;
import org.rabix.engine.store.model.LinkRecord;
import org.rabix.engine.store.model.VariableRecord;
import org.rabix.engine.store.model.scatter.ScatterStrategy;
import org.rabix.engine.store.model.scatter.ScatterStrategy.JobPortPair;

import com.google.inject.Inject;

/**
 * Handles {@link OutputUpdateEvent} events.
 */
public class OutputEventHandler implements EventHandler<OutputUpdateEvent> {

  private JobRecordService jobRecordService;
  private LinkRecordService linkService;
  private VariableRecordService variableService;
  private ContextRecordService contextService;
  private JobStatsRecordService jobStatsRecordService;
  private final EventProcessor eventProcessor;
  
  private DAGNodeService dagNodeService;
  private AppService appService;
  private JobService jobService;
  
  @Inject
  public OutputEventHandler(EventProcessor eventProcessor, JobRecordService jobRecordService,
      VariableRecordService variableService, LinkRecordService linkService, ContextRecordService contextService,
      DAGNodeService dagNodeService, AppService appService, JobService jobService,
      JobStatsRecordService jobStatsRecordService) {
    this.dagNodeService = dagNodeService;
    this.appService = appService;
    this.jobRecordService = jobRecordService;
    this.linkService = linkService;
    this.contextService = contextService;
    this.variableService = variableService;
    this.eventProcessor = eventProcessor;
    this.jobService = jobService;
    this.jobStatsRecordService = jobStatsRecordService;
  }

  public void handle(final OutputUpdateEvent event) throws EventHandlerException {
    JobRecord sourceJob = jobRecordService.find(event.getJobId(), event.getContextId());
    if (sourceJob.getState().equals(JobRecord.JobState.COMPLETED)) {
      return;
    }
    if (event.isFromScatter()) {
      jobRecordService.resetOutputPortCounter(sourceJob, event.getNumberOfScattered(), event.getPortId());
    }
    VariableRecord sourceVariable = variableService.find(event.getJobId(), event.getPortId(), LinkPortType.OUTPUT, event.getContextId());
    jobRecordService.decrementPortCounter(sourceJob, event.getPortId(), LinkPortType.OUTPUT);
    variableService.addValue(sourceVariable, event.getValue(), event.getPosition(), event.isFromScatter());
    variableService.update(sourceVariable); // TODO wha?
    jobRecordService.update(sourceJob);
    
    if (sourceJob.isCompleted()) {
      if(sourceJob.getOutputCounter(sourceVariable.getPortId()) != null) {
        if ((sourceJob.isContainer() || sourceJob.isScatterWrapper()) &&
            sourceJob.getParentId() != null && sourceJob.getParentId().equals(sourceJob.getRootId())) {
          JobStatsRecord jobStatsRecord = jobStatsRecordService.findOrCreate(sourceJob.getRootId());
          jobStatsRecord.increaseCompleted();
          jobStatsRecord.increaseRunning();
          jobStatsRecordService.update(jobStatsRecord);
        }

        if (sourceJob.isRoot()) {
          Job rootJob = createRootJob(sourceJob, JobHelper.transformStatus(sourceJob.getState()));
          if (!event.isFromScatter() || (event.getNumberOfScattered() == sourceVariable.getNumberOfTimesUpdated())) {
            jobService.handleJobRootPartiallyCompleted(rootJob, event.isFromScatter() ? InternalSchemaHelper.getJobIdFromScatteredId(event.getProducedByNode()) : event.getProducedByNode());
          }
          if (sourceJob.isContainer()) {
            eventProcessor.send(
                new JobStatusEvent(sourceJob.getId(), event.getContextId(), JobRecord.JobState.COMPLETED, rootJob.getOutputs(), event.getEventGroupId(), event.getProducedByNode()));
          }
          return;
        }
        else {
          try {
            Job completedJob = JobHelper.createCompletedJob(sourceJob, JobStatus.COMPLETED, jobRecordService, variableService, linkService, contextService, dagNodeService, appService);
            jobService.handleJobCompleted(completedJob);
          } catch (BindingException e) {
          }
        }
      }
    }

    if(sourceJob.isRoot() && (!event.isFromScatter() || (event.getNumberOfScattered()==sourceVariable.getNumberOfTimesUpdated()))){
        jobService.handleJobRootPartiallyCompleted(createRootJob(sourceJob, JobHelper.transformStatus(sourceJob.getState())), event.isFromScatter() ? InternalSchemaHelper.getJobIdFromScatteredId(event.getProducedByNode()) : event.getProducedByNode());
    }
    
    Object value = null;
    
    if (sourceJob.isScatterWrapper()) {
      ScatterStrategy scatterStrategy = sourceJob.getScatterStrategy();
      
      boolean isValueFromScatterStrategy = false;
      if (scatterStrategy.isBlocking() || scatterStrategy.isEmptyListDetected()) {
        if (sourceJob.isOutputPortReady(event.getPortId())) {
          isValueFromScatterStrategy = true;

          List<Object> valueStructure = scatterStrategy.valueStructure(sourceJob.getId(), event.getPortId(), event.getContextId());
          value = Recursive.make(jp -> {
            JobPortPair jobPair = (JobPortPair) jp;
            VariableRecord variableRecord = variableService.find(jobPair.getJobId(), jobPair.getPortId(), LinkPortType.OUTPUT, event.getContextId());
            return variableService.getValue(variableRecord);
          }).apply(valueStructure);
        } else {
          return;
        }
      }
      
      List<LinkRecord> links = linkService.findBySourceAndSourceType(sourceVariable.getJobId(), sourceVariable.getPortId(), LinkPortType.OUTPUT, event.getContextId());
      for (LinkRecord link : links) {
        if (!isValueFromScatterStrategy) {
          value = null; // reset
        }
        VariableRecord destinationVariable = variableService.find(link.getDestinationJobId(), link.getDestinationJobPort(), link.getDestinationVarType(), event.getContextId());

        JobRecord destinationJob = null;
        boolean isDestinationPortScatterable = false;
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
              value = value != null ? value : variableService.getValue(sourceVariable);
              Event updateInputEvent = new OutputUpdateEvent(event.getContextId(), destinationVariable.getJobId(), destinationVariable.getPortId(), value, link.getPosition(), event.getEventGroupId(), event.getProducedByNode());
              eventProcessor.send(updateInputEvent);
            }
          } else {
            value = value != null ? value : event.getValue();
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
      return;
    }
    
    if (sourceJob.isOutputPortReady(event.getPortId())) {
      List<LinkRecord> links = linkService.findBySourceAndSourceType(event.getJobId(), event.getPortId(),
          LinkPortType.OUTPUT, event.getContextId());
      for (LinkRecord link : links) {
        VariableRecord destinationVariable = variableService.find(link.getDestinationJobId(), link.getDestinationJobPort(), link.getDestinationVarType(), event.getContextId());
        value = variableService.getValue(sourceVariable);
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
  
  private Job createRootJob(JobRecord jobRecord, JobStatus status) {
    Map<String, Object> outputs = new HashMap<>();
    List<VariableRecord> outputVariables = variableService.find(jobRecord.getId(), LinkPortType.OUTPUT, jobRecord.getRootId());
    for (VariableRecord outputVariable : outputVariables) {
      Object value = CloneHelper.deepCopy(variableService.getValue(outputVariable));
      outputs.put(outputVariable.getPortId(), value);
    }
    return JobHelper.createRootJob(jobRecord, status, jobRecordService, variableService, linkService, contextService, dagNodeService, appService, outputs);
  }
  
}