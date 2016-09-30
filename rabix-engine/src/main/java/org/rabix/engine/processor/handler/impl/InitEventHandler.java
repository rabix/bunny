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
import org.rabix.engine.model.ContextRecord;
import org.rabix.engine.model.ContextRecord.ContextStatus;
import org.rabix.engine.model.JobRecord;
import org.rabix.engine.model.VariableRecord;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.handler.EventHandler;
import org.rabix.engine.processor.handler.EventHandlerException;
import org.rabix.engine.service.ContextRecordService;
import org.rabix.engine.service.JobRecordService;
import org.rabix.engine.service.JobRecordService.JobState;
import org.rabix.engine.service.VariableRecordService;

import com.google.inject.Inject;

/**
 * Handles {@link InitEvent} events.
 */
public class InitEventHandler implements EventHandler<InitEvent> {

  private DAGNodeDB nodeDB;
  private EventProcessor eventProcessor;
  private JobRecordService jobRecordService;
  private ContextRecordService contextRecordService;
  private VariableRecordService variableRecordService;

  @Inject
  public InitEventHandler(EventProcessor eventProcessor, JobRecordService jobRecordService, VariableRecordService variableRecordService, ContextRecordService contextRecordService, DAGNodeDB dagNodeDB) {
    this.nodeDB = dagNodeDB;
    this.eventProcessor = eventProcessor;
    this.jobRecordService = jobRecordService;
    this.contextRecordService = contextRecordService;
    this.variableRecordService = variableRecordService;
  }

  public void handle(final InitEvent event) throws EventHandlerException {
    ContextRecord context = new ContextRecord(event.getRootId(), event.getConfig(), ContextStatus.RUNNING);
    
    contextRecordService.create(context);
    nodeDB.loadDB(event.getNode(), event.getContextId());
    
    DAGNode node = nodeDB.get(event.getNode().getId(), event.getContextId());
    JobRecord job = new JobRecord(event.getContextId(), event.getNode().getId(), event.getContextId(), null, JobState.PENDING, node instanceof DAGContainer, false, true, false);

    for (DAGLinkPort inputPort : node.getInputPorts()) {
      if (job.getState().equals(JobState.PENDING)) {
        job.incrementPortCounter(inputPort, LinkPortType.INPUT);
      }
      Object defaultValue = node.getDefaults().get(inputPort.getId());
      VariableRecord variable = new VariableRecord(event.getContextId(), event.getNode().getId(), inputPort.getId(), LinkPortType.INPUT, defaultValue, node.getLinkMerge(inputPort.getId(), inputPort.getType()));
      variableRecordService.create(variable);
    }

    for (DAGLinkPort outputPort : node.getOutputPorts()) {
      job.incrementPortCounter(outputPort, LinkPortType.OUTPUT);

      VariableRecord variable = new VariableRecord(event.getContextId(), event.getNode().getId(), outputPort.getId(), LinkPortType.OUTPUT, null, node.getLinkMerge(outputPort.getId(), outputPort.getType()));
      variableRecordService.create(variable);
    }
    jobRecordService.create(job);

    if (node.getInputPorts().isEmpty()) {
      // the node is ready
      eventProcessor.send(new JobStatusEvent(job.getId(), event.getContextId(), JobState.READY, null));
      return;
    }
    
    Map<String, Object> mixedInputs = mixInputs(node, event.getValue());
    for (DAGLinkPort inputPort : node.getInputPorts()) {
      Object value = mixedInputs.get(inputPort.getId());
      eventProcessor.send(new InputUpdateEvent(event.getContextId(), event.getNode().getId(), inputPort.getId(), value, 1));
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
      throw new RuntimeException("Failed to clone default inputs for node " + dagNode.getId());
    }
  }

}
