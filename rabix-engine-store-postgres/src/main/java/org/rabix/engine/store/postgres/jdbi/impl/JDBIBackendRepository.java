package org.rabix.engine.store.postgres.jdbi.impl;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.rabix.engine.store.model.BackendRecord;
import org.rabix.engine.store.postgres.jdbi.impl.JDBIBackendRepository.BackendMapper;
import org.rabix.engine.store.repository.BackendRepository;
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

@RegisterMapper({BackendMapper.class, JDBIBackendRepository.InstantMapper.class})
public interface JDBIBackendRepository extends BackendRepository {

  @Override
  @SqlUpdate("insert into backend (id,name,type,configuration,heartbeat_info,status) values (:id,:name,:type::backend_type,:configuration::jsonb,:heartbeat_info,:status::backend_status)")
  void insert(@BindBackend BackendRecord backend);
  
  @Override
  @SqlUpdate("update backend set configuration=:configuration where id=:id")
  void updateConfiguration(@Bind("id") UUID id, @Bind("configuration") String configuration);
  
  @Override
  @SqlQuery("select * from backend where id=:id")
  BackendRecord get(@Bind("id") UUID id);
  
  @Override
  @SqlQuery("select * from backend")
  List<BackendRecord> getAll();
  
  @Override
  @SqlQuery("select * from backend where status=:status::backend_status")
  List<BackendRecord> getByStatus(@Bind("status") BackendRecord.Status status);
  
  @Override
  @SqlUpdate("update backend set status=:status::backend_status where id=:id")
  void updateStatus(@Bind("id") UUID id, @Bind("status") BackendRecord.Status status);
  
  @Override
  @SqlUpdate("update backend set heartbeat_info=:heartbeat_info where id=:id")
  void updateHeartbeatInfo(@Bind("id") UUID id, @BindInstant("heartbeat_info") Instant heartbeatInfo);
  
  @Override
  @SqlQuery("select heartbeat_info from backend where id=:id")
  Instant getHeartbeatInfo(@Bind("id") UUID id);
  
  public static class BackendMapper implements ResultSetMapper<BackendRecord> {
    public BackendRecord map(int index, ResultSet r, StatementContext ctx) throws SQLException {
      UUID id = r.getObject("id", UUID.class);
      String name = r.getString("name");
      BackendRecord.Status status = BackendRecord.Status.valueOf(r.getString("status"));
      Instant heartbit = r.getObject("heartbeat_info", Timestamp.class).toInstant();
      BackendRecord.Type type = BackendRecord.Type.valueOf(r.getString("type"));
      return new BackendRecord(id, name, heartbit, r.getString("configuration"), status, type);
    }
  }

  public static class InstantMapper implements ResultSetMapper<Instant> {
    public Instant map(int index, ResultSet r, StatementContext ctx) throws SQLException {
      return r.getObject("heartbeat_info", Timestamp.class).toInstant();
    }
  }

  @BindingAnnotation(JDBIBackendRepository.BindBackend.BackendBinderFactory.class)
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ ElementType.PARAMETER })
  public static @interface BindBackend {
    public static class BackendBinderFactory implements BinderFactory<Annotation> {
      public Binder<JDBIBackendRepository.BindBackend, BackendRecord> build(Annotation annotation) {
        return new Binder<JDBIBackendRepository.BindBackend, BackendRecord>() {
          public void bind(SQLStatement<?> q, JDBIBackendRepository.BindBackend bind, BackendRecord backend) {
            q.bind("id", backend.getId());
            q.bind("name", backend.getName());
            q.bind("type", backend.getType().toString());
            q.bind("heartbeat_info", Timestamp.from(backend.getHeartbeatInfo()));
            q.bind("status", backend.getStatus().toString());
            q.bind("configuration", backend.getBackendConfig());
          }
        };
      }
    }
  }

  @BindingAnnotation(JDBIBackendRepository.BindInstant.InstantdBinderFactory.class)
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ ElementType.PARAMETER })
  public static @interface BindInstant {
    String value();
    public static class InstantdBinderFactory implements BinderFactory<Annotation> {
      public Binder<JDBIBackendRepository.BindInstant, Instant> build(Annotation annotation) {
        return new Binder<JDBIBackendRepository.BindInstant, Instant>() {
          public void bind(SQLStatement<?> q, JDBIBackendRepository.BindInstant bind, Instant instant) {
            q.bind(bind.value(), Timestamp.from(instant));
          }
        };
      }
    }
  }
  
}