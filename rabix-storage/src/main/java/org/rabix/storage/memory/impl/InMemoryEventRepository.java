package org.rabix.storage.memory.impl;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.rabix.storage.model.EventRecord;
import org.rabix.storage.repository.EventRepository;

import com.google.inject.Inject;

public class InMemoryEventRepository implements EventRepository {

  private class Key {
    UUID id;
    EventRecord.PersistentType type;

    public Key(UUID id, EventRecord.PersistentType type) {
      this.id = id;
      this.type = type;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Key key = (Key) o;
      return Objects.equals(id, key.id) &&
          type == key.type;
    }

    @Override
    public int hashCode() {
      return Objects.hash(id, type);
    }
  }

  private Key key(EventRecord event) {
    return new Key(event.getGroupId(), event.getType());
  }

  private Map<Key, EventRecord> eventRepository;
  
  @Inject
  public InMemoryEventRepository() {
    this.eventRepository = new ConcurrentHashMap<>();
  }

  @Override
  public synchronized void insert(EventRecord event) {
    eventRepository.put(key(event), event);
  }

  @Override
  public synchronized void updateStatus(EventRecord event) {
    eventRepository.get(key(event)).setStatus(event.getStatus());
  }

  @Override
  public synchronized void deleteGroup(UUID id) {
    eventRepository.entrySet().removeIf(e -> e.getKey().id == id);
  }

  @Override
  public synchronized List<EventRecord> findUnprocessed() {
    return eventRepository.values().stream()
        .filter(event -> event.getStatus() == EventRecord.Status.UNPROCESSED)
        .collect(Collectors.toList());
  }
  
}
