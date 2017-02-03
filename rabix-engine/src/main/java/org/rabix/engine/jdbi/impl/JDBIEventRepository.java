package org.rabix.engine.jdbi.impl;

import java.util.UUID;

import org.rabix.engine.event.Event;
import org.rabix.engine.event.Event.EventStatus;
import org.rabix.engine.event.Event.PersistentEventType;
import org.rabix.engine.jdbi.bindings.BindJson;
import org.rabix.engine.repository.EventRepository;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface JDBIEventRepository extends EventRepository {

  @Override
  @SqlUpdate("insert into event (id,event,type,status) values (:id,:event::jsonb,:type,:status)")
  void insert(@Bind("id") UUID id, @Bind("type") PersistentEventType type, @BindJson("event") Event event, @Bind("status") EventStatus status);
  
  @Override
  @SqlUpdate("update event set status=:status where id=:id and type=:type")
  void update(@Bind("id") UUID id, @Bind("type") PersistentEventType type, @Bind("status") EventStatus status);
  
}
