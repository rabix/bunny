package org.rabix.engine.store.memory.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.rabix.engine.store.model.EventRecord;
import org.rabix.engine.store.repository.EventRepository;

import com.google.inject.Inject;

public class InMemoryEventRepository implements EventRepository {

  private Set<EventRecord> eventRepository;

  @Inject
  public InMemoryEventRepository() {
    this.eventRepository = new HashSet<>();
  }

  @Override
  public synchronized void insert(EventRecord event) {
    eventRepository.add(event);
  }

  @Override
  public synchronized void deleteGroup(UUID id) {
    eventRepository.removeIf(e -> e.getGroupId().equals(id));
  }

  @Override
  public synchronized List<EventRecord> getAll() {
    return eventRepository.stream()
        .filter(event -> event.getStatus() == EventRecord.Status.UNPROCESSED)
        .collect(Collectors.toList());
  }

}
