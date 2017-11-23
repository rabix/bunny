package org.rabix.engine.store.memory.impl;

import com.google.inject.Inject;
import org.rabix.engine.store.model.EventRecord;
import org.rabix.engine.store.repository.EventRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryEventRepository implements EventRepository {

  private final Map<UUID, EventRecord> eventRepository;

  @Inject
  public InMemoryEventRepository() {
    this.eventRepository = new ConcurrentHashMap<>();
  }

  @Override
  public void insert(EventRecord event) {
    eventRepository.put(event.getGroupId(), event);
  }

  @Override
  public void deleteGroup(UUID id) {
    eventRepository.remove(id);
  }

  @Override
  public void deleteByGroupIds(UUID rootId, Set<UUID> groupIds) {
    groupIds.forEach(this::deleteGroup);
  }

  @Override
  public void deleteByRootId(UUID rootId) {
    eventRepository.values().removeIf(e -> rootId.equals(e.getRootId()));
  }

  @Override
  public void updateStatus(UUID groupId, EventRecord.Status status) {
    EventRecord eventRecord = eventRepository.get(groupId);
    if (eventRecord != null) {
      eventRecord.setStatus(status);
    }
  }

  @Override
  public List<EventRecord> getPendingEvents() {
    return eventRepository.values().stream()
        .filter(event ->
                event.getStatus() == EventRecord.Status.UNPROCESSED
                        || event.getStatus() == EventRecord.Status.PROCESSED)
        .collect(Collectors.toList());
  }

}
