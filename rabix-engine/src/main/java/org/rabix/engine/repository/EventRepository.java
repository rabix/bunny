package org.rabix.engine.repository;

import java.util.UUID;

import org.rabix.engine.event.Event;
import org.rabix.engine.event.Event.EventStatus;
import org.rabix.engine.event.Event.PersistentEventType;

public interface EventRepository {

  void insert(UUID id, PersistentEventType type, Event event, EventStatus status);
  
  void update(UUID id, PersistentEventType type, EventStatus status);
  
}
