package org.rabix.engine.store.postgres.jdbi.impl;

import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.bindings.model.Resources;
import org.rabix.common.helper.JSONHelper;
import org.rabix.engine.store.postgres.jdbi.impl.JDBIJobRepository.BackendIDMapper;
import org.rabix.engine.store.postgres.jdbi.impl.JDBIJobRepository.JobEntityMapper;
import org.rabix.engine.store.postgres.jdbi.impl.JDBIJobRepository.JobMapper;
import org.rabix.engine.store.repository.JobRepository;
import org.skife.jdbi.v2.SQLStatement;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import org.skife.jdbi.v2.unstable.BindIn;

import java.lang.annotation.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

@RegisterMapper({ JobMapper.class, JobEntityMapper.class, BackendIDMapper.class })
@UseStringTemplate3StatementLocator
public interface JDBIJobRepository extends JobRepository {

  @Override
  @SqlUpdate("insert into job (id,root_id,name, parent_id, status, message, inputs, outputs, resources, group_id, produced_by_node, app, config) values (:id,:root_id,:name,:parent_id,:status::job_status,:message,:inputs,:outputs,:resources::jsonb,:group_id,:produced_by_node,:app,:config::jsonb)")
  void insert(@BindJob Job job, @Bind("group_id") UUID groupId, @Bind("produced_by_node") String producedByNode);

  @Override
  @SqlUpdate("update job set root_id=:root_id,name=:name, parent_id=:parent_id, status=:status::job_status, message=:message, inputs=:inputs, outputs=:outputs, resources=:resources::jsonb,app=:app,config=:config::jsonb,modified_at='now' where id=:id")
  void update(@BindJob Job job);

  @Override
  @SqlBatch("update job set root_id=:root_id,name=:name, parent_id=:parent_id, status=:status::job_status, message=:message, inputs=:inputs, outputs=:outputs, resources=:resources::jsonb,app=:app,config=:config::jsonb,modified_at='now' where id=:id")
  void update(@BindJob Iterator<Job> jobs);

  @Override
  @SqlUpdate("update job set backend_id=:backend_id,modified_at='now' where id=:id")
  void updateBackendId(@Bind("id") UUID jobId, @Bind("backend_id") UUID backendId);

  @Override
  @SqlBatch("update job set backend_id=:backend_id,modified_at='now' where id=:id")
  void updateBackendIds(@BindJobEntityBackendId Iterator<JobEntity> entities);

  @Override
  @SqlUpdate("update job set status=:status::job_status,modified_at='now' where status::text in (<statuses>) and root_id=:root_id")
  void updateStatus(@Bind("root_id") UUID rootId, @Bind("status") JobStatus status, @BindIn("statuses") Set<JobStatus> whereStatuses);

  @Override
  @SqlUpdate("update job set backend_id=null, status='READY'::job_status,modified_at='now' where backend_id=:backend_id and status in ('READY'::job_status,'RUNNING'::job_status)")
  void dealocateJobs(@Bind("backend_id") UUID backendId);

  @Override
  @SqlQuery("select * from job where id=:id")
  Job get(@Bind("id") UUID id);

  @Override
  @SqlQuery("select * from job where id=root_id and status=:status::job_status and modified_at \\< :time")
  Set<Job> getRootJobsForDeletion(@Bind("status") JobStatus status, @Bind("time") Timestamp olderThanTime);

  @Override
  @SqlQuery("select status from job where id=:id")
  JobStatus getStatus(@Bind("id") UUID id);

  @Override
  @SqlQuery("select * from job")
  Set<Job> get();

  @Override
  @SqlQuery("select * from job where status::text in (<statuses>) and root_id=:root_id")
  Set<Job> get(@Bind("root_id") UUID rootID, @BindIn("statuses") Set<JobStatus> whereStatuses);

  @Override
  @SqlUpdate("delete from job where root_id=:root_id and id in (<ids>)")
  void delete(@Bind("root_id") UUID rootId, @BindIn("ids") Set<UUID> ids);

  @Override
  @SqlQuery("select backend_id from job where root_id=:root_id")
  Set<UUID> getBackendsByRootId(@Bind("root_id") UUID rootId);

  @Override
  @SqlQuery("select backend_id from job where id=:id")
  UUID getBackendId(@Bind("id") UUID id);

  @Override
  @SqlQuery("select * from job where root_id=:root_id")
  Set<Job> getByRootId(@Bind("root_id") UUID rootId);

  @Override
  @SqlQuery("select * from job where group_id=:group_id and status='READY'::job_status")
  Set<Job> getReadyJobsByGroupId(@Bind("group_id") UUID group_id);

  @Override
  @SqlQuery("select * from job where backend_id is null and status='READY'::job_status")
  Set<JobEntity> getReadyFree();

  @Override
  @SqlUpdate("delete from job where root_id in (<ids>)")
  void deleteByRootIds(@BindIn("ids") Set<UUID> rootIds);

  @Override
  @SqlQuery("select * from job where status=:status::job_status")
  Set<JobEntity> getByStatus(@Bind("status") JobStatus status);

