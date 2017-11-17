package org.rabix.engine.processor.handler.impl;

import com.google.inject.Inject;
import org.apache.commons.configuration.Configuration;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.helper.URIHelper;
import org.rabix.bindings.model.Application;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.bindings.model.LinkMerge;
import org.rabix.bindings.model.dag.DAGContainer;
import org.rabix.bindings.model.dag.DAGLink;
import org.rabix.bindings.model.dag.DAGLinkPort;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.helper.CloneHelper;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.common.helper.JSONHelper;
import org.rabix.engine.JobHelper;
import org.rabix.engine.event.Event;
import org.rabix.engine.event.impl.ContextStatusEvent;
import org.rabix.engine.event.impl.InputUpdateEvent;
import org.rabix.engine.event.impl.JobStatusEvent;
import org.rabix.engine.event.impl.OutputUpdateEvent;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.handler.EventHandler;
import org.rabix.engine.processor.handler.EventHandlerException;
import org.rabix.engine.service.*;
import org.rabix.engine.store.model.*;
import org.rabix.engine.store.model.ContextRecord.ContextStatus;
import org.rabix.engine.store.model.JobRecord.PortCounter;
import org.rabix.engine.store.repository.JobRepository;
import org.rabix.engine.validator.JobStateValidationException;
import org.rabix.engine.validator.JobStateValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class JobStatusEventHandler implements EventHandler<JobStatusEvent> {

  private final Logger logger = LoggerFactory.getLogger(JobStatusEventHandler.class);

  private final DAGNodeService dagNodeService;
  private final AppService appService;
  private final ScatterHandler scatterHelper;
  private final EventProcessor eventProcessor;

  private final JobRecordService jobRecordService;
  private final LinkRecordService linkRecordService;
  private final VariableRecordService variableRecordService;
  private final ContextRecordService contextRecordService;
  private final JobStatsRecordService jobStatsRecordService;

  private final JobRepository jobRepository;
  private final JobService jobService;

  private final boolean setResources;
  private JobHelper jobHelper;

  @Inject
  public JobStatusEventHandler(final DAGNodeService dagNodeService, final AppService appService,
      final JobRecordService jobRecordService, final LinkRecordService linkRecordService,
      final VariableRecordService variableRecordService, final ContextRecordService contextRecordService,
      final EventProcessor eventProcessor, final ScatterHandler scatterHelper, final JobRepository jobRepository,
      final JobService jobService, final JobStatsRecordService jobStatsRecordService,
      final Configuration configuration, final JobHelper jobHelper) {
    this.dagNodeService = dagNodeService;
    this.scatterHelper = scatterHelper;
    this.eventProcessor = eventProcessor;
    this.jobRecordService = jobRecordService;
    this.linkRecordService = linkRecordService;
    this.contextRecordService = contextRecordService;
    this.jobStatsRecordService = jobStatsRecordService;
    this.variableRecordService = variableRecordService;
    this.appService = appService;
    this.jobService = jobService;
    this.jobHelper = jobHelper;

    this.jobRepository = jobRepository;
    this.setResources = configuration.getBoolean("engine.set_resources", false);
  }

  @Override
  public void handle(JobStatusEvent event, EventHandlingMode mode) throws EventHandlerException {
    JobRecord jobRecord = jobRecordService.find(event.getJobId(), event.getContextId());
    if (jobRecord == null) {
      logger.info("Possible stale message. Job {} for root {} doesn't exist.", event.getJobId(), event.getContextId());
      return;
    }

    JobStatsRecord jobStatsRecord = null;
    if ((jobRecord.getParentId() != null && jobRecord.getParentId().equals(jobRecord.getRootId())) || (jobRecord.isRoot()))
      jobStatsRecord = jobStatsRecordService.findOrCreate(jobRecord.getRootId());
    try {
      JobStateValidator.checkState(jobRecord, event.getState());
    } catch (JobStateValidationException e) {
      logger.warn("Cannot transition from state {} to {}", jobRecord.getState(), event.getState());
      return;
    }

    switch (event.getState()) {
    case READY:
      ready(jobRecord, event);
      if (jobRecord.getState().equals(JobRecord.JobState.COMPLETED)) {
        break;
      }
      if (jobRecord.getScatterStrategy() != null && jobRecord.getScatterStrategy().skipScatter()) {
        break;
      }
      if (shouldGenerateReadyJob(mode, jobRecord)) {
        Job job = null;
        try {
          job = jobHelper.createReadyJob(jobRecord, JobStatus.READY, setResources);
          if (!job.getName().equals(InternalSchemaHelper.ROOT_NAME)) {
            jobRepository.insert(job, event.getEventGroupId(), event.getProducedByNode());
          } else {
            jobRepository.update(job);
          }
        } catch (BindingException e1) {
          // FIXME: is this really safe to ignore?
          logger.info("Failed to create job", e1);
        }
        if (job.isRoot()) {
          jobService.handleJobContainerReady(job);
        }
      } else {
        Job containerJob = null;
        try {
          containerJob = jobHelper.createJob(jobRecord, JobStatus.READY, false);
        } catch (BindingException e) {
          throw new EventHandlerException("Failed to call onReady callback for Job " + containerJob, e);
        }
        jobService.handleJobContainerReady(containerJob);
      }
      break;
    case RUNNING:
      jobRecord.setState(JobRecord.JobState.RUNNING);
      jobRecordService.update(jobRecord);
      if (jobStatsRecord != null) {
        jobStatsRecord.increaseRunning();
        jobStatsRecordService.update(jobStatsRecord);
      }
      break;
    case COMPLETED:
      jobRecord.setState(JobRecord.JobState.COMPLETED);
      jobRecordService.update(jobRecord);
      if (jobStatsRecord != null) {
        jobStatsRecord.increaseCompleted();
        jobStatsRecordService.update(jobStatsRecord);
      }

      if ((!jobRecord.isScatterWrapper() || jobRecord.isRoot()) && !jobRecord.isContainer()) {
        for (PortCounter portCounter : jobRecord.getOutputCounters()) {
          Object output = event.getResult().get(portCounter.getPort());
          eventProcessor.send(new OutputUpdateEvent(jobRecord.getRootId(), jobRecord.getId(), portCounter.getPort(), output,
              jobRecord.getNumberOfGlobalOutputs(), 1, event.getEventGroupId(), event.getProducedByNode()));
        }
      }
      if (jobRecord.isRoot()) {
        eventProcessor.send(new ContextStatusEvent(event.getContextId(), ContextStatus.COMPLETED));
        try {
          Job rootJob = jobHelper.createJob(jobRecord, JobStatus.COMPLETED, event.getResult());
          if(!jobRecord.isContainer())
            jobService.handleJobRootPartiallyCompleted(jobRecord.getRootId(), rootJob.getOutputs(), jobRecord.getId());
          jobService.handleJobRootCompleted(rootJob);
        } catch (BindingException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      } else {
        if (!jobRecord.isScattered()) {
          List<LinkRecord> rootLinks = linkRecordService
                  .findBySourceAndSourceType(jobRecord.getId(), LinkPortType.OUTPUT, jobRecord.getRootId())
                  .stream()
                  .filter(p -> p.getDestinationJobId().equals(InternalSchemaHelper.ROOT_NAME))
                  .collect(Collectors.toList());
          Map<String, Object> outs = new HashMap<>();
          rootLinks.stream().forEach(link -> {
            outs.put(link.getDestinationJobPort(), variableRecordService
                .find(InternalSchemaHelper.ROOT_NAME, link.getDestinationJobPort(), LinkPortType.OUTPUT, jobRecord.getRootId()).getValue());
          });
          if (!outs.isEmpty()) {
            jobService.handleJobRootPartiallyCompleted(jobRecord.getRootId(), outs, jobRecord.getId());
          }
          try {
            jobService.handleJobCompleted(jobHelper.createJob(jobRecord, JobStatus.COMPLETED, event.getResult()));
          } catch (BindingException e) {
          }
        }
      }
      break;
    case ABORTED:
      Set<JobRecord.JobState> jobRecordStatuses = new HashSet<>();
      jobRecordStatuses.add(JobRecord.JobState.PENDING);
      jobRecordStatuses.add(JobRecord.JobState.READY);
      jobRecordStatuses.add(JobRecord.JobState.RUNNING);

      List<JobRecord> records = jobRecordService.find(jobRecord.getRootId(), jobRecordStatuses);
      for (JobRecord record : records) {
        record.setState(JobRecord.JobState.ABORTED);
        jobRecordService.update(record);
      }

      ContextRecord contextRecord = contextRecordService.find(jobRecord.getRootId());
      contextRecord.setStatus(ContextStatus.ABORTED);
      contextRecordService.update(contextRecord);
      break;
    case FAILED:
      jobRecord.setState(JobRecord.JobState.READY);
      jobRecordService.update(jobRecord);

      if (jobRecord.isRoot()) {
        try {
          Job rootJob = jobHelper.createJob(jobRecord, JobStatus.FAILED, null);
          rootJob = Job.cloneWithMessage(rootJob, event.getMessage());
          jobService.handleJobRootFailed(rootJob);

          eventProcessor.send(new ContextStatusEvent(event.getContextId(), ContextStatus.FAILED));
        } catch (Exception e) {
          throw new EventHandlerException("Failed to call onRootFailed callback for Job " + jobRecord.getRootId(), e);
        }
      } else {
        try {
          Job failedJob = jobHelper.createJob(jobRecord, JobStatus.FAILED);
          failedJob = Job.cloneWithMessage(failedJob, event.getMessage());
          jobService.handleJobFailed(failedJob);

          eventProcessor.send(new JobStatusEvent(InternalSchemaHelper.ROOT_NAME, event.getContextId(), JobRecord.JobState.FAILED, event.getMessage(), event.getEventGroupId(), event.getProducedByNode()));
        } catch (Exception e) {
          throw new EventHandlerException("Failed to call onFailed callback for Job " + jobRecord.getId(), e);
        }
      }
      break;
    default:
      break;
    }
  }

  private boolean shouldGenerateReadyJob(EventHandlingMode mode, JobRecord jobRecord) {
    return mode != EventHandlingMode.REPLAY && !jobRecord.isContainer() && !jobRecord.isScatterWrapper();
  }

  /*
   * Job is ready
   */
  private void ready(JobRecord job, Event event) throws EventHandlerException {
    job.setState(JobRecord.JobState.READY);

    UUID rootId = event.getContextId();
    DAGNode node = dagNodeService.get(InternalSchemaHelper.normalizeId(job.getId()), rootId, job.getDagHash());

    if (!job.isScattered() && job.getScatterPorts().size() > 0) {
      job.setState(JobRecord.JobState.RUNNING);

      for (String port : job.getScatterPorts()) {
        VariableRecord variable = variableRecordService.find(job.getId(), port, LinkPortType.INPUT, rootId);
        scatterHelper.scatterPort(job, event, port, variableRecordService.getValue(variable), 1, null, false, false);
        if (job.getScatterStrategy().skipScatter()) {
          return;
        }
      }
    } else if (job.isContainer()) {
      job.setState(JobRecord.JobState.RUNNING);

      DAGContainer containerNode = (DAGContainer) node;
      rollOutContainer(job, containerNode, rootId);
      handleTransform(job, containerNode);

      List<LinkRecord> containerLinks = linkRecordService.findBySourceAndSourceType(job.getId(), LinkPortType.INPUT, rootId);
      if (containerLinks.isEmpty()) {
        Set<String> immediateReadyNodeIds = findImmediateReadyNodes(containerNode);
        for (String readyNodeId : immediateReadyNodeIds) {
          JobRecord childJobRecord = jobRecordService.find(readyNodeId, rootId);
          if(childJobRecord.isContainer() || childJobRecord.isScatterWrapper()) {
            ready(childJobRecord, event);
          }
          else {
            JobStatusEvent jobStatusEvent = new JobStatusEvent(childJobRecord.getId(), rootId, JobRecord.JobState.READY, event.getEventGroupId(), event.getProducedByNode());
            eventProcessor.send(jobStatusEvent);
          }
        }
      } else {
        for (LinkRecord link : containerLinks) {
          VariableRecord sourceVariable = variableRecordService.find(link.getSourceJobId(), link.getSourceJobPort(), LinkPortType.INPUT, rootId);
          VariableRecord destinationVariable = variableRecordService.find(link.getDestinationJobId(), link.getDestinationJobPort(), LinkPortType.INPUT, rootId);
          if(destinationVariable == null) {
            destinationVariable = new VariableRecord(rootId, link.getDestinationJobId(), link.getDestinationJobPort(), LinkPortType.INPUT, variableRecordService.getValue(sourceVariable), node.getLinkMerge(sourceVariable.getPortId(), sourceVariable.getType()));
            variableRecordService.create(destinationVariable);
          }
          Event updateEvent = new InputUpdateEvent(rootId, link.getDestinationJobId(), link.getDestinationJobPort(), variableRecordService.getValue(sourceVariable), link.getPosition(), event.getEventGroupId(), event.getProducedByNode());
          eventProcessor.send(updateEvent);
        }
      }
    }
  }

  private void handleTransform(JobRecord job, DAGNode node) throws EventHandlerException {
    try {
      boolean hasTransform = false;
      for (DAGLinkPort p : node.getInputPorts()) {
        if (p.getTransform() != null) {
          hasTransform = true;
          break;
        }
      }
      if (!hasTransform) {
        return;
      }

      Application app = appService.get(node.getAppHash());

      Bindings bindings = null;
      if (node.getProtocolType() != null) {
        bindings = BindingsFactory.create(node.getProtocolType());
      } else {
        String encodedApp = URIHelper.createDataURI(JSONHelper.writeObject(appService.get(node.getAppHash())));
        bindings = BindingsFactory.create(encodedApp);
      }

      List<VariableRecord> inputVariables = variableRecordService.find(job.getId(), LinkPortType.INPUT, job.getRootId());
      Map<String, Object> preprocesedInputs = new HashMap<>();
      for (VariableRecord inputVariable : inputVariables) {
        Object value = variableRecordService.getValue(inputVariable);
        preprocesedInputs.put(inputVariable.getPortId(), value);
      }

      for (VariableRecord inputVariable : inputVariables) {
        Object value = CloneHelper.deepCopy(variableRecordService.getValue(inputVariable));
        for (DAGLinkPort p : node.getInputPorts()) {
          if (p.getId().equals(inputVariable.getPortId())) {
            if (p.getTransform() != null) {
              Object transform = p.getTransform();
              if (transform != null) {
                value = bindings.transformInputs(value, new Job(JSONHelper.writeObject(app), preprocesedInputs), transform);
                inputVariable.setValue(value);
                variableRecordService.update(inputVariable);
              }
            }
          }
        }
      }
    } catch (BindingException e) {
      throw new EventHandlerException("Failed to set evaluate transform", e);
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
  private void rollOutContainer(JobRecord job, DAGContainer containerNode, UUID contextId) {
    for (DAGNode node : containerNode.getChildren()) {
      String newJobId = InternalSchemaHelper.concatenateIds(job.getId(), InternalSchemaHelper.getLastPart(node.getId()));

      JobRecord childJob = scatterHelper.createJobRecord(newJobId, job.getExternalId(), node, false, contextId, job.getDagHash());
      jobRecordService.create(childJob);

      for (DAGLinkPort port : node.getInputPorts()) {
        if (port.getTransform() != null) {
          childJob.setBlocking(true);
        }
        VariableRecord childVariable = new VariableRecord(contextId, newJobId, port.getId(), LinkPortType.INPUT, port.getDefaultValue(), node.getLinkMerge(port.getId(), port.getType()));
        variableRecordService.create(childVariable);
      }

      for (DAGLinkPort port : node.getOutputPorts()) {
        VariableRecord childVariable = new VariableRecord(contextId, newJobId, port.getId(), LinkPortType.OUTPUT, null, node.getLinkMerge(port.getId(), port.getType()));
        variableRecordService.create(childVariable);
      }
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
      if (job.getState().equals(JobRecord.JobState.PENDING)) {
        jobRecordService.incrementPortCounter(job, linkPort, LinkPortType.INPUT);
        jobRecordService.increaseInputPortIncoming(job, linkPort.getId());

        if (job.getInputPortIncoming(linkPort.getId()) > 1) {
          if (LinkMerge.isBlocking(linkPort.getLinkMerge())) {
            job.setBlocking(true);
          }
        }
      }
    } else {
      jobRecordService.increaseOutputPortIncoming(job, linkPort.getId());
      jobRecordService.incrementPortCounter(job, linkPort, LinkPortType.OUTPUT);
      if (isSource) {
        job.getOutputCounter(linkPort.getId()).updatedAsSource(1);
      }
    }
    jobRecordService.update(job);
  }

}
