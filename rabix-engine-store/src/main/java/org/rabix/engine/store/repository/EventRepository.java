package org.rabix.engine.store.repository;

import org.rabix.engine.store.model.EventRecord;

import java.util.List;
import java.util.UUID;


public interface EventRepository {

  void insert(EventRecord event);

  void deleteGroup(UUID groupId);

  void deleteByRootId(UUID rootId);

  void updateStatus(UUID groupId, EventRecord.Status status);

  List<EventRecord> getPendingEvents();

}
