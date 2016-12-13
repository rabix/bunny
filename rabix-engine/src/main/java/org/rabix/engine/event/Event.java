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

  /**
   * Gets type of the event 
   */
  EventType getType();
  
  String getContextId();
  
  String getEventGroupId();
}
