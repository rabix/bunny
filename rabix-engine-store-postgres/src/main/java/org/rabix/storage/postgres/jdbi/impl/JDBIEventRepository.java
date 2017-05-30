package org.rabix.storage.postgres.jdbi.impl;

import java.lang.annotation.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.rabix.common.helper.JSONHelper;
import org.rabix.common.json.BeanSerializer;
import org.rabix.storage.model.EventRecord;
import org.rabix.storage.postgres.jdbi.bindings.BindJson;
import org.rabix.storage.repository.EventRepository;
import org.skife.jdbi.v2.SQLStatement;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

@RegisterMapper(value = JDBIEventRepository.EventMapper.class)
public interface JDBIEventRepository extends EventRepository {

  @Override
  @SqlUpdate("insert into event (id,event,type,status) select :id,:event::jsonb,:type::persistent_event_type,:status::event_status where not exists (select id from event where id=:id and type=:type::persistent_event_type)")
  void insert(@BindEvent EventRecord eventRecord);
  
  @Override
  @SqlUpdate("update event set status=:status::event_status,modified_at='now' where id=:id and type=:type::persistent_event_type")
  void updateStatus(@BindEvent EventRecord eventRecord);
  
  @Override
  @SqlUpdate("deleteGroup from event where id=:id")
  void deleteGroup(@Bind("id") UUID id);
  
  @Override
  @SqlQuery("select * from event where status='UNPROCESSED'::event_status")
  List<EventRecord> findUnprocessed();

  public static class EventMapper implements ResultSetMapper<EventRecord> {
    public EventRecord map(int index, ResultSet r, StatementContext ctx) throws SQLException {
      UUID groupId = r.getObject("event", UUID.class);
      EventRecord.PersistentType type = EventRecord.PersistentType.valueOf(r.getString("type"));
      EventRecord.Status status = EventRecord.Status.valueOf(r.getString("status"));
      Map<String, ?> event = JSONHelper.readMap(r.getString("event"));

      return new EventRecord(groupId, type, status, event);
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
            q.bind("event", JSONHelper.writeObject(event.getEvent()));
            q.bind("type", event.getType().toString());
            q.bind("status", event.getStatus().toString());
          }
        };
      }
    }
  }
}
