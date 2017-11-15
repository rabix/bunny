package org.rabix.engine.store.memory.impl;

import com.google.inject.Inject;
import org.rabix.engine.store.model.EventRecord;
import org.rabix.engine.store.repository.EventRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryEventRepository implements EventRepository {

  private final Set<EventRecord> eventRepository;

  @Inject
  public InMemoryEventRepository() {
    this.eventRepository = Collections.newSetFromMap(new ConcurrentHashMap<>());
  }

  @Override
  public void insert(EventRecord event) {
    eventRepository.add(event);
  }

  @Override
  public void deleteGroup(UUID id) {
    eventRepository.removeIf(e -> e.getGroupId().equals(id));
  }

  @Override
  public List<EventRecord> getPendingEvents() {
    return eventRepository.stream()
        .filter(event ->
                event.getStatus() == EventRecord.Status.UNPROCESSED
                        || event.getStatus() == EventRecord.Status.PROCESSED)
        .collect(Collectors.toList());
  }

}
