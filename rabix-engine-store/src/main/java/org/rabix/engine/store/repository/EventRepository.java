package org.rabix.engine.store.repository;

import org.rabix.engine.store.model.EventRecord;

import java.util.List;
import java.util.UUID;


public interface EventRepository {

  void insert(EventRecord event);

  void deleteGroup(UUID groupId);

  List<EventRecord> getPendingEvents();

}