  public static class JobMapper implements ResultSetMapper<Job> {
    public Job map(int index, ResultSet r, StatementContext ctx) throws SQLException {
      UUID id = r.getObject("id", UUID.class);
      UUID root_id = r.getObject("root_id", UUID.class);
      UUID parent_id = r.getObject("parent_id", UUID.class);
      String name = r.getString("name");
      String app = r.getString("app");
      Job.JobStatus status = Job.JobStatus.valueOf(r.getString("status"));
      String message = r.getString("message");
      String inputsJson = new String(r.getBytes("inputs"));
      String outputsJson = new String(r.getBytes("outputs"));
      String resourcesStr = r.getString("resources");
      String configJson = r.getString("config");
      Resources res = JSONHelper.readObject(resourcesStr, Resources.class);

      Map<String, Object> inputs = (Map<String, Object>) FileValue.deserialize(JSONHelper.readMap(inputsJson));
      Map<String, Object> outputs = (Map<String, Object>) FileValue.deserialize(JSONHelper.readMap(outputsJson));
      Map<String, Object> config = JSONHelper.readMap(configJson);

      return new Job(id, parent_id, root_id, name, app, status, message, inputs, outputs, config, res, Collections.emptySet());
    }
  }

  public static class JobEntityMapper implements ResultSetMapper<JobEntity> {
    public JobEntity map(int index, ResultSet r, StatementContext ctx) throws SQLException {
      UUID id = r.getObject("id", UUID.class);
      UUID root_id = r.getObject("root_id", UUID.class);
      UUID groupId = r.getObject("group_id", UUID.class);
      UUID backendId = r.getObject("backend_id", UUID.class);
      UUID parentId = r.getObject("parent_id", UUID.class);
      String name = r.getString("name");
      String app = r.getString("app");
      String producedByNode = r.getString("produced_by_node");
      Job.JobStatus status = Job.JobStatus.valueOf(r.getString("status"));
      String message = r.getString("message");
      String inputsJson = new String(r.getBytes("inputs"));
      String outputsJson = new String(r.getBytes("outputs"));
      String configJson = r.getString("config");
      String resourcesStr = r.getString("resources");
      Resources res = JSONHelper.readObject(resourcesStr, Resources.class);


      Map<String, Object> inputs = (Map<String, Object>) FileValue.deserialize(JSONHelper.readMap(inputsJson));
      Map<String, Object> outputs = (Map<String, Object>) FileValue.deserialize(JSONHelper.readMap(outputsJson));
      Map<String, Object> config = JSONHelper.readMap(configJson);

      Job job = new Job(id, parentId, root_id, name, app, status, message, inputs, outputs, config, res, Collections.emptySet());
      return new JobEntity(job, groupId, producedByNode, backendId);
    }
  }

  public static class BackendIDMapper implements ResultSetMapper<UUID> {
    @Override
    public UUID map(int index, ResultSet r, StatementContext ctx) throws SQLException {
      return r.getObject("backend_id", UUID.class);
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
            q.bind("inputs", JSONHelper.writeObject(job.getInputs()).getBytes());
            q.bind("outputs", JSONHelper.writeObject(job.getOutputs()).getBytes());
            q.bind("app", job.getApp());
            q.bind("resources", JSONHelper.writeObject(job.getResources()));
            q.bind("config", JSONHelper.writeObject(job.getConfig()));
          }
        };
      }
    }
  }

  @BindingAnnotation(JDBIJobRepository.BindJobEntity.JobBinderFactory.class)
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ ElementType.PARAMETER })
  public static @interface BindJobEntity {
    public static class JobBinderFactory implements BinderFactory<Annotation> {
      public Binder<JDBIJobRepository.BindJobEntity, JobEntity> build(Annotation annotation) {
        return new Binder<JDBIJobRepository.BindJobEntity, JobEntity>() {
          public void bind(SQLStatement<?> q, JDBIJobRepository.BindJobEntity bind, JobEntity entity) {
            Job job = entity.getJob();
            if (job != null) {
              q.bind("id", job.getId());
              q.bind("root_id", job.getRootId());
              q.bind("name", job.getName());
              q.bind("parent_id", job.getParentId());
              q.bind("status", job.getStatus().toString());
              q.bind("message", job.getMessage());
              q.bind("inputs", JSONHelper.writeObject(job.getInputs()).getBytes());
              q.bind("outputs", JSONHelper.writeObject(job.getOutputs()).getBytes());
              q.bind("app", job.getApp());
              q.bind("resources", JSONHelper.writeObject(job.getResources()));
              q.bind("config", JSONHelper.writeObject(job.getConfig()));
            }
            q.bind("group_id", entity.getGroupId());
            q.bind("produced_by_node", entity.getProducedByNode());
            q.bind("backend_id", entity.getBackendId());
          }
        };
      }
    }
  }

  @BindingAnnotation(JDBIJobRepository.BindJobEntityBackendId.JobBinderFactory.class)
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ ElementType.PARAMETER })
  public static @interface BindJobEntityBackendId {
    public static class JobBinderFactory implements BinderFactory<Annotation> {
      public Binder<JDBIJobRepository.BindJobEntityBackendId, JobEntity> build(Annotation annotation) {
        return new Binder<JDBIJobRepository.BindJobEntityBackendId, JobEntity>() {
          public void bind(SQLStatement<?> q, JDBIJobRepository.BindJobEntityBackendId bind, JobEntity entity) {
            q.bind("id", entity.getJob().getId());
            q.bind("backend_id", entity.getBackendId());
          }
        };
      }
    }
  }

}