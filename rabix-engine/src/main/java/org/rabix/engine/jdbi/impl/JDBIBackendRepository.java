package org.rabix.engine.jdbi.impl;

import java.lang.annotation.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.sql.Timestamp;
import java.util.List;

import org.rabix.common.json.BeanSerializer;
import org.rabix.engine.jdbi.impl.JDBIBackendRepository.BackendMapper;
import org.rabix.engine.repository.BackendRepository;
import org.rabix.transport.backend.Backend;
import org.skife.jdbi.v2.SQLStatement;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

@RegisterMapper(BackendMapper.class)
public interface JDBIBackendRepository extends BackendRepository {

  @SqlUpdate("insert into backend (id,type,name,configuration,heartbeat_info,status) values (:id,:type::backend_type,:name,:configuration::jsonb,:heartbeat_info,:status::backend_status)")
  void insert(@BindBackend Backend backend, @Bind("heartbeat_info") Timestamp heartbeatInfo, @Bind("status") BackendStatus status);
  
  @SqlUpdate("update backend set configuration=:configuration::jsonb where id=:id")
  void update(@BindBackend Backend backend);

  @SqlQuery("select * from backend where id=:id")
  Backend get(@Bind("id") UUID id);

  @SqlQuery("select * from backend where name=:name")
  Backend getByName(@Bind("name") String name);
  
  @SqlQuery("select * from backend where status=:status::backend_status")
  List<Backend> getByStatus(@Bind("status") BackendStatus status);
  
  @SqlUpdate("update backend set status=:status::backend_status where id=:id")
  void updateStatus(@Bind("id") UUID id, @Bind("status") BackendStatus status);
  
  @SqlUpdate("update backend set heartbeat_info=:heartbeat_info where id=:id")
  void updateHeartbeatInfo(@Bind("id") UUID id, @Bind("heartbeat_info") Timestamp heartbeatInfo);
  
  @SqlQuery("select heartbeat_info from backend where id=:id")
  Timestamp getHeartbeatInfo(@Bind("id") UUID id);
  
  public static class BackendMapper implements ResultSetMapper<Backend> {
    public Backend map(int index, ResultSet r, StatementContext ctx) throws SQLException {
      return BeanSerializer.deserialize(r.getString("configuration"), Backend.class);
    }
  }

  @BindingAnnotation(JDBIBackendRepository.BindBackend.BackendBinderFactory.class)
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ ElementType.PARAMETER })
  public static @interface BindBackend {
    public static class BackendBinderFactory implements BinderFactory<Annotation> {
      public Binder<JDBIBackendRepository.BindBackend, Backend> build(Annotation annotation) {
        return new Binder<JDBIBackendRepository.BindBackend, Backend>() {
          public void bind(SQLStatement<?> q, JDBIBackendRepository.BindBackend bind, Backend backend) {
            q.bind("id", backend.getId());
            q.bind("type", backend.getType());
            q.bind("name", backend.getName());
            q.bind("configuration", BeanSerializer.serializeFull(backend));
          }
        };
      }
    }
  }
  
}