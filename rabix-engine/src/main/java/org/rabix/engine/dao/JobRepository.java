package org.rabix.engine.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import org.rabix.bindings.model.Job;
import org.rabix.common.json.BeanSerializer;
import org.rabix.engine.dao.JobRepository.JobMapper;
import org.rabix.engine.dao.bindings.BindJson;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

@RegisterMapper(JobMapper.class)
public interface JobRepository {

  @SqlUpdate("insert into job (id,root_id,job,group_id) values (:id,:root_id,:job::jsonb,:group_id)")
  void insert(@Bind("id") String id, @Bind("root_id") String rootId, @BindJson("job") String job, @Bind("group_id") String groupId);
  
  @SqlUpdate("update job set job=:job where id=:id")
  void update(@Bind("id") String id, @BindJson("job") String job);
  
  @SqlQuery("select * from job where id=:id")
  Job get(@Bind("id") String id);
  
  @SqlQuery("select * from job")
  Set<Job> get();
  
  @SqlQuery("select * from job where root_id=:root_id")
  Set<Job> getByRootId(@Bind("root_id") String rootId);
  
  @SqlQuery("select * from job where group_id=:group_id")
  Set<Job> getJobsByGroupId(@Bind("group_id") String group_id);
  
  public static class JobMapper implements ResultSetMapper<Job> {
    public Job map(int index, ResultSet r, StatementContext ctx) throws SQLException {
      return BeanSerializer.deserialize(r.getString("job"), Job.class);
    }
  }

}