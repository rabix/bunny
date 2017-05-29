package org.rabix.storage.repository;

import org.rabix.storage.model.EventRecord;

import java.util.List;
import java.util.UUID;


public interface EventRepository {

  void insert(EventRecord event);
  
  void updateStatus(EventRecord event);
  
  void deleteGroup(UUID groupId);
  
  List<EventRecord> findUnprocessed();
  
}
