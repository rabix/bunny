package org.rabix.engine.processor.handler;

import org.rabix.engine.event.Event;

/**
 * Describes an event handler interface
 */
public interface EventHandler<T extends Event> {

  /**
   * Handles the event
   * @param event
   * @throws EventHandlerException
   */
  void handle(T event) throws EventHandlerException;

}
