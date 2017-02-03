package org.rabix.engine.jdbi.impl;

import java.lang.annotation.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;

import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Resources;
import org.rabix.common.json.BeanSerializer;
import org.rabix.engine.jdbi.impl.JDBIJobRepository.JobMapper;
import org.rabix.engine.repository.JobRepository;
import org.skife.jdbi.v2.SQLStatement;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterColumnMapper;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultColumnMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

@RegisterMapper(JobMapper.class)
@RegisterColumnMapper(JDBIJobRepository.ResourcesMapper.class)
public interface JDBIJobRepository extends JobRepository {

  @SqlUpdate("insert into job (id,root_id,job,group_id) values (:id,:root_id,:job::jsonb,:group_id)")
  void insert(@BindJob Job job);
  
  @SqlUpdate("update job set job=:job where id=:id")
  void update(@BindJob Job job);
  
  @SqlQuery("select * from job where id=:id")
  Job get(@Bind("id") UUID id);
  
  @SqlQuery("select * from job")
  Set<Job> get();
  
  @SqlQuery("select * from job where root_id=:root_id")
  Set<Job> getByRootId(@Bind("root_id") UUID rootId);
  
  @SqlQuery("select * from job where group_id=:group_id")
  Set<Job> getByGroupId(@Bind("group_id") UUID group_id);

  public static class ResourcesMapper implements ResultColumnMapper<Resources> {

    @Override
    public Resources mapColumn(ResultSet resultSet, int i, StatementContext statementContext) throws SQLException {
      return new Resources(0L, 0L, 0L, false, "", "", 0L, 0L);
    }

    @Override
    public Resources mapColumn(ResultSet resultSet, String s, StatementContext statementContext) throws SQLException {
      System.out.println(resultSet);
      System.out.println(s);
      System.out.println(statementContext);
      return new Resources(0L, 0L, 0L, false, "", "", 0L, 0L);
    }
  }
  
  public static class JobMapper implements ResultSetMapper<Job> {
    public Job map(int index, ResultSet r, StatementContext ctx) throws SQLException {
      UUID id = r.getObject("id", UUID.class);
      UUID root_id = r.getObject("id", UUID.class);
      UUID parent_id = r.getObject("id", UUID.class);
      String name = r.getString("");
      Job.JobStatus status = Job.JobStatus.valueOf(r.getString("status"));
      String message = r.getString("message");
      String inputsJson = r.getString("inputs");
      String outputsJson = r.getString("outputs");
      Resources resources = r.getObject("resources", Resources.class);
      return BeanSerializer.deserialize(r.getString("job"), Job.class);
    }
  }

  @BindingAnnotation(JDBIJobRepository.BindJob.JobBinderFactory.class)
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ ElementType.PARAMETER })
  public static @interface BindJob {
    public static class JobBinderFactory implements BinderFactory<Annotation> {
      public Binder<JDBIJobRepository.BindJob, Job> build(Annotation annotation) {
        return new Binder<JDBIJobRepository.BindJob, Job>() {
          public void bind(SQLStatement<?> q, JDBIJobRepository.BindJob bind, Job job) {

          }
        };
      }
    }
  }

}