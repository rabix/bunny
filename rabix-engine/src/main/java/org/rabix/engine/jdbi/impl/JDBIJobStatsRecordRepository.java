package org.rabix.engine.jdbi.impl;

import org.rabix.engine.jdbi.impl.JDBIJobStatsRecordRepository.JobStatsRecordMapper;
import org.rabix.engine.model.JobStatsRecord;
import org.rabix.engine.repository.JobStatsRecordRepository;
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
  @SqlUpdate("insert into job_stats (job_id,completed,running) values (:job_id,:completed,:running)")
  int insert(@BindJobStatsRecord JobStatsRecord jobStatsRecord);
  
  @Override
  @SqlUpdate("update job_stats set completed=:completed,running=:running where job_id=:job_id")
  int update(@BindJobStatsRecord JobStatsRecord jobStatsRecord);
  
  @Override
  @SqlQuery("select * from job_stats where job_id=:job_id")
  JobStatsRecord get(@Bind("job_id") UUID id);

  @Override
  @SqlUpdate("delete from job_stats where job_id=:job_id")
  int delete(@Bind("job_id") UUID id);

  @BindingAnnotation(BindJobStatsRecord.JobStatsBinderFactory.class)
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ ElementType.PARAMETER })
  public static @interface BindJobStatsRecord {
    public static class JobStatsBinderFactory implements BinderFactory<Annotation> {
      public Binder<BindJobStatsRecord, JobStatsRecord> build(Annotation annotation) {
        return new Binder<BindJobStatsRecord, JobStatsRecord>() {
          public void bind(SQLStatement<?> q, BindJobStatsRecord bind, JobStatsRecord jobStatsRecord) {
            q.bind("job_id", jobStatsRecord.getJobId());
            q.bind("completed", jobStatsRecord.getCompleted());
            q.bind("running", jobStatsRecord.getRunning());
          }
        };
      }
    }
  }
  
  public static class JobStatsRecordMapper implements ResultSetMapper<JobStatsRecord> {
    public JobStatsRecord map(int index, ResultSet resultSet, StatementContext ctx) throws SQLException {
      UUID id = resultSet.getObject("JOB_ID", UUID.class);
      int completed = resultSet.getInt("COMPLETED");
      int running = resultSet.getInt("RUNNING");
      return new JobStatsRecord(id, completed, running);
    }
  }
}
