package org.rabix.engine.jdbi.impl;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import org.postgresql.util.PGobject;
import org.rabix.common.helper.JSONHelper;
import org.rabix.engine.jdbi.impl.JDBIRootJobRepository.ContextRecordMapper;
import org.rabix.engine.model.RootJob;
import org.rabix.engine.model.RootJob.RootJobStatus;
import org.rabix.engine.repository.RootJobRepository;
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
public interface JDBIRootJobRepository extends RootJobRepository {

  @SqlUpdate("insert into root_job (id,status,config) values (:id,:status::root_job_status,:config)")
  int insert(@BindContextRecord RootJob rootJob);
  
  @SqlUpdate("update root_job set status=:status::root_job_status,config=:config where id=:id")
  int update(@BindContextRecord RootJob rootJob);
  
  @SqlQuery("select * from root_job where id=:id")
  RootJob get(@Bind("id") UUID id);

  @BindingAnnotation(BindContextRecord.ContextBinderFactory.class)
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ ElementType.PARAMETER })
  public static @interface BindContextRecord {
    public static class ContextBinderFactory implements BinderFactory<Annotation> {
      public Binder<BindContextRecord, RootJob> build(Annotation annotation) {
        return new Binder<BindContextRecord, RootJob>() {
          public void bind(SQLStatement<?> q, BindContextRecord bind, RootJob rootJob) {
            q.bind("id", rootJob.getId());
            q.bind("status", rootJob.getStatus());
            try {
              PGobject data = new PGobject();
              data.setType("jsonb");
              data.setValue(JSONHelper.writeObject(rootJob.getConfig()));
              q.bind("config", data);
            } catch (SQLException ex) {
              throw new IllegalStateException("Error Binding config", ex);
            }
          }
        };
      }
    }
  }
  
  public static class ContextRecordMapper implements ResultSetMapper<RootJob> {
    public RootJob map(int index, ResultSet resultSet, StatementContext ctx) throws SQLException {
      UUID id = resultSet.getObject("ID", UUID.class);
      String config = resultSet.getString("CONFIG");
      String status = resultSet.getString("STATUS");

      Map<String, Object> configObject = JSONHelper.readMap(config);
      return new RootJob(id, configObject, RootJobStatus.valueOf(status));
    }
  }
}
