package org.rabix.engine.store.postgres.jdbi.impl;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.rabix.common.helper.JSONHelper;
import org.rabix.engine.store.model.EventRecord;
import org.rabix.engine.store.repository.EventRepository;
import org.skife.jdbi.v2.SQLStatement;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.Binder;
import org.skife.jdbi.v2.sqlobject.BinderFactory;
import org.skife.jdbi.v2.sqlobject.BindingAnnotation;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

@RegisterMapper(value = JDBIEventRepository.EventMapper.class)
public interface JDBIEventRepository extends EventRepository {

  @Override
  @SqlUpdate("insert into event (id,event,status) values (:id,:event,:status::event_status)")
  void insert(@BindEvent EventRecord eventRecord);
  

  @Override
  @SqlUpdate("delete from event where id=:id")
  void deleteGroup(@Bind("id") UUID id);
  
  @Override
  @SqlQuery("select * from event where status='UNPROCESSED'::event_status")
  List<EventRecord> findUnprocessed();

  public static class EventMapper implements ResultSetMapper<EventRecord> {
    public EventRecord map(int index, ResultSet r, StatementContext ctx) throws SQLException {
      EventRecord.Status status = EventRecord.Status.valueOf(r.getString("status"));
      Map<String, ?> event = JSONHelper.readMap(new String(r.getBytes("event")));
      return new EventRecord(UUID.fromString((String) event.get("eventGroupId")), status, event);
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
