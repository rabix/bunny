package org.rabix.engine.store.postgres.jdbi.impl;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.postgresql.util.PGobject;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.LinkMerge;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.common.helper.JSONHelper;
import org.rabix.engine.store.model.JobRecord;
import org.rabix.engine.store.model.VariableRecord;
import org.rabix.engine.store.cache.Cachable;
import org.rabix.engine.store.postgres.jdbi.impl.JDBIVariableRecordRepository.VariableRecordMapper;
import org.rabix.engine.store.repository.VariableRecordRepository;
import org.skife.jdbi.v2.SQLStatement;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.Binder;
import org.skife.jdbi.v2.sqlobject.BinderFactory;
import org.skife.jdbi.v2.sqlobject.BindingAnnotation;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

@RegisterMapper(VariableRecordMapper.class)
public abstract class JDBIVariableRecordRepository extends VariableRecordRepository {

  @Override
  public int insertCachable(Cachable record) {
    return insert((VariableRecord) record);
  }
  
  @Override
  @SqlUpdate("insert into variable_record (job_id,value,port_id,type,link_merge,is_wrapped,globals_count,times_updated_count,context_id,is_default,transform,created_at,modified_at) values (:job_id,:value,:port_id,:type::port_type,:link_merge::link_merge_type,:is_wrapped,:globals_count,:times_updated_count,:context_id,:is_default,:transform,:created_at,:modified_at)")
  public abstract int insert(@BindVariableRecord VariableRecord jobRecord);
  
  @Override
  @SqlUpdate("update variable_record set value=:value,link_merge=:link_merge::link_merge_type,is_wrapped=:is_wrapped,globals_count=:globals_count,times_updated_count=:times_updated_count,is_default=:is_default,transform=:transform,modified_at='now' where port_id=:port_id and context_id=:context_id and job_id=:job_id and type=:type::port_type")
  public abstract int update(@BindVariableRecord VariableRecord jobRecord);
  
  @Override
  @SqlBatch("insert into variable_record (job_id,value,port_id,type,link_merge,is_wrapped,globals_count,times_updated_count,context_id,is_default,transform,created_at,modified_at) values (:job_id,:value,:port_id,:type::port_type,:link_merge::link_merge_type,:is_wrapped,:globals_count,:times_updated_count,:context_id,:is_default,:transform,:created_at,:modified_at)")
  public abstract void insertBatch(@BindVariableRecord Iterator<VariableRecord> records);
  
  @Override
  @SqlBatch("update variable_record set value=:value,link_merge=:link_merge::link_merge_type,is_wrapped=:is_wrapped,globals_count=:globals_count,times_updated_count=:times_updated_count,is_default=:is_default,transform=:transform,modified_at='now' where port_id=:port_id and context_id=:context_id and job_id=:job_id and type=:type::port_type")
  public abstract void updateBatch(@BindVariableRecord Iterator<VariableRecord> records);
  
  @Override
  @SqlBatch("delete from variable_record where job_id=:id and context_id=:root_id")
  public abstract void delete(@JDBIJobRecordRepository.BindJobIdRootId Set<JobRecord.JobIdRootIdPair> pairs);
  
  @Override
  @SqlQuery("select * from variable_record where job_id=:job_id and port_id=:port_id and type=:type::port_type and context_id=:context_id")
  public abstract VariableRecord get(@Bind("job_id") String jobId, @Bind("port_id") String portId, @Bind("type") LinkPortType type, @Bind("context_id") UUID rootId);
 
  @Override
  @SqlQuery("select * from variable_record where job_id=:job_id and type=:type::port_type and context_id=:context_id")
  public abstract List<VariableRecord> getByType(@Bind("job_id") String jobId, @Bind("type") LinkPortType type, @Bind("context_id") UUID rootId);
  
  @Override
  @SqlQuery("select * from variable_record where job_id=:job_id and port_id=:port_id and context_id=:context_id")
  public abstract List<VariableRecord> getByPort(@Bind("job_id") String jobId, @Bind("port_id") String portId, @Bind("context_id") UUID rootId);
 
  @BindingAnnotation(BindVariableRecord.VariableBinderFactory.class)
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ ElementType.PARAMETER })
  public static @interface BindVariableRecord {
    public static class VariableBinderFactory implements BinderFactory<Annotation> {
      public Binder<BindVariableRecord, VariableRecord> build(Annotation annotation) {
        return new Binder<BindVariableRecord, VariableRecord>() {
          public void bind(SQLStatement<?> q, BindVariableRecord bind, VariableRecord variableRecord) {
            q.bind("job_id", variableRecord.getJobId());
            
            try {
              PGobject data = new PGobject();
              data.setType("jsonb");
              data.setValue(JSONHelper.writeObject(variableRecord.getValue()));
              q.bind("value", data);
            } catch (SQLException ex) {
              throw new IllegalStateException("Error Binding value", ex);
            }
            
            try {
              PGobject data = new PGobject();
              data.setType("jsonb");
              data.setValue(JSONHelper.writeObject(variableRecord.getTransform()));
              q.bind("transform", data);
            } catch (SQLException ex) {
              throw new IllegalStateException("Error Binding value", ex);
            }
            
            q.bind("port_id", variableRecord.getPortId());
            q.bind("type", variableRecord.getType());
            q.bind("link_merge", variableRecord.getLinkMerge());
            q.bind("is_wrapped", variableRecord.isWrapped());
            q.bind("globals_count", variableRecord.getNumberOfGlobals());
            q.bind("times_updated_count", variableRecord.getNumberOfTimesUpdated());
            q.bind("context_id", variableRecord.getRootId());
            q.bind("is_default", variableRecord.isDefault());
            q.bind("created_at", Timestamp.valueOf(variableRecord.getCreatedAt()));
            q.bind("modified_at", Timestamp.valueOf(variableRecord.getModifiedAt()));
          }
        };
      }
    }
  }
  
  public static class VariableRecordMapper implements ResultSetMapper<VariableRecord> {
    public VariableRecord map(int index, ResultSet resultSet, StatementContext ctx) throws SQLException {
      String jobId = resultSet.getString("job_id");
      String value = resultSet.getString("value");
      String transform = resultSet.getString("transform");
      String portId = resultSet.getString("port_id");
      String type = resultSet.getString("type");
      String linkMerge = resultSet.getString("link_merge");
      Boolean isWrapped = resultSet.getBoolean("is_wrapped");
      Integer globalsCount = resultSet.getInt("globals_count");
      Integer timesUpdatedCount = resultSet.getInt("times_updated_count");
      UUID rootId = resultSet.getObject("context_id", UUID.class);
      Boolean isDefault = resultSet.getBoolean("is_default");

      Object valueObject = FileValue.deserialize(JSONHelper.transform(JSONHelper.readJsonNode(value), false));
      
      Object transformObject = JSONHelper.transform(JSONHelper.readJsonNode(transform), false);

      LocalDateTime createdAt = resultSet.getTimestamp("created_at").toLocalDateTime();
      LocalDateTime modifiedAt = resultSet.getTimestamp("modified_at").toLocalDateTime();
      
      VariableRecord variableRecord = new VariableRecord(rootId, jobId, portId, LinkPortType.valueOf(type), valueObject, LinkMerge.valueOf(linkMerge), createdAt, modifiedAt);
      variableRecord.setWrapped(isWrapped);
      variableRecord.setNumberGlobals(globalsCount);
      variableRecord.setNumberOfTimesUpdated(timesUpdatedCount);
      variableRecord.setDefault(isDefault);
      variableRecord.setTransform(transformObject);
      return variableRecord;
    }
  }
  
}
