package org.rabix.engine.jdbi.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.rabix.common.json.BeanSerializer;
import org.rabix.engine.event.Event;
import org.rabix.engine.event.Event.EventStatus;
import org.rabix.engine.event.Event.PersistentEventType;
import org.rabix.engine.jdbi.bindings.BindJson;
import org.rabix.engine.jdbi.impl.JDBIEventRepository.EventMapper;
import org.rabix.engine.repository.EventRepository;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

@RegisterMapper(value = EventMapper.class)
public interface JDBIEventRepository extends EventRepository {

  @Override
  @SqlUpdate("insert into event (id,event,type,status) select :id,:event::jsonb,:type,:status where not exists (select id from event where id=:id and type=:type)")
  void insert(@Bind("id") UUID id, @Bind("type") PersistentEventType type, @BindJson("event") Event event, @Bind("status") EventStatus status);
  
  @Override
  @SqlUpdate("update event set status=:status where id=:id and type=:type")
  void update(@Bind("id") UUID id, @Bind("type") PersistentEventType type, @Bind("status") EventStatus status);
  
  @Override
  @SqlUpdate("delete from event where id=:id")
  void delete(@Bind("id") UUID id);
  
  @Override
  @SqlQuery("select * from event where status='UNPROCESSED'")
  List<Event> findUnprocessed();
  
  public static class EventMapper implements ResultSetMapper<Event> {
    public Event map(int index, ResultSet r, StatementContext ctx) throws SQLException {
      return BeanSerializer.deserialize(r.getString("event"), Event.class);
    }
  }
}
