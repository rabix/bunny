package org.rabix.engine.processor.handler.impl;

import java.util.ArrayList;
import java.util.List;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.model.LinkMerge;
import org.rabix.bindings.model.ScatterMethod;
import org.rabix.bindings.model.dag.DAGContainer;
import org.rabix.bindings.model.dag.DAGLinkPort;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.engine.db.DAGNodeDB;
import org.rabix.engine.event.Event;
import org.rabix.engine.event.impl.InputUpdateEvent;
import org.rabix.engine.model.JobRecord;
import org.rabix.engine.model.LinkRecord;
import org.rabix.engine.model.VariableRecord;
import org.rabix.engine.model.JobRecord.PortCounter;
import org.rabix.engine.model.scatter.RowMapping;
import org.rabix.engine.model.scatter.ScatterStrategy;
import org.rabix.engine.model.scatter.ScatterStrategyFactory;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.handler.EventHandlerException;
import org.rabix.engine.service.JobRecordService;
import org.rabix.engine.service.JobRecordService.JobState;
import org.rabix.engine.service.LinkRecordService;
import org.rabix.engine.service.VariableRecordService;

import com.google.inject.Inject;

public class ScatterHandler {

  private final DAGNodeDB dagNodeDB;
  private final EventProcessor eventProcessor;
  
  private final JobRecordService jobRecordService;
  private final LinkRecordService linkRecordService;
  private final VariableRecordService variableRecordService;
  private final ScatterStrategyFactory scatterStrategyFactory;
  
  @Inject
  public ScatterHandler(final DAGNodeDB dagNodeDB, final JobRecordService jobRecordService, final VariableRecordService variableRecordService, final LinkRecordService linkRecordService, final EventProcessor eventProcessor, final ScatterStrategyFactory scatterStrategyFactory) {
    this.dagNodeDB = dagNodeDB;
    this.eventProcessor = eventProcessor;
    this.jobRecordService = jobRecordService;
    this.linkRecordService = linkRecordService;
    this.variableRecordService = variableRecordService;
    this.scatterStrategyFactory = scatterStrategyFactory;
  }
  
  /**
   * Scatters port
   * @throws BindingException 
   */
  @SuppressWarnings("unchecked")
  public void scatterPort(JobRecord job, String portId, Object value, Integer position, Integer numberOfScatteredFromEvent, boolean isLookAhead, boolean isFromEvent) throws EventHandlerException {
    DAGNode node = dagNodeDB.get(InternalSchemaHelper.normalizeId(job.getId()), job.getRootId());

    if (job.getScatterStrategy() == null) {
      try {
        job.setScatterStrategy(scatterStrategyFactory.create(node));
      } catch (BindingException e) {
        throw new EventHandlerException(e);
      }
    }

    if (isLookAhead) {
      int numberOfScattered = getNumberOfScattered(job, numberOfScatteredFromEvent);
      createScatteredJobs(job, portId, value, node, numberOfScattered, position);
      return;
    }

    if (value == null) {
      createScatteredJobs(job, portId, value, node, 1, position);
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
    
    for (int i = 0; i < values.size(); i++) {
      createScatteredJobs(job, portId, values.get(i), node, values.size(), usePositionFromEvent ? position : i + 1);
    }
  }
  
  public JobRecord createJobRecord(String id, String parentId, DAGNode node, boolean isScattered, String contextId) {
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
    return new JobRecord(contextId, id, JobRecordService.generateUniqueId(), parentId, JobState.PENDING, node instanceof DAGContainer, isScattered, false, isBlocking);
  }
  
  private void createScatteredJobs(JobRecord job, String port, Object value, DAGNode node, Integer numberOfScattered, Integer position) throws EventHandlerException {
    ScatterStrategy scatterStrategy = job.getScatterStrategy();
    scatterStrategy.enable(port, value, position);
    
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
      job.setState(JobState.RUNNING);
      jobRecordService.update(job);

      List<Event> events = new ArrayList<>();

      String jobNId = InternalSchemaHelper.scatterId(job.getId(), mapping.getIndex());
      JobRecord jobN = createJobRecord(jobNId, job.getExternalId(), node, true, job.getRootId());
       
      for (DAGLinkPort inputPort : node.getInputPorts()) {
        Object defaultValue = node.getDefaults().get(inputPort.getId());
        VariableRecord variableN = new VariableRecord(job.getRootId(), jobNId, inputPort.getId(), LinkPortType.INPUT, defaultValue, node.getLinkMerge(inputPort.getId(), inputPort.getType()));
        variableN.setNumberGlobals(getNumberOfScattered(job, numberOfScattered));
        variableRecordService.create(variableN);

        PortCounter inputPortCounter = job.getInputCounter(inputPort.getId());
        if (inputPortCounter == null) {
          continue;
        }
        
        if (jobN.getState().equals(JobState.PENDING)) {
          jobN.incrementPortCounter(inputPort, LinkPortType.INPUT);
        }
        LinkRecord link = new LinkRecord(job.getRootId(), job.getId(), inputPort.getId(), LinkPortType.INPUT, jobNId, inputPort.getId(), LinkPortType.INPUT, 1);
        linkRecordService.create(link);

        if (inputPort.isScatter()) {
          Event eventInputPort = new InputUpdateEvent(job.getRootId(), jobNId, inputPort.getId(), mapping.getValue(inputPort.getId()), 1);
          events.add(eventInputPort);
        } else {
          if (job.isInputPortReady(inputPort.getId())) {
            VariableRecord variable = variableRecordService.find(job.getId(), inputPort.getId(), LinkPortType.INPUT, job.getRootId());
            events.add(new InputUpdateEvent(job.getRootId(), jobNId, inputPort.getId(), variable.getValue(), 1));
          }
        }
      }
      for (DAGLinkPort outputPort : node.getOutputPorts()) {
        VariableRecord variableN = new VariableRecord(job.getRootId(), jobNId, outputPort.getId(), LinkPortType.OUTPUT, null, node.getLinkMerge(outputPort.getId(), outputPort.getType()));
        variableN.setNumberGlobals(getNumberOfScattered(job, numberOfScattered));
        variableRecordService.create(variableN);
        jobN.incrementPortCounter(outputPort, LinkPortType.OUTPUT);

        LinkRecord link = new LinkRecord(job.getRootId(), jobNId, outputPort.getId(), LinkPortType.OUTPUT, job.getId(), outputPort.getId(), LinkPortType.OUTPUT, null);
        linkRecordService.create(link);
      }

      job.setState(JobState.RUNNING);
      job.setScatterWrapper(true);
      
      newScatteredNumber = getNumberOfScattered(job, numberOfScattered);
      
      job.resetOutputPortCounters(newScatteredNumber);
      jobRecordService.update(job);
      
      jobN.setNumberOfGlobalOutputs(newScatteredNumber);
      jobRecordService.create(jobN);

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
