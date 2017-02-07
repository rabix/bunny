package org.rabix.engine.event;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.rabix.engine.event.impl.*;

/**
 * Describes event interface used in the algorithm 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ 
    @Type(value = InitEvent.class, name = "INIT"),
    @Type(value = InputUpdateEvent.class, name = "INPUT_UPDATE"),
    @Type(value = OutputUpdateEvent.class, name = "OUTPUT_UPDATE"),
    @Type(value = JobStatusEvent.class, name = "JOB_STATUS_UPDATE"),
    @Type(value = RootJobStatusEvent.class, name = "CONTEXT_STATUS_UPDATE")})
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface Event {

  public enum EventType {
    INIT,
    INPUT_UPDATE,
    OUTPUT_UPDATE,
    JOB_STATUS_UPDATE,
    CONTEXT_STATUS_UPDATE
  }
  
  public enum PersistentEventType {
    INIT,
    JOB_STATUS_UPDATE_RUNNING,
    JOB_STATUS_UPDATE_COMPLETED
  }

  public enum EventStatus {
    PROCESSED,
    UNPROCESSED
  }
  
  /**
   * Gets type of the event 
   */
  EventType getType();
  
  UUID getRootId();

  UUID getEventGroupId();

  PersistentEventType getPersistentType();

}
