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
import org.rabix.engine.model.JobRecord.PortCounter;
import org.rabix.engine.model.JobStatsRecord;
import org.rabix.engine.model.LinkRecord;
import org.rabix.engine.model.VariableRecord;
import org.rabix.engine.model.scatter.ScatterStrategy;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.handler.EventHandler;
import org.rabix.engine.processor.handler.EventHandlerException;
import org.rabix.engine.service.ContextRecordService;
import org.rabix.engine.service.JobRecordService;
import org.rabix.engine.service.JobService;
import org.rabix.engine.service.JobStatsRecordService;
import org.rabix.engine.service.LinkRecordService;
import org.rabix.engine.service.VariableRecordService;
import org.rabix.engine.service.impl.JobRecordServiceImpl.JobState;

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

  private DAGNodeDB dagNodeDB;
  private AppDB appDB;
  private JobService jobService;

  @Inject
  public OutputEventHandler(EventProcessor eventProcessor, JobRecordService jobRecordService, VariableRecordService variableService,
      LinkRecordService linkService, ContextRecordService contextService, DAGNodeDB dagNodeDB, AppDB appDB, JobService jobService,
      JobStatsRecordService jobStatsRecordService) {
    this.dagNodeDB = dagNodeDB;
    this.appDB = appDB;
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
    if (sourceJob.getState().equals(JobState.COMPLETED)) {
      return;
    }
    if (sourceJob.isScatterWrapper()) {
      jobRecordService.resetOutputPortCounter(sourceJob, event.getNumberOfScattered(), event.getPortId());
    }
    VariableRecord sourceVariable = variableService.find(event.getJobId(), event.getPortId(), LinkPortType.OUTPUT, event.getContextId());
    jobRecordService.decrementPortCounter(sourceJob, event.getPortId(), LinkPortType.OUTPUT);
    variableService.addValue(sourceVariable, event.getValue(), event.getPosition(), sourceJob.isScatterWrapper() && !sourceJob.getScatterStrategy().isEmptyListDetected());
    variableService.update(sourceVariable); // TODO wha?
    jobRecordService.update(sourceJob);

    Boolean isScatterWrapper = sourceJob.isScatterWrapper();

    if (sourceJob.isCompleted()) {
      if (sourceJob.getOutputCounter(sourceVariable.getPortId()) != null) {
        if ((sourceJob.isContainer() || isScatterWrapper) && sourceJob.getParentId() != null && sourceJob.getParentId().equals(sourceJob.getRootId())) {
          JobStatsRecord jobStatsRecord = jobStatsRecordService.findOrCreate(sourceJob.getRootId());
          jobStatsRecord.increaseCompleted();
          jobStatsRecord.increaseRunning();
          jobStatsRecordService.update(jobStatsRecord);
        }

        if (sourceJob.isRoot()) {
          Job rootJob = createRootJob(sourceJob, JobHelper.transformStatus(sourceJob.getState()));
          if (!event.isFromScatter() || (event.getNumberOfScattered() == sourceVariable.getNumberOfTimesUpdated())) {
            jobService.handleJobRootPartiallyCompleted(rootJob,
                event.isFromScatter() ? InternalSchemaHelper.getJobIdFromScatteredId(event.getProducedByNode()) : event.getProducedByNode());
          }
          if (sourceJob.isContainer()) {
            eventProcessor.send(new JobStatusEvent(sourceJob.getId(), event.getContextId(), JobState.COMPLETED, rootJob.getOutputs(), event.getEventGroupId(),
                event.getProducedByNode()));
          }
          return;
        } else {
          try {
            Job completedJob = JobHelper.createCompletedJob(sourceJob, JobStatus.COMPLETED, jobRecordService, variableService, linkService, contextService,
                dagNodeDB, appDB);
            jobService.handleJobCompleted(completedJob);
          } catch (BindingException e) {
          }
        }
      }
    }

    if (sourceJob.isRoot() && (!event.isFromScatter() || (event.getNumberOfScattered() == sourceVariable.getNumberOfTimesUpdated()))) {
      jobService.handleJobRootPartiallyCompleted(createRootJob(sourceJob, JobHelper.transformStatus(sourceJob.getState())),
          event.isFromScatter() ? InternalSchemaHelper.getJobIdFromScatteredId(event.getProducedByNode()) : event.getProducedByNode());
    }

    Object value = variableService.getValue(sourceVariable);
    boolean valueFromScatterStrategy = false;
    PortCounter outputCounter = sourceJob.getOutputCounter(event.getPortId());
    Integer numberOfGlobalOutputs = outputCounter == null ? 0 : outputCounter.getGlobalCounter();
    
    if (isScatterWrapper) {
      numberOfGlobalOutputs = sourceJob.getNumberOfGlobalOutputs();
      ScatterStrategy scatterStrategy = sourceJob.getScatterStrategy();
      if (scatterStrategy.isBlocking()) {
        if (sourceJob.isOutputPortReady(event.getPortId())) {
          valueFromScatterStrategy = true;
          value = scatterStrategy.values(variableService, sourceJob.getId(), event.getPortId(), event.getContextId());
        } else {
          return;
        }
      }
    }

    List<LinkRecord> links = linkService.findBySourceAndSourceType(sourceVariable.getJobId(), sourceVariable.getPortId(), LinkPortType.OUTPUT,
        event.getContextId());

    for (LinkRecord link : links) {
      VariableRecord destinationVariable = variableService.find(link.getDestinationJobId(), link.getDestinationJobPort(), link.getDestinationVarType(),
          event.getContextId());
      JobRecord destinationJob = jobRecordService.find(destinationVariable.getJobId(), destinationVariable.getRootId());
      boolean isDestinationPortScatterable = false;

      Event newEvent = null;
      switch (destinationVariable.getType()) {
        case INPUT:
          boolean lookAhead = false;
          if (isScatterWrapper) {
            isDestinationPortScatterable = destinationJob.isScatterPort(destinationVariable.getPortId());
            if (isDestinationPortScatterable && !destinationJob.isBlocking() && !(destinationJob.getInputPortIncoming(event.getPortId()) > 1)) {
              value = event.getValue();
              lookAhead = true;
            } else {
              if (!sourceJob.isOutputPortReady(event.getPortId())) {
                break;
              }
            }
          }
          newEvent = propagateInput(event, destinationVariable, value, lookAhead, numberOfGlobalOutputs, link.getPosition());
          break;
        case OUTPUT:
          if(sourceJob.isOutputPortReady(event.getPortId()) || sourceJob.isScattered())
            newEvent = propagateOutput(event, destinationVariable, value, numberOfGlobalOutputs, link.getPosition());
          break;
      }
      if (newEvent != null)
        eventProcessor.send(newEvent);
    }
  }

  private OutputUpdateEvent propagateOutput(OutputUpdateEvent event, VariableRecord variable, Object value, Integer position) {
    return propagateOutput(event, variable, value, null, position);
  }

  private OutputUpdateEvent propagateOutput(OutputUpdateEvent event, VariableRecord variable, Object value, Integer numberOfScattered, Integer position) {
    return new OutputUpdateEvent(event.getContextId(), variable.getJobId(), variable.getPortId(), value, numberOfScattered, position, event.getEventGroupId(),
        event.getProducedByNode());
  }

  private InputUpdateEvent propagateInput(OutputUpdateEvent event, VariableRecord variable, Object value, boolean lookAhead, Integer numberOfScattered,
      Integer position) {
    return new InputUpdateEvent(event.getContextId(), variable.getJobId(), variable.getPortId(), value, lookAhead, numberOfScattered, position,
        event.getEventGroupId(), event.getProducedByNode());
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
