package org.rabix.engine.store.postgres.jdbi.impl;

import org.rabix.engine.store.postgres.jdbi.impl.JDBIJobStatsRecordRepository.JobStatsRecordMapper;
import org.rabix.engine.store.model.JobStatsRecord;
import org.rabix.engine.store.repository.JobStatsRecordRepository;
import org.skife.jdbi.v2.SQLStatement;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.lang.annotation.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@RegisterMapper(JobStatsRecordMapper.class)
public interface JDBIJobStatsRecordRepository extends JobStatsRecordRepository {

  @Override
  @SqlUpdate("insert into job_stats (root_id,completed,running, total) values (:root_id,:completed,:running,:total)")
  int insert(@BindJobStatsRecord JobStatsRecord jobStatsRecord);
  
  @Override
  @SqlUpdate("update job_stats set completed=:completed,running=:running,total=:total where root_id=:root_id")
  int update(@BindJobStatsRecord JobStatsRecord jobStatsRecord);
  
  @Override
  @SqlQuery("select * from job_stats where root_id=:root_id")
  JobStatsRecord get(@Bind("root_id") UUID id);

  @Override
  @SqlUpdate("deleteGroup from job_stats where root_id=:root_id")
  int delete(@Bind("root_id") UUID id);

  @BindingAnnotation(BindJobStatsRecord.JobStatsBinderFactory.class)
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ ElementType.PARAMETER })
  public static @interface BindJobStatsRecord {
    public static class JobStatsBinderFactory implements BinderFactory<Annotation> {
      public Binder<BindJobStatsRecord, JobStatsRecord> build(Annotation annotation) {
        return new Binder<BindJobStatsRecord, JobStatsRecord>() {
          public void bind(SQLStatement<?> q, BindJobStatsRecord bind, JobStatsRecord jobStatsRecord) {
            q.bind("root_id", jobStatsRecord.getRootId());
            q.bind("completed", jobStatsRecord.getCompleted());
            q.bind("running", jobStatsRecord.getRunning());
            q.bind("total", jobStatsRecord.getTotal());
          }
        };
      }
    }
  }
  
  public static class JobStatsRecordMapper implements ResultSetMapper<JobStatsRecord> {
    public JobStatsRecord map(int index, ResultSet resultSet, StatementContext ctx) throws SQLException {
      UUID id = resultSet.getObject("ROOT_ID", UUID.class);
      int completed = resultSet.getInt("COMPLETED");
      int running = resultSet.getInt("RUNNING");
      int total = resultSet.getInt("TOTAL");
      return new JobStatsRecord(id, completed, running, total);
    }
  }
}
