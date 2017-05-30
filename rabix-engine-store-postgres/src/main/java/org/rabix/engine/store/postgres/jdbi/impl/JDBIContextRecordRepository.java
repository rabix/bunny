package org.rabix.engine.store.postgres.jdbi.impl;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.postgresql.util.PGobject;
import org.rabix.common.helper.JSONHelper;
import org.rabix.engine.store.model.ContextRecord;
import org.rabix.engine.store.postgres.jdbi.impl.JDBIContextRecordRepository.ContextRecordMapper;
import org.rabix.engine.store.repository.ContextRecordRepository;
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

@RegisterMapper(ContextRecordMapper.class)
public interface JDBIContextRecordRepository extends ContextRecordRepository {

  @Override
  @SqlUpdate("insert into context_record (id,status,config,created_at,modified_at) values (:id,:status::context_record_status,:config,:created_at,:modified_at)")
  int insert(@BindContextRecord ContextRecord contextRecord);
  
  @Override
  @SqlUpdate("update context_record set status=:status::context_record_status,config=:config,modified_at='now' where id=:id")
  int update(@BindContextRecord ContextRecord contextRecord);
  
  @Override
  @SqlQuery("select * from context_record where id=:id")
  ContextRecord get(@Bind("id") UUID id);

  @Override
  @SqlUpdate("deleteGroup from context_record where id=:id")
  int delete(@Bind("id") UUID id);

  @BindingAnnotation(BindContextRecord.ContextBinderFactory.class)
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ ElementType.PARAMETER })
  public static @interface BindContextRecord {
    public static class ContextBinderFactory implements BinderFactory<Annotation> {
      public Binder<BindContextRecord, ContextRecord> build(Annotation annotation) {
        return new Binder<BindContextRecord, ContextRecord>() {
          public void bind(SQLStatement<?> q, BindContextRecord bind, ContextRecord contextRecord) {
            q.bind("id", contextRecord.getId());
            q.bind("status", contextRecord.getStatus());
            try {
              PGobject data = new PGobject();
              data.setType("jsonb");
              data.setValue(JSONHelper.writeObject(contextRecord.getConfig()));
              q.bind("config", data);
            } catch (SQLException ex) {
              throw new IllegalStateException("Error Binding config", ex);
            }
            q.bind("created_at", Timestamp.valueOf(contextRecord.getCreatedAt()));
            q.bind("modified_at", Timestamp.valueOf(contextRecord.getModifiedAt()));
          }
        };
      }
    }
  }
  
  public static class ContextRecordMapper implements ResultSetMapper<ContextRecord> {
    public ContextRecord map(int index, ResultSet resultSet, StatementContext ctx) throws SQLException {
      UUID id = resultSet.getObject("ID", UUID.class);
      String config = resultSet.getString("CONFIG");
      String status = resultSet.getString("STATUS");
      LocalDateTime createdAt = resultSet.getTimestamp("created_at").toLocalDateTime();
      LocalDateTime modifiedAt = resultSet.getTimestamp("modified_at").toLocalDateTime();

      Map<String, Object> configObject = JSONHelper.readMap(config);
      return new ContextRecord(id, configObject, ContextRecord.ContextStatus.valueOf(status), createdAt, modifiedAt);
    }
  }
}
