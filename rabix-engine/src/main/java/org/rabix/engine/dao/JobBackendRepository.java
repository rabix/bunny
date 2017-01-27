package org.rabix.engine.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import org.rabix.engine.dao.JobBackendRepository.BackendJobMapper;
import org.rabix.engine.db.JobBackendService.BackendJob;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

@RegisterMapper(BackendJobMapper.class)
public interface JobBackendRepository {

  @SqlUpdate("insert into job_backend (job_id,root_id,backend_id) values (:job_id,:root_id,:backend_id)")
  int insert(@Bind("job_id") String jobId, @Bind("root_id") String rootId, @Bind("backend_id") String backendId);
  
  @SqlUpdate("update job_backend set backend_id=:backend_id where job_id=:job_id")
  int update(@Bind("job_id") String jobId, @Bind("backend_id") String backendId);
  
  @SqlUpdate("delete from job_backend where job_id=:job_id")
  int delete(@Bind("job_id") String jobId);
  
  @SqlQuery("select * from job_backend where job_id=:job_id")
  BackendJob getByJobId(@Bind("job_id") String jobId);
  
  @SqlQuery("select * from job_backend where root_id=:root_id")
  Set<BackendJob> getByRootId(@Bind("root_id") String rootId);
  
  @SqlQuery("select * from job_backend where backend_id=:backend_id")
  Set<BackendJob> getByBackendId(@Bind("backend_id") String backendId);
  
  @SqlQuery("select * from job_backend where backend_id is null")
  Set<BackendJob> getFreeJobs();
  
  @SqlQuery("select * from job_backend where root_id=:root_id and backend_id is null")
  Set<BackendJob> getFreeJobs(@Bind("root_id") String rootId);
  
  public static class BackendJobMapper implements ResultSetMapper<BackendJob> {
    public BackendJob map(int index, ResultSet resultSet, StatementContext ctx) throws SQLException {
      String jobId = resultSet.getString("job_id");
      String backendId = resultSet.getString("backend_id");
      String rootId = resultSet.getString("root_id");
      return new BackendJob(jobId, rootId, backendId);
    }
  }

}
