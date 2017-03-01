package org.rabix.engine.memory.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.rabix.engine.event.Event;
import org.rabix.engine.event.Event.EventStatus;
import org.rabix.engine.event.Event.PersistentEventType;
import org.rabix.engine.repository.EventRepository;

import com.google.inject.Inject;

public class MemoryEventRepository implements EventRepository {

  Map<UUID, EventEntity> eventRepository;
  
  @Inject
  public MemoryEventRepository() {
    this.eventRepository = new ConcurrentHashMap<UUID, EventEntity>();
  }

  @Override
  public synchronized void insert(UUID id, PersistentEventType type, Event event, EventStatus status) {
    eventRepository.put(id, new EventEntity(type, event, status));
  }

  @Override
  public synchronized void update(UUID id, PersistentEventType type, EventStatus status) {
    EventEntity event = eventRepository.get(id);
    event.setType(type);
    event.setStatus(status);
  }

  @Override
  public synchronized void delete(UUID id) {
    eventRepository.remove(id);
  }

  @Override
  public synchronized List<Event> findUnprocessed() {
    List<Event> unprocessedEvents = new ArrayList<Event>();
    for(EventEntity event: eventRepository.values()) {
      if (event.getStatus().equals(EventStatus.UNPROCESSED)) {
        unprocessedEvents.add(event.getEvent());
      }
    }
    return unprocessedEvents;
  }
  
}
