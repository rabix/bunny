package org.rabix.engine.processor.handler.impl;

import java.util.Map;
import java.util.Map.Entry;

import org.rabix.bindings.model.dag.DAGContainer;
import org.rabix.bindings.model.dag.DAGLinkPort;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.helper.CloneHelper;
import org.rabix.engine.db.DAGNodeDB;
import org.rabix.engine.event.impl.InitEvent;
import org.rabix.engine.event.impl.InputUpdateEvent;
import org.rabix.engine.event.impl.JobStatusEvent;
import org.rabix.engine.model.RootJob;
import org.rabix.engine.model.RootJob.RootJobStatus;
import org.rabix.engine.model.JobRecord;
import org.rabix.engine.model.VariableRecord;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.handler.EventHandler;
import org.rabix.engine.processor.handler.EventHandlerException;
import org.rabix.engine.service.RootJobService;
import org.rabix.engine.service.JobRecordService;
import org.rabix.engine.service.VariableRecordService;

import com.google.inject.Inject;

/**
 * Handles {@link InitEvent} events.
 */
public class InitEventHandler implements EventHandler<InitEvent> {

  private DAGNodeDB nodeDB;
  private EventProcessor eventProcessor;
  private JobRecordService jobRecordService;
  private RootJobService rootJobService;
  private VariableRecordService variableRecordService;

  @Inject
  public InitEventHandler(EventProcessor eventProcessor, JobRecordService jobRecordService, VariableRecordService variableRecordService, RootJobService rootJobService, DAGNodeDB dagNodeDB) {
    this.nodeDB = dagNodeDB;
    this.eventProcessor = eventProcessor;
    this.jobRecordService = jobRecordService;
    this.rootJobService = rootJobService;
    this.variableRecordService = variableRecordService;
  }

  public void handle(final InitEvent event) throws EventHandlerException {

    RootJob context = new RootJob(event.getRootId(), event.getConfig(), RootJobStatus.RUNNING);
    
    rootJobService.create(context);
    nodeDB.loadDB(event.getNode(), event.getRootId());
    
    DAGNode node = nodeDB.get(event.getNode().getName(), event.getRootId());
    JobRecord job = new JobRecord(event.getRootId(), event.getNode().getName(), event.getRootId(), null, JobRecord.JobState.PENDING, node instanceof DAGContainer, false, true, false);

    for (DAGLinkPort inputPort : node.getInputPorts()) {
      if (job.getState().equals(JobRecord.JobState.PENDING)) {
        jobRecordService.incrementPortCounter(job, inputPort, LinkPortType.INPUT);
      }
      Object defaultValue = node.getDefaults().get(inputPort.getId());
      VariableRecord variable = new VariableRecord(event.getRootId(), event.getNode().getName(), inputPort.getId(), LinkPortType.INPUT, defaultValue, node.getLinkMerge(inputPort.getId(), inputPort.getType()));
      variableRecordService.create(variable);
    }

    for (DAGLinkPort outputPort : node.getOutputPorts()) {
      jobRecordService.incrementPortCounter(job, outputPort, LinkPortType.OUTPUT);

      VariableRecord variable = new VariableRecord(event.getRootId(), event.getNode().getName(), outputPort.getId(), LinkPortType.OUTPUT, null, node.getLinkMerge(outputPort.getId(), outputPort.getType()));
      variableRecordService.create(variable);
    }
    jobRecordService.create(job);

    if (node.getInputPorts().isEmpty()) {
      // the node is ready
      eventProcessor.send(new JobStatusEvent(job.getName(), JobRecord.JobState.READY, event.getRootId(), null, event.getEventGroupId()));
      return;
    }
    
    Map<String, Object> mixedInputs = mixInputs(node, event.getValue());
    for (DAGLinkPort inputPort : node.getInputPorts()) {
      Object value = mixedInputs.get(inputPort.getId());
      eventProcessor.send(new InputUpdateEvent(event.getRootId(), event.getNode().getName(), inputPort.getId(), value, 1, event.getEventGroupId()));
    }
  }
  
  @SuppressWarnings("unchecked")
  private Map<String, Object> mixInputs(DAGNode dagNode, Map<String, Object> inputs) {
    Map<String, Object> mixedInputs;
    try {
      mixedInputs = (Map<String, Object>) CloneHelper.deepCopy(dagNode.getDefaults());
      if (inputs == null) {
        return mixedInputs;
      }
      for (Entry<String, Object> inputEntry : inputs.entrySet()) {
        mixedInputs.put(inputEntry.getKey(), inputEntry.getValue());
      }
      return mixedInputs;
    } catch (Exception e) {
      throw new RuntimeException("Failed to clone default inputs for node " + dagNode.getName());
    }
  }

}
