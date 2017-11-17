package org.rabix.engine.store.postgres.jdbi.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import org.postgresql.util.PGobject;
import org.rabix.common.helper.JSONHelper;
import org.rabix.engine.store.model.JobRecord;
import org.rabix.engine.store.model.JobRecord.JobIdRootIdPair;
import org.rabix.engine.store.model.JobRecord.PortCounter;
import org.rabix.engine.store.model.scatter.ScatterStrategy;
import org.rabix.engine.store.postgres.jdbi.impl.JDBIJobRecordRepository.JobRecordMapper;
import org.rabix.engine.store.repository.JobRecordRepository;
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
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RegisterMapper(JobRecordMapper.class)
@UseStringTemplate3StatementLocator
public abstract class JDBIJobRecordRepository extends JobRecordRepository {

  @Override
  @SqlUpdate("insert into job_record (id,external_id,root_id,parent_id,blocking,job_state,input_counters,output_counters,is_scattered,is_container,is_scatter_wrapper,global_inputs_count,global_outputs_count,scatter_strategy,dag_hash,created_at,modified_at) values (:id,:external_id,:root_id,:parent_id,:blocking,:job_state::job_record_state,:input_counters,:output_counters,:is_scattered,:is_container,:is_scatter_wrapper,:global_inputs_count,:global_outputs_count,:scatter_strategy,:dag_hash, :created_at,:modified_at)")
  public abstract int insert(@BindJobRecord JobRecord jobRecord);

  @Override
  @SqlUpdate("update job_record set id=:id,external_id=:external_id,root_id=:root_id,parent_id=:parent_id,blocking=:blocking,job_state=:job_state::job_record_state,input_counters=:input_counters,output_counters=:output_counters,is_scattered=:is_scattered,is_container=:is_container,is_scatter_wrapper=:is_scatter_wrapper,global_inputs_count=:global_inputs_count,global_outputs_count=:global_outputs_count,scatter_strategy=:scatter_strategy,dag_hash=:dag_hash,modified_at='now'::timestamp where id=:id and root_id=:root_id")
  public abstract int update(@BindJobRecord JobRecord jobRecord);

  @Override
  @SqlBatch("insert into job_record (id,external_id,root_id,parent_id,blocking,job_state,input_counters,output_counters,is_scattered,is_container,is_scatter_wrapper,global_inputs_count,global_outputs_count,scatter_strategy,dag_hash,created_at,modified_at) values (:id,:external_id,:root_id,:parent_id,:blocking,:job_state::job_record_state,:input_counters,:output_counters,:is_scattered,:is_container,:is_scatter_wrapper,:global_inputs_count,:global_outputs_count,:scatter_strategy,:dag_hash,:created_at,:modified_at)")
  public abstract void insertBatch(@BindJobRecord Iterator<JobRecord> records);

  @Override
  @SqlBatch("update job_record set id=:id,external_id=:external_id,root_id=:root_id,parent_id=:parent_id,blocking=:blocking,job_state=:job_state::job_record_state,input_counters=:input_counters,output_counters=:output_counters,is_scattered=:is_scattered,is_container=:is_container,is_scatter_wrapper=:is_scatter_wrapper,global_inputs_count=:global_inputs_count,global_outputs_count=:global_outputs_count,scatter_strategy=:scatter_strategy,dag_hash=:dag_hash,modified_at='now'::timestamp where id=:id and root_id=:root_id")
  public abstract void updateBatch(@BindJobRecord Iterator<JobRecord> records);

  @Override
  @SqlUpdate("update job_record set job_state=:state::job_record_state where root_id=:root_id and job_state::text in (<states>)")
  public abstract void updateStatus(@Bind("root_id") UUID rootId, @Bind("state") JobRecord.JobState state, @BindIn("states") Set<JobRecord.JobState> whereStates);

  @Override
  @SqlUpdate("delete from job_record where job_state=:state::job_record_state")
  public abstract int deleteByStatus(@Bind("state") JobRecord.JobState state);

  @Override
  @SqlBatch("delete from job_record where external_id=:external_id and root_id=:root_id")
  public abstract void delete(@Bind("external_id") UUID externalId, @Bind("root_id") UUID rootId);

  @Override
  @SqlQuery("select * from job_record where root_id=:root_id")
  public abstract List<JobRecord> get(@Bind("root_id") UUID rootId);

  @Override
  @SqlQuery("select * from job_record where id='root' and root_id=:root_id")
  public abstract JobRecord getRoot(@Bind("root_id") UUID rootId);

  @Override
  @SqlQuery("select * from job_record where id=:id and root_id=:root_id")
  public abstract JobRecord get(@Bind("id") String id, @Bind("root_id") UUID rootId);

  @Override
  @SqlQuery("select * from job_record where parent_id=:parent_id and root_id=:root_id")
  public abstract List<JobRecord> getByParent(@Bind("parent_id") UUID parentId, @Bind("root_id") UUID rootId);

  @Override
  @SqlQuery("select * from job_record where job_state='READY'::job_record_state and root_id=:root_id")
  public abstract List<JobRecord> getReady(@Bind("root_id") UUID rootId);

  @Override
  @SqlQuery("select * from job_record where job_state::text in (<states>) and root_id=:root_id")
  public abstract List<JobRecord> get(@Bind("root_id") UUID rootId, @BindIn("states") Set<JobRecord.JobState> states);

