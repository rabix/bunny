package org.rabix.engine.jdbi.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;

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

  @SqlUpdate("update job set backend_id=:backend_id where id=:id")
  int insert(@Bind("id") UUID jobId, UUID rootId, @Bind("backend_id") UUID backendId);
  
  @SqlUpdate("update job set backend_id=:backend_id where id=:id")
  int update(@Bind("id") UUID jobId, @Bind("backend_id") UUID backendId);
  
  @SqlUpdate("update job set backend_id=NULL where id=:id")
  int delete(@Bind("id") UUID jobId);
  
  @SqlQuery("select id job_id, backend_id, root_id from job where id=:id")
  BackendJob getByJobId(@Bind("id") UUID jobId);
  
  @SqlQuery("select id job_id, backend_id, root_id from job where root_id=:root_id")
  Set<BackendJob> getByRootId(@Bind("root_id") UUID rootId);
  
  @SqlQuery("select id job_id, backend_id, root_id from job where backend_id=:backend_id")
  Set<BackendJob> getByBackendId(@Bind("backend_id") UUID backendId);
  
  @SqlQuery("select id job_id, backend_id, root_id from job where backend_id is null and status='READY'")
  Set<BackendJob> getFreeJobs();
  
  @SqlQuery("select id job_id, backend_id, root_id from job where root_id=:root_id and backend_id is null and status='READY'")
  Set<BackendJob> getFreeJobs(@Bind("root_id") UUID rootId);
  
  public static class BackendJobMapper implements ResultSetMapper<BackendJob> {
    public BackendJob map(int index, ResultSet resultSet, StatementContext ctx) throws SQLException {
      UUID jobId = resultSet.getObject("job_id", UUID.class);
      UUID backendId = resultSet.getObject("backend_id", UUID.class);
      UUID rootId = resultSet.getObject("root_id", UUID.class);
      return new BackendJob(jobId, rootId, backendId);
    }
  }

}
