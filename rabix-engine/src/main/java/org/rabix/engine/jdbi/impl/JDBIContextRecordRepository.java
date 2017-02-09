package org.rabix.engine.jdbi.impl;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.postgresql.util.PGobject;
import org.rabix.common.helper.JSONHelper;
import org.rabix.engine.jdbi.impl.JDBIContextRecordRepository.ContextRecordMapper;
import org.rabix.engine.model.ContextRecord;
import org.rabix.engine.model.ContextRecord.ContextStatus;
import org.rabix.engine.repository.ContextRecordRepository;
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

  @SqlUpdate("insert into context_record (id,status,config) values (:id,:status,:config)")
  int insert(@BindContextRecord ContextRecord contextRecord);
  
  @SqlUpdate("update context_record set status=:status,config=:config where id=:id")
  int update(@BindContextRecord ContextRecord contextRecord);
  
  @SqlQuery("select * from context_record where id=:id")
  ContextRecord get(@Bind("id") String id);
  
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
          }
        };
      }
    }
  }
  
  public static class ContextRecordMapper implements ResultSetMapper<ContextRecord> {
    public ContextRecord map(int index, ResultSet resultSet, StatementContext ctx) throws SQLException {
      String id = resultSet.getString("ID");
      String config = resultSet.getString("CONFIG");
      String status = resultSet.getString("STATUS");

      Map<String, Object> configObject = JSONHelper.readMap(config);
      return new ContextRecord(id, configObject, ContextStatus.valueOf(status));
    }
  }
}