  @BindingAnnotation(BindJobRecord.JobBinderFactory.class)
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ ElementType.PARAMETER })
  public static @interface BindJobRecord {
    public static class JobBinderFactory implements BinderFactory<Annotation> {
      public Binder<BindJobRecord, JobRecord> build(Annotation annotation) {
        return new Binder<BindJobRecord, JobRecord>() {
          public void bind(SQLStatement<?> q, BindJobRecord bind, JobRecord jobRecord) {
            q.bind("id", jobRecord.getId());
            q.bind("external_id", jobRecord.getExternalId());
            q.bind("root_id", jobRecord.getRootId());
            q.bind("parent_id", jobRecord.getParentId());
            q.bind("blocking", jobRecord.isBlocking());
            q.bind("job_state", jobRecord.getState());

            try {
              PGobject data = new PGobject();
              data.setType("jsonb");
              data.setValue(JSONHelper.writeObject(jobRecord.getInputCounters()));
              q.bind("input_counters", data);
            } catch (SQLException ex) {
              throw new IllegalStateException("Error Binding input counters", ex);
            }

            try {
              PGobject data = new PGobject();
              data.setType("jsonb");
              data.setValue(JSONHelper.writeObject(jobRecord.getOutputCounters()));
              q.bind("output_counters", data);
            } catch (SQLException ex) {
              throw new IllegalStateException("Error Binding output counters", ex);
            }

            q.bind("is_scattered", jobRecord.isScattered());
            q.bind("is_container", jobRecord.isContainer());
            q.bind("is_scatter_wrapper", jobRecord.isScatterWrapper());
            q.bind("global_inputs_count", jobRecord.getNumberOfGlobalInputs());
            q.bind("global_outputs_count", jobRecord.getNumberOfGlobalOutputs());

            try {
              PGobject data = new PGobject();
              data.setType("jsonb");
              data.setValue(JSONHelper.writeObject(jobRecord.getScatterStrategy()));
              q.bind("scatter_strategy", data);
            } catch (SQLException ex) {
              throw new IllegalStateException("Error Binding output counters", ex);
            }

            q.bind("dag_hash", jobRecord.getDagHash());
            q.bind("modified_at", Timestamp.valueOf(jobRecord.getModifiedAt()));
            q.bind("created_at", Timestamp.valueOf(jobRecord.getCreatedAt()));

          }
        };
      }
    }
  }

  @BindingAnnotation(BindJobIdRootId.JobBinderFactory.class)
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ ElementType.PARAMETER })
  public static @interface BindJobIdRootId {
    public static class JobBinderFactory implements BinderFactory<Annotation> {
      public Binder<BindJobIdRootId, JobIdRootIdPair> build(Annotation annotation) {
        return new Binder<BindJobIdRootId, JobIdRootIdPair>() {
          public void bind(SQLStatement<?> q, BindJobIdRootId bind, JobIdRootIdPair pair) {
            q.bind("id", pair.id);
            q.bind("root_id", pair.rootId);
          }
        };
      }
    }
  }

  public static class JobRecordMapper implements ResultSetMapper<JobRecord> {
    public JobRecord map(int index, ResultSet resultSet, StatementContext ctx) throws SQLException {
      String id = resultSet.getString("id");
      UUID externalId = resultSet.getObject("external_id", UUID.class);
      UUID rootId = resultSet.getObject("root_id", UUID.class);
      UUID parentId = resultSet.getObject("parent_id", UUID.class);
      Boolean isBlocking = resultSet.getBoolean("blocking");
      String jobState = resultSet.getString("job_state");
      String inputCounters = resultSet.getString("input_counters");
      String outputCounters = resultSet.getString("output_counters");
      Boolean isScattered = resultSet.getBoolean("is_scattered");
      Boolean isContainer = resultSet.getBoolean("is_container");
      Boolean isScatterWrapper = resultSet.getBoolean("is_scatter_wrapper");
      Integer globalInputsCount = resultSet.getInt("global_inputs_count");
      Integer globalOutputsCount = resultSet.getInt("global_outputs_count");
      String scatterStrategy = resultSet.getString("scatter_strategy");
      String dagHash = resultSet.getString("dag_hash");
      LocalDateTime createdAt = resultSet.getTimestamp("created_at").toLocalDateTime();
      LocalDateTime modifiedAt = resultSet.getTimestamp("modified_at").toLocalDateTime();

      JobRecord jobRecord = new JobRecord(rootId, id, externalId, parentId, JobRecord.JobState.valueOf(jobState), isContainer, isScattered, externalId.equals(rootId), isBlocking, dagHash, createdAt, modifiedAt);
      jobRecord.setScatterWrapper(isScatterWrapper);
      jobRecord.setNumberOfGlobalInputs(globalInputsCount);
      jobRecord.setNumberOfGlobalOutputs(globalOutputsCount);
      jobRecord.setScatterStrategy(JSONHelper.readObject(scatterStrategy, ScatterStrategy.class));
      jobRecord.setInputCounters(JSONHelper.readObject(inputCounters, new TypeReference<List<PortCounter>>() {}));
      jobRecord.setOutputCounters(JSONHelper.readObject(outputCounters, new TypeReference<List<PortCounter>>() {}));
      return jobRecord;
    }
  }

}
