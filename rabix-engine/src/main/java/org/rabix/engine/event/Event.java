package org.rabix.engine.event;

/**
 * Describes event interface used in the algorithm 
 */
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
  
  PersistentEventType getPersistentType();
  
  String getContextId();
  
  String getEventGroupId();
  
}
