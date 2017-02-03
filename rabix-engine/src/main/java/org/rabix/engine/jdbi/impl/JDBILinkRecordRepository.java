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

import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.engine.jdbi.impl.JDBILinkRecordRepository.LinkRecordMapper;
import org.rabix.engine.model.LinkRecord;
import org.rabix.engine.repository.LinkRecordRepository;
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

@RegisterMapper(LinkRecordMapper.class)
public abstract class JDBILinkRecordRepository extends LinkRecordRepository {

  @SqlUpdate("insert into link_record (context_id,source_job_id,source_job_port_id,source_type,destination_job_id,destination_job_port_id,destination_type,position) values (:context_id,:source_job_id,:source_job_port_id,:source_type,:destination_job_id,:destination_job_port_id,:destination_type,:position)")
  public abstract int insert(@BindLinkRecord LinkRecord linkRecord);
  
  @SqlUpdate("update link_record set context_id=:context_id,source_job_id=:source_job_id,source_job_port_id=:source_job_port_id,source_type=:source_type,destination_job_id=:destination_job_id,destination_job_port_id=:destination_job_port_id,destination_type=:destination_type,position=:position where context_id=:context_id and source_job_id=:source_job_id and source_job_port_id=:source_job_port_id and source_type=:source_type and destination_job_id=:destination_job_id and destination_job_port_id=:destination_job_port_id and destination_type=:destination_type")
  public abstract int update(@BindLinkRecord LinkRecord linkRecord);
  
  @SqlQuery("select * from link_record where source_job_id=:source_job_id and source_job_port_id=:source_job_port_id and context_id=:context_id")
  public abstract List<LinkRecord> getBySource(@Bind("source_job_id") String sourceJobId, @Bind("source_job_port_id") String sourceJobPortId, @Bind("context_id") String rootId);
  
  @SqlQuery("select * from link_record where source_job_id=:source_job_id and context_id=:context_id")
  public abstract List<LinkRecord> getBySourceJobId(@Bind("source_job_id") String sourceJobId, @Bind("context_id") String rootId);
  
  @SqlQuery("select * from link_record where source_job_id=:source_job_id and source_type=:source_type and context_id=:context_id")
  public abstract List<LinkRecord> getBySourceAndSourceType(@Bind("source_job_id") String sourceJobId, @Bind("source_type") LinkPortType sourceType, @Bind("context_id") String rootId);
  
  @SqlQuery("select * from link_record where source_job_id=:source_job_id and source_job_port_id=:source_job_port_id and destination_type=:destination_type and context_id=:context_id")
  public abstract List<LinkRecord> getBySourceAndDestinationType(@Bind("source_job_id") String sourceJobId, @Bind("source_job_port_id") String sourceJobPortId, @Bind("destination_type") LinkPortType destinationType, @Bind("context_id") UUID rootId);
  
  @BindingAnnotation(BindLinkRecord.LinkBinderFactory.class)
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ ElementType.PARAMETER })
  public static @interface BindLinkRecord {
    public static class LinkBinderFactory implements BinderFactory<Annotation> {
      public Binder<BindLinkRecord, LinkRecord> build(Annotation annotation) {
        return new Binder<BindLinkRecord, LinkRecord>() {
          public void bind(SQLStatement<?> q, BindLinkRecord bind, LinkRecord linkRecord) {
            q.bind("context_id", linkRecord.getRootId());
            q.bind("source_job_id", linkRecord.getSourceJobId());
            q.bind("source_job_port_id", linkRecord.getSourceJobPort());
            q.bind("source_type", linkRecord.getSourceVarType());
            q.bind("destination_job_id", linkRecord.getDestinationJobId());
            q.bind("destination_job_port_id", linkRecord.getDestinationJobPort());
            q.bind("destination_type", linkRecord.getDestinationVarType());
            q.bind("position", linkRecord.getPosition());
          }
        };
      }
    }
  }
  
  public static class LinkRecordMapper implements ResultSetMapper<LinkRecord> {
    public LinkRecord map(int index, ResultSet resultSet, StatementContext ctx) throws SQLException {
      UUID rootId = resultSet.getObject("context_id", UUID.class);
      String sourceJobId = resultSet.getString("source_job_id");
      String sourceJobPortId = resultSet.getString("source_job_port_id");
      String sourceType = resultSet.getString("source_type");
      String destinationJobId = resultSet.getString("destination_job_id");
      String destinationJobPortId = resultSet.getString("destination_job_port_id");
      String destinationType = resultSet.getString("destination_type");
      Integer position = resultSet.getInt("position");
      return new LinkRecord(rootId, sourceJobId, sourceJobPortId, LinkPortType.valueOf(sourceType), destinationJobId, destinationJobPortId, LinkPortType.valueOf(destinationType), position);
    }
  }
  
}
