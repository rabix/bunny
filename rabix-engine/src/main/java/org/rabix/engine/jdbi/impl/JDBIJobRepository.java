package org.rabix.engine.jdbi.impl;

import java.lang.annotation.*;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Resources;
import org.rabix.common.helper.JSONHelper;
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

  @SqlUpdate("insert into job (id,root_id,name, parent_id, status, message, inputs, outputs, resources,visible_ports,app) values (:id,:root_id,:name,:parent_id,:status::job_status,:message,:inputs::jsonb,:outputs::jsonb,(:cpu,:mem_mb,:disk_space_mb,:network_access,:working_dir,:tmp_dir,:out_dir_size,:tmp_dir_size)::resources,:visible_ports,:app)")
  void insert(@BindJob Job job);

  @SqlUpdate("insert into job (id,root_id,name, parent_id, status, message, inputs, outputs, resources,visible_ports, group_id,app) values (:id,:root_id,:name,:parent_id,:status::job_status,:message,:inputs::jsonb,:outputs::jsonb,(:cpu,:mem_mb,:disk_space_mb,:network_access,:working_dir,:tmp_dir,:out_dir_size,:tmp_dir_size)::resources,:visible_ports,:group_id,:app)")
  void insertToGroup(@BindJob Job job, @Bind("group_id") UUID groupId);

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
      return Resources.empty();
    }

    @Override
    public Resources mapColumn(ResultSet resultSet, String s, StatementContext statementContext) throws SQLException {
      System.out.println(resultSet);
      System.out.println(s);
      System.out.println(statementContext);
      System.out.println(resultSet.getObject(s));
      return Resources.empty();
    }
  }
  
  public static class JobMapper implements ResultSetMapper<Job> {
    public Job map(int index, ResultSet r, StatementContext ctx) throws SQLException {
      UUID id = r.getObject("id", UUID.class);
      UUID root_id = r.getObject("id", UUID.class);
      UUID parent_id = r.getObject("id", UUID.class);
      String name = r.getString("");
      String app = r.getString("app");
      Job.JobStatus status = Job.JobStatus.valueOf(r.getString("status"));
      String message = r.getString("message");
      String inputsJson = r.getString("inputs");
      String outputsJson = r.getString("outputs");
      Resources resources = r.getObject("resources", Resources.class);
      Array visiblePorts = r.getArray("visible_ports");

      System.out.println(visiblePorts.getArray());

      Map<String, Object> inputs = JSONHelper.readMap(inputsJson);
      Map<String, Object> outputs = JSONHelper.readMap(outputsJson);

      return new Job(id, parent_id, root_id, name, app, status, message, inputs, outputs, Collections.emptyMap(), resources, Collections.emptySet());
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
            q.bind("id", job.getId());
            q.bind("root_id", job.getRootId());
            q.bind("name", job.getName());
            q.bind("parent_id", job.getParentId());
            q.bind("status", job.getStatus().toString());
            q.bind("message", job.getMessage());
            q.bind("inputs", JSONHelper.writeObject(job.getInputs()));
            q.bind("outputs", JSONHelper.writeObject(job.getOutputs()));
            q.bind("app", job.getApp());
            q.bind("visible_ports", job.getVisiblePorts());

            Resources res = job.getResources();

            if (res == null) {
              res = Resources.empty();
            }

            q.bind("cpu", res.getCpu());
            q.bind("mem_mb", res.getMemMB());
            q.bind("disk_space_mb", res.getDiskSpaceMB());
            q.bind("network_access", res.getNetworkAccess());
            q.bind("working_dir", res.getWorkingDir());
            q.bind("tmp_dir", res.getTmpDir());
            q.bind("out_dir_size", res.getOutDirSize());
            q.bind("tmp_dir_size", res.getTmpDirSize());
          }
        };
      }
    }
  }

}