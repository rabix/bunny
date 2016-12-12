package org.rabix.engine.processor.handler.impl;

import java.util.ArrayList;
import java.util.List;

import org.rabix.bindings.model.LinkMerge;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.engine.db.DAGNodeDB;
import org.rabix.engine.event.Event;
import org.rabix.engine.event.impl.InputUpdateEvent;
import org.rabix.engine.event.impl.JobStatusEvent;
import org.rabix.engine.model.JobRecord;
import org.rabix.engine.model.LinkRecord;
import org.rabix.engine.model.VariableRecord;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.handler.EventHandler;
import org.rabix.engine.processor.handler.EventHandlerException;
import org.rabix.engine.service.JobRecordService;
import org.rabix.engine.service.JobRecordService.JobState;
import org.rabix.engine.service.LinkRecordService;
import org.rabix.engine.service.VariableRecordService;

import com.google.inject.Inject;

/**
 * Handles {@link InputUpdateEvent} events.
 */
public class InputEventHandler implements EventHandler<InputUpdateEvent> {

  private final DAGNodeDB nodeDB;
  private final JobRecordService jobService;
  private final LinkRecordService linkService;
  private final VariableRecordService variableService;
  
  private final ScatterHandler scatterHelper;
  private final EventProcessor eventProcessor;

  @Inject
  public InputEventHandler(EventProcessor eventProcessor, ScatterHandler scatterHelper, JobRecordService jobService, VariableRecordService variableService, LinkRecordService linkService, DAGNodeDB nodeDB) {
    this.nodeDB = nodeDB;
    this.jobService = jobService;
    this.linkService = linkService;
    this.variableService = variableService;
    
    this.scatterHelper = scatterHelper;
    this.eventProcessor = eventProcessor;
  }
  
  @Override
  public void handle(InputUpdateEvent event) throws EventHandlerException {
    JobRecord job = jobService.find(event.getJobId(), event.getContextId());
    VariableRecord variable = variableService.find(event.getJobId(), event.getPortId(), LinkPortType.INPUT, event.getContextId());

    DAGNode node = nodeDB.get(InternalSchemaHelper.normalizeId(job.getId()), event.getContextId());

    if (event.isLookAhead()) {
      if (job.isBlocking() || (job.getInputPortIncoming(event.getPortId()) > 1)) {
        return; // guard: should not happen
      } else {
        job.resetInputPortCounters(event.getNumberOfScattered());
      }
    } else if ((job.getInputPortIncoming(event.getPortId()) > 1) && job.isScatterPort(event.getPortId()) && !LinkMerge.isBlocking(node.getLinkMerge(event.getPortId(), LinkPortType.INPUT))) {
      job.resetOutputPortCounters(job.getInputPortIncoming(event.getPortId()));
    }
    
    variable.addValue(event.getValue(), event.getPosition());
    job.decrementPortCounter(event.getPortId(), LinkPortType.INPUT);
    
    // scatter
    if (!job.isBlocking() && !job.isScattered()) {
      if (job.isScatterPort(event.getPortId())) {
        if ((job.isInputPortBlocking(node, event.getPortId()))) {
          // it's blocking
          if (job.isInputPortReady(event.getPortId())) {
            scatterHelper.scatterPort(job, event, event.getPortId(), variable.getValue(), event.getPosition(), event.getNumberOfScattered(), event.isLookAhead(), false);
            update(job, variable);
            return;
          }
        } else {
          // it's not blocking
          scatterHelper.scatterPort(job, event, event.getPortId(), event.getValue(), event.getPosition(), event.getNumberOfScattered(), event.isLookAhead(), true);
          update(job, variable);
          return;
        }
      } else if (job.isScatterWrapper()) {
        sendValuesToScatteredJobs(job, variable, event);
        update(job, variable);
        return;
      }
    }

    if (job.isReady()) {
      JobStatusEvent jobStatusEvent = new JobStatusEvent(job.getId(), event.getContextId(), JobState.READY, null, event.getEventGroupId());
      eventProcessor.send(jobStatusEvent);
    }
    update(job, variable);
  }
  
  private void update(JobRecord job, VariableRecord variable) {
    jobService.update(job);
    variableService.update(variable);
  }
  
  /**
   * Send events from scatter wrapper to scattered jobs
   */
  private void sendValuesToScatteredJobs(JobRecord job, VariableRecord variable, InputUpdateEvent event) throws EventHandlerException {
    List<LinkRecord> links = linkService.findBySourceAndDestinationType(job.getId(), event.getPortId(), LinkPortType.INPUT, event.getContextId());

    List<Event> events = new ArrayList<>();
    for (LinkRecord link : links) {
      VariableRecord destinationVariable = variableService.find(link.getDestinationJobId(), link.getDestinationJobPort(), LinkPortType.INPUT, event.getContextId());

      Event updateInputEvent = new InputUpdateEvent(event.getContextId(), destinationVariable.getJobId(), destinationVariable.getPortId(), variable.getValue(), event.getPosition(), event.getEventGroupId());
      events.add(updateInputEvent);
    }
    for (Event subevent : events) {
      eventProcessor.send(subevent);
    }
  }

}
