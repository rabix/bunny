package org.rabix.engine.jdbi.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;

import org.rabix.engine.SchemaHelper;
import org.rabix.engine.db.JobBackendService.BackendJob;
import org.rabix.engine.jdbi.impl.JDBIJobBackendRepository.BackendJobMapper;
import org.rabix.engine.repository.JobBackendRepository;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

@RegisterMapper(BackendJobMapper.class)
public interface JDBIJobBackendRepository extends JobBackendRepository {

  @Override
  @SqlUpdate("insert into job_backend (job_id,root_id,backend_id) values (:job_id,:root_id,:backend_id)")
  int insert(@Bind("job_id") UUID jobId, @Bind("root_id") UUID rootId, @Bind("backend_id") UUID backendId);
  
  @Override
  @SqlUpdate("update job_backend set backend_id=:backend_id where job_id=:job_id")
  int update(@Bind("job_id") UUID jobId, @Bind("backend_id") UUID backendId);
  
  @Override
  @SqlUpdate("delete from job_backend where job_id=:job_id")
  int delete(@Bind("job_id") UUID jobId);
  
  @Override
  @SqlQuery("select * from job_backend where job_id=:job_id")
  BackendJob getByJobId(@Bind("job_id") UUID jobId);
  
  @Override
  @SqlQuery("select * from job_backend where root_id=:root_id")
  Set<BackendJob> getByRootId(@Bind("root_id") UUID rootId);
  
  @Override
  @SqlQuery("select * from job_backend where backend_id=:backend_id")
  Set<BackendJob> getByBackendId(@Bind("backend_id") UUID backendId);
  
  @Override
  @SqlQuery("select * from job_backend where backend_id is null")
  Set<BackendJob> getFreeJobs();
  
  @Override
  @SqlQuery("select * from job_backend where root_id=:root_id and backend_id is null")
  Set<BackendJob> getFreeJobs(@Bind("root_id") UUID rootId);
  
  public static class BackendJobMapper implements ResultSetMapper<BackendJob> {
    public BackendJob map(int index, ResultSet resultSet, StatementContext ctx) throws SQLException {
      UUID jobId = resultSet.getObject("job_id", UUID.class);
      UUID backendId = resultSet.getObject("backend_id", UUID.class);
      UUID rootId = resultSet.getObject("root_id", UUID.class);
      return new BackendJob(SchemaHelper.fromUUID(jobId), SchemaHelper.fromUUID(rootId), SchemaHelper.fromUUID(backendId));
    }
  }

}
