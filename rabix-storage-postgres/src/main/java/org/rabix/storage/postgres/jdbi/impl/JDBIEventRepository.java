package org.rabix.storage.postgres.jdbi.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.rabix.common.json.BeanSerializer;
import org.rabix.storage.postgres.jdbi.bindings.BindJson;
import org.rabix.storage.postgres.jdbi.impl.JDBIEventRepository.EventMapper;
import org.rabix.storage.repository.EventRepository;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

@RegisterMapper(value = EventMapper.class)
public interface JDBIEventRepository extends EventRepository {

  @Override
  @SqlUpdate("insert into event (id,event,type,status) select :id,:event::jsonb,:type::persistent_event_type,:status::event_status where not exists (select id from event where id=:id and type=:type::persistent_event_type)")
  void insert(@Bind("id") UUID id, @Bind("type") PersistentEventType type, @BindJson("event") Event event, @Bind("status") EventStatus status);
  
  @Override
  @SqlUpdate("update event set status=:status::event_status,modified_at='now' where id=:id and type=:type::persistent_event_type")
  void update(@Bind("id") UUID id, @Bind("type") PersistentEventType type, @Bind("status") EventStatus status);
  
  @Override
  @SqlUpdate("deleteGroup from event where id=:id")
  void deleteGroup(@Bind("id") UUID id);
  
  @Override
  @SqlQuery("select * from event where status='UNPROCESSED'::event_status")
  List<Event> findUnprocessed();
  
  public static class EventMapper implements ResultSetMapper<Event> {
    public Event map(int index, ResultSet r, StatementContext ctx) throws SQLException {
      return BeanSerializer.deserialize(r.getString("event"), Event.class);
    }
  }
}
