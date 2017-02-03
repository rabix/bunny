package org.rabix.engine.jdbi.impl;

import java.lang.annotation.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

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

  @SqlUpdate("insert into backend (id, type, configuration) values (:id, :type, :configuration)")
  void insert(@BindBackend Backend backend);
  
  @SqlUpdate("update backend set configuration=:configuration where id=:id")
  void update(@BindBackend Backend backend);

  @SqlQuery("select * from backend where id=:id")
  Backend get(@Bind("id") UUID id);

  @SqlQuery("select * from backend where name=:name")
  Backend getByName(@Bind("name") String name);
  
  public static class BackendMapper implements ResultSetMapper<Backend> {
    public Backend map(int index, ResultSet r, StatementContext ctx) throws SQLException {
      return BeanSerializer.deserialize(r.getString("configuration"), Backend.class);
    }
  }

  @BindingAnnotation(JDBIJobRecordRepository.BindJobRecord.JobBinderFactory.class)
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ ElementType.PARAMETER })
  public static @interface BindBackend {
    public static class BackendBinderFactory implements BinderFactory<Annotation> {
      public Binder<JDBIBackendRepository.BindBackend, Backend> build(Annotation annotation) {
        return new Binder<JDBIBackendRepository.BindBackend, Backend>() {
          public void bind(SQLStatement<?> q, JDBIBackendRepository.BindBackend bind, Backend backend) {
            q.bind("id", backend.getId());
            q.bind("type", backend.getType());
            q.bind("configuration", BeanSerializer.serializeFull(backend));
          }
        };
      }
    }
  }
  
}