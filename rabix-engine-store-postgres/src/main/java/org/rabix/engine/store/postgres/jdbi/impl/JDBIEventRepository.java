package org.rabix.engine.store.postgres.jdbi.impl;

import org.rabix.common.helper.JSONHelper;
import org.rabix.engine.store.model.EventRecord;
import org.rabix.engine.store.repository.EventRepository;
import org.skife.jdbi.v2.SQLStatement;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.lang.annotation.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RegisterMapper(value = JDBIEventRepository.EventMapper.class)
public interface JDBIEventRepository extends EventRepository {

  @Override
  @SqlUpdate("insert into event (id,event,status) values (:id,:event,:status::event_status)")
  void insert(@BindEvent EventRecord eventRecord);


  @Override
  @SqlUpdate("delete from event where id=:id")
  void deleteGroup(@Bind("id") UUID id);

  @Override
  @SqlUpdate("delete from event where root_id=:root_id")
  void deleteByRootId(@Bind("root_id") UUID rootId);

  @Override
  @SqlUpdate("update event set status=:status::event_status where id=:id")
  void updateStatus(@Bind("id") UUID groupId, @Bind("status") EventRecord.Status status);

  @Override
  @SqlQuery("select * from event where status <> 'FAILED' order by created_at asc")
  List<EventRecord> getPendingEvents();

  public static class EventMapper implements ResultSetMapper<EventRecord> {
    public EventRecord map(int index, ResultSet r, StatementContext ctx) throws SQLException {
      EventRecord.Status status = EventRecord.Status.valueOf(r.getString("status"));
      Map<String, ?> event = JSONHelper.readMap(new String(r.getBytes("event")));

      UUID rootId = r.getString("root_id") != null ? UUID.fromString(r.getString("root_id")) : null;
      return new EventRecord(rootId, UUID.fromString(r.getString("id")), status, event);
    }
  }

  @BindingAnnotation(JDBIEventRepository.BindEvent.EventBinderFactory.class)
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ ElementType.PARAMETER })
  public static @interface BindEvent {
    public static class EventBinderFactory implements BinderFactory<Annotation> {
      public Binder<JDBIEventRepository.BindEvent, EventRecord> build(Annotation annotation) {
        return new Binder<JDBIEventRepository.BindEvent, EventRecord>() {
          public void bind(SQLStatement<?> q, JDBIEventRepository.BindEvent bind, EventRecord event) {
            q.bind("id", event.getGroupId());
            q.bind("event", JSONHelper.writeObject(event.getEvent()).getBytes());
            q.bind("status", event.getStatus().toString());
          }
        };
      }
    }
  }
}
