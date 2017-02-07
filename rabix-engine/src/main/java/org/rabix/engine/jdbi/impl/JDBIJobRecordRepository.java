package org.rabix.engine.jdbi.impl;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.postgresql.util.PGobject;
import org.rabix.common.helper.JSONHelper;
import org.rabix.engine.jdbi.impl.JDBIJobRecordRepository.JobRecordMapper;
import org.rabix.engine.model.JobRecord;
import org.rabix.engine.model.JobRecord.PortCounter;
import org.rabix.engine.model.scatter.ScatterStrategy;
import org.rabix.engine.repository.JobRecordRepository;
import org.skife.jdbi.v2.SQLStatement;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.Binder;
import org.skife.jdbi.v2.sqlobject.BinderFactory;
import org.skife.jdbi.v2.sqlobject.BindingAnnotation;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import com.fasterxml.jackson.core.type.TypeReference;

@RegisterMapper(JobRecordMapper.class)
public abstract class JDBIJobRecordRepository extends JobRecordRepository {

  @SqlUpdate("insert into job_record (id,external_id,root_id,parent_id,blocking,job_state,input_counters,output_counters,is_scattered,is_container,is_scatter_wrapper,global_inputs_count,global_outputs_count,scatter_strategy,dag_cache) values (:id,:external_id,:root_id,:parent_id,:blocking,:job_state,:input_counters,:output_counters,:is_scattered,:is_container,:is_scatter_wrapper,:global_inputs_count,:global_outputs_count,:scatter_strategy,:dag_cache)")
  public abstract int insert(@BindJobRecord JobRecord jobRecord);
  
  @SqlUpdate("update job_record set id=:id,external_id=:external_id,root_id=:root_id,parent_id=:parent_id,blocking=:blocking,job_state=:job_state,input_counters=:input_counters,output_counters=:output_counters,is_scattered=:is_scattered,is_container=:is_container,is_scatter_wrapper=:is_scatter_wrapper,global_inputs_count=:global_inputs_count,global_outputs_count=:global_outputs_count,scatter_strategy=:scatter_strategy,dag_cache=:dag_cache where id=:id and root_id=:root_id")
  public abstract int update(@BindJobRecord JobRecord jobRecord);
  
  @SqlQuery("select * from job_record where root_id=:root_id")
  public abstract List<JobRecord> get(@Bind("root_id") UUID rootId);
  
  @SqlQuery("select * from job_record where name='root' and root_id=:root_id")
  public abstract JobRecord getRoot(@Bind("root_id") UUID rootId);
  
  @SqlQuery("select * from job_record where name=:name and root_id=:root_id")
  public abstract JobRecord get(@Bind("name") String name, @Bind("root_id") UUID rootId);
  
  @SqlQuery("select * from job_record where parent_id=:parent_id and root_id=:root_id")
  public abstract List<JobRecord> getByParent(@Bind("parent_id") UUID parentId, @Bind("root_id") UUID rootId);
  
  @SqlQuery("select * from job_record where job_state='READY' and root_id=?")
  public abstract List<JobRecord> getReady(@Bind("root_id") UUID rootId);
  
  @BindingAnnotation(BindJobRecord.JobBinderFactory.class)
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ ElementType.PARAMETER })
  public static @interface BindJobRecord {
    public static class JobBinderFactory implements BinderFactory<Annotation> {
      public Binder<BindJobRecord, JobRecord> build(Annotation annotation) {
        return new Binder<BindJobRecord, JobRecord>() {
          public void bind(SQLStatement<?> q, BindJobRecord bind, JobRecord jobRecord) {
            q.bind("name", jobRecord.getName());
            q.bind("id", jobRecord.getId());
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
            
            q.bind("dag_cache", jobRecord.getDagCache());
          }
        };
      }
    }
  }
  
  public static class JobRecordMapper implements ResultSetMapper<JobRecord> {
    public JobRecord map(int index, ResultSet resultSet, StatementContext ctx) throws SQLException {
      String name = resultSet.getString("id");
      UUID id = resultSet.getObject("id", UUID.class);
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
      String dagCache = resultSet.getString("dag_cache");

      JobRecord jobRecord = new JobRecord(rootId, name, id, parentId, JobRecord.JobState.valueOf(jobState), isContainer, isScattered, id.equals(rootId), isBlocking, dagCache);
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
