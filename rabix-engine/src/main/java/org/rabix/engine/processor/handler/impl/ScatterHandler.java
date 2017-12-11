package org.rabix.engine.processor.handler.impl;

import com.google.inject.Inject;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.model.LinkMerge;
import org.rabix.bindings.model.ScatterMethod;
import org.rabix.bindings.model.dag.DAGContainer;
import org.rabix.bindings.model.dag.DAGLinkPort;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.engine.event.Event;
import org.rabix.engine.event.impl.InputUpdateEvent;
import org.rabix.engine.event.impl.OutputUpdateEvent;
import org.rabix.engine.event.impl.ScatterJobEvent;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.handler.EventHandlerException;
import org.rabix.engine.service.DAGNodeService;
import org.rabix.engine.service.JobRecordService;
import org.rabix.engine.service.LinkRecordService;
import org.rabix.engine.service.VariableRecordService;
import org.rabix.engine.store.model.JobRecord;
import org.rabix.engine.store.model.JobRecord.PortCounter;
import org.rabix.engine.store.model.LinkRecord;
import org.rabix.engine.store.model.VariableRecord;
import org.rabix.engine.store.model.scatter.RowMapping;
import org.rabix.engine.store.model.scatter.ScatterStrategy;
import org.rabix.engine.store.model.scatter.ScatterStrategyException;
import org.rabix.engine.store.model.scatter.ScatterStrategyFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ScatterHandler {

  private final DAGNodeService dagNodeService;
  private final EventProcessor eventProcessor;

  private final JobRecordService jobRecordService;
  private final LinkRecordService linkRecordService;
  private final VariableRecordService variableRecordService;
  private final ScatterStrategyFactory scatterStrategyFactory;

  @Inject
  public ScatterHandler(final DAGNodeService dagNodeService, final JobRecordService jobRecordService,
      final VariableRecordService variableRecordService, final LinkRecordService linkRecordService,
      final EventProcessor eventProcessor, final ScatterStrategyFactory scatterStrategyFactory) {
    this.dagNodeService = dagNodeService;
    this.eventProcessor = eventProcessor;
    this.jobRecordService = jobRecordService;
    this.linkRecordService = linkRecordService;
    this.variableRecordService = variableRecordService;
    this.scatterStrategyFactory = scatterStrategyFactory;
  }

  /*
   * Scatters port
   */
  @SuppressWarnings("unchecked")
  void scatterPort(JobRecord job, Event event, String portId, Object value, Integer position, Integer numberOfScatteredFromEvent, boolean isLookAhead, boolean isFromEvent) throws EventHandlerException {
    eventProcessor.persist(new ScatterJobEvent(job.getRootId(), job.getExternalId(), job.getId(), event, portId, value, position, numberOfScatteredFromEvent, isLookAhead, isFromEvent));

    job.setScatterWrapper(true);
    ScatterStrategy scatterStrategy = job.getScatterStrategy();
    if (scatterStrategy != null && scatterStrategy.skipScatter()) {
      return;
    }

    DAGNode node = dagNodeService.get(InternalSchemaHelper.normalizeId(job.getId()), job.getRootId(), job.getDagHash());
    if (job.getScatterStrategy() == null) {
      try {
        scatterStrategy = scatterStrategyFactory.create(node);
        job.setScatterStrategy(scatterStrategy);
      } catch (BindingException e) {
        throw new EventHandlerException(e);
      }
    }

    if(value instanceof List<?> && ((List<?>)value).isEmpty()){
      scatterStrategy.setEmptyListDetected();
    }

    if (isLookAhead && !scatterStrategy.isEmptyListDetected()) {
      int numberOfScattered = getNumberOfScattered(job, numberOfScatteredFromEvent);
      createScatteredJobs(job, event, portId, value, node, numberOfScattered, position);
      return;
    }

    if (value == null) {
      createScatteredJobs(job, event, portId, value, node, 1, position);
      return;
    }

    List<Object> values = null;
    boolean usePositionFromEvent = true;
    if (isFromEvent || !(value instanceof List<?>)) {
      if (job.getInputPortIncoming(portId) == 1) {
        usePositionFromEvent = false;
        if (!(value instanceof List<?>)) {
          throw new EventHandlerException("Port " + portId + " for " + job.getId() + " and rootId " + job.getRootId() + " is not a list and therefore cannot be scattered.");
        }
        values = (List<Object>) value;
      } else {
        values = new ArrayList<>();
        values.add(value);
      }
    } else {
      usePositionFromEvent = false;
      values = (List<Object>) value;
    }

    if (!scatterStrategy.isEmptyListDetected()) {
      for (int i = 0; i < values.size(); i++) {
        createScatteredJobs(job, event, portId, values.get(i), node, values.size(), usePositionFromEvent ? position : i + 1);
      }
    }
    if (scatterStrategy.isEmptyListDetected() && scatterStrategy.isHanging()) {
      Object output = scatterStrategy.generateOutputsForEmptyList();
      scatterStrategy.skipScatter(true);

      List<VariableRecord> outputVariableRecords = variableRecordService.find(job.getId(), LinkPortType.OUTPUT, job.getRootId());
      for (VariableRecord outputVariableRecord : outputVariableRecords) {
        eventProcessor.send(new OutputUpdateEvent(job.getRootId(), job.getId(), outputVariableRecord.getPortId(), output, 1, 1, event.getEventGroupId(), event.getProducedByNode()));
      }
      return;
    }
  }

  JobRecord createJobRecord(String id, UUID parentId, DAGNode node, boolean isScattered, UUID contextId, String dagCache) {
    boolean isBlocking = false;
    for (LinkMerge linkMerge : node.getLinkMergeSet(LinkPortType.INPUT)) {
      if (LinkMerge.isBlocking(linkMerge)) {
        isBlocking = true;
        break;
      }
    }
    if (ScatterMethod.isBlocking(node.getScatterMethod())) {
      isBlocking = true;
    }
    return new JobRecord(contextId, id, JobRecordService.generateUniqueId(), parentId, JobRecord.JobState.PENDING, node instanceof DAGContainer, isScattered, false, isBlocking, dagCache);
  }

  private void createScatteredJobs(JobRecord job, Event event, String port, Object value, DAGNode node, Integer numberOfScattered, Integer position) throws EventHandlerException {
    ScatterStrategy scatterStrategy = job.getScatterStrategy();
    try {
      scatterStrategy.enable(port, value, position, numberOfScattered);
    } catch (ScatterStrategyException e) {
      throw new EventHandlerException("Failed to enable ScatterStrategy for node " + node.getId(), e);
    }

    List<RowMapping> mappings = null;
    try {
      mappings = scatterStrategy.enabled();
    } catch (BindingException e) {
      throw new EventHandlerException("Failed to enable ScatterStrategy for node " + node.getId(), e);
    }
    scatterStrategy.commit(mappings);

    int oldScatteredNumber = job.getNumberOfGlobalOutputs();
    int newScatteredNumber = job.getNumberOfGlobalOutputs();

    for (RowMapping mapping : mappings) {
      job.setState(JobRecord.JobState.RUNNING);
      jobRecordService.update(job);

      List<Event> events = new ArrayList<>();

      String jobNId = InternalSchemaHelper.scatterId(job.getId(), mapping.getIndex());
      JobRecord jobN = createJobRecord(jobNId, job.getExternalId(), node, true, job.getRootId(), job.getDagHash());

      jobRecordService.create(jobN);

      for (DAGLinkPort inputPort : node.getInputPorts()) {
        Object defaultValue = node.getDefaults().get(inputPort.getId());
        VariableRecord variableN = new VariableRecord(job.getRootId(), jobNId, inputPort.getId(), LinkPortType.INPUT, defaultValue, node.getLinkMerge(inputPort.getId(), inputPort.getType()));
        variableN.setNumberGlobals(getNumberOfScattered(job, numberOfScattered));
        variableRecordService.create(variableN);

        PortCounter inputPortCounter = job.getInputCounter(inputPort.getId());
        if (inputPortCounter == null) {
          continue;
        }

        if (jobN.getState().equals(JobRecord.JobState.PENDING)) {
          jobRecordService.incrementPortCounter(jobN, inputPort, LinkPortType.INPUT);
        }
        LinkRecord link = new LinkRecord(job.getRootId(), job.getId(), inputPort.getId(), LinkPortType.INPUT, jobNId, inputPort.getId(), LinkPortType.INPUT, 1);
        linkRecordService.create(link);

        if (inputPort.isScatter()) {
          Event eventInputPort = new InputUpdateEvent(job.getRootId(), jobNId, inputPort.getId(), mapping.getValue(inputPort.getId()), 1, event.getEventGroupId(), event.getProducedByNode());
          events.add(eventInputPort);
        } else {
          if (job.isInputPortReady(inputPort.getId())) {
            VariableRecord variable = variableRecordService.find(job.getId(), inputPort.getId(), LinkPortType.INPUT, job.getRootId());
            events.add(new InputUpdateEvent(job.getRootId(), jobNId, inputPort.getId(), variableRecordService.getValue(variable), 1, event.getEventGroupId(), event.getProducedByNode()));
          }
        }
      }
      for (DAGLinkPort outputPort : node.getOutputPorts()) {
        VariableRecord variableN = new VariableRecord(job.getRootId(), jobNId, outputPort.getId(), LinkPortType.OUTPUT, null, node.getLinkMerge(outputPort.getId(), outputPort.getType()));
        variableN.setNumberGlobals(getNumberOfScattered(job, numberOfScattered));
        variableRecordService.create(variableN);
        jobRecordService.incrementPortCounter(jobN, outputPort, LinkPortType.OUTPUT);

        LinkRecord link = new LinkRecord(job.getRootId(), jobNId, outputPort.getId(), LinkPortType.OUTPUT, job.getId(), outputPort.getId(), LinkPortType.OUTPUT, mapping.getIndex());
        linkRecordService.create(link);
      }

      job.setState(JobRecord.JobState.RUNNING);

      newScatteredNumber = getNumberOfScattered(job, numberOfScattered);

      jobRecordService.resetOutputPortCounters(job, newScatteredNumber);
      jobRecordService.update(job);

      jobN.setNumberOfGlobalOutputs(newScatteredNumber);
      jobRecordService.update(jobN);

      for (Event subevent : events) {
        eventProcessor.send(subevent);
      }
    }

    if (newScatteredNumber > oldScatteredNumber) {
      List<JobRecord> jobRecords = jobRecordService.findByParent(job.getExternalId(), job.getRootId());
      for (JobRecord jobRecord : jobRecords) {
        jobRecord.setNumberOfGlobalOutputs(newScatteredNumber);
        jobRecordService.update(jobRecord);
      }
    }
  }

  /**
   * Get number of scattered jobs
   */
  private int getNumberOfScattered(JobRecord job, Integer scatteredNodes) {
    if (scatteredNodes != null) {
      return Math.max(scatteredNodes, job.getScatterStrategy().enabledCount());
    } else {
      return job.getScatterStrategy().enabledCount();
    }
  }

}
