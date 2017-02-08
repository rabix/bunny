package org.rabix.engine.jdbi.impl;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
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
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

@RegisterMapper(LinkRecordMapper.class)
public abstract class JDBILinkRecordRepository extends LinkRecordRepository {

  @SqlUpdate("insert into link_record (root_id,source_job_name,source_job_port,source_type,destination_job_name,destination_job_port,destination_type,position) values (:root_id,:source_job_name,:source_job_port,:source_type::port_type,:destination_job_name,:destination_job_port,:destination_type::port_type,:position)")
  public abstract int insert(@BindLinkRecord LinkRecord linkRecord);
  
  @SqlUpdate("update link_record set root_id=:root_id,source_job_name=:source_job_name,source_job_port=:source_job_port,source_type=:source_type::port_type,destination_job_name=:destination_job_name,destination_job_port=:destination_job_port,destination_type=:destination_type::port_type,position=:position where root_id=:root_id and source_job_name=:source_job_name and source_job_port=:source_job_port and source_type=:source_type and destination_job_name=:destination_job_name and destination_job_port=:destination_job_port and destination_type=:destination_type")
  public abstract int update(@BindLinkRecord LinkRecord linkRecord);
 
  @SqlBatch("insert into link_record (root_id,source_job_name,source_job_port,source_type,destination_job_name,destination_job_port,destination_type,position) values (:root_id,:source_job_name,:source_job_port,:source_type::port_type,:destination_job_name,:destination_job_port,:destination_type::port_type,:position)")
  public abstract void insertBatch(@BindLinkRecord Iterator<LinkRecord> records);
  
  @SqlBatch("update link_record set root_id=:root_id,source_job_name=:source_job_name,source_job_port=:source_job_port,source_type=:source_type::port_type,destination_job_name=:destination_job_name,destination_job_port=:destination_job_port,destination_type=:destination_type::port_type,position=:position where root_id=:root_id and source_job_name=:source_job_name and source_job_port=:source_job_port and source_type=:source_type::port_type and destination_job_name=:destination_job_name and destination_job_port=:destination_job_port and destination_type=:destination_type::port_type")
  public abstract void updateBatch(@BindLinkRecord Iterator<LinkRecord> records);
  
  @SqlQuery("select * from link_record where source_job_name=:source_job_name and source_job_port=:source_job_port and root_id=:root_id")
  public abstract List<LinkRecord> getBySourcePort(@Bind("source_job_name") String sourceJobId, @Bind("source_job_port") String sourceJobPortId, @Bind("root_id") UUID rootId);
  
  @SqlQuery("select * from link_record where source_job_name=:source_job_name and root_id=:root_id")
  public abstract List<LinkRecord> getBySourceJob(@Bind("source_job_name") String sourceJobName, @Bind("root_id") UUID rootId);
  
  @SqlQuery("select * from link_record where source_job_name=:source_job_name and source_type=:source_type::port_type and root_id=:root_id")
  public abstract List<LinkRecord> getBySourceJobAndSourceType(@Bind("source_job_name") String sourceJobName, @Bind("source_type") LinkPortType sourceType, @Bind("root_id") UUID rootId);
  
  @SqlQuery("select * from link_record where source_job_name=:source_job_name and source_job_port=:source_job_port and destination_type=:destination_type::port_type and root_id=:root_id")
  public abstract List<LinkRecord> getBySourcePortAndDestinationType(@Bind("source_job_name") String sourceJobId, @Bind("source_job_port") String sourceJobPort, @Bind("destination_type") LinkPortType destinationType, @Bind("root_id") UUID rootId);
  
  @BindingAnnotation(BindLinkRecord.LinkBinderFactory.class)
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ ElementType.PARAMETER })
  public static @interface BindLinkRecord {
    public static class LinkBinderFactory implements BinderFactory<Annotation> {
      public Binder<BindLinkRecord, LinkRecord> build(Annotation annotation) {
        return new Binder<BindLinkRecord, LinkRecord>() {
          public void bind(SQLStatement<?> q, BindLinkRecord bind, LinkRecord linkRecord) {
            q.bind("root_id", linkRecord.getRootId());
            q.bind("source_job_name", linkRecord.getSourceJobName());
            q.bind("source_job_port", linkRecord.getSourceJobPort());
            q.bind("source_type", linkRecord.getSourceVarType());
            q.bind("destination_job_name", linkRecord.getDestinationJobName());
            q.bind("destination_job_port", linkRecord.getDestinationJobPort());
            q.bind("destination_type", linkRecord.getDestinationVarType());
            q.bind("position", linkRecord.getPosition());
          }
        };
      }
    }
  }
  
  public static class LinkRecordMapper implements ResultSetMapper<LinkRecord> {
    public LinkRecord map(int index, ResultSet resultSet, StatementContext ctx) throws SQLException {
      UUID rootId = resultSet.getObject("root_id", UUID.class);
      String sourceJobName = resultSet.getString("source_job_name");
      String sourceJobPort = resultSet.getString("source_job_port");
      String sourceType = resultSet.getString("source_type");
      String destinationJobName = resultSet.getString("destination_job_name");
      String destinationJobPort = resultSet.getString("destination_job_port");
      String destinationType = resultSet.getString("destination_type");
      Integer position = resultSet.getInt("position");
      return new LinkRecord(rootId, sourceJobName, sourceJobPort, LinkPortType.valueOf(sourceType), destinationJobName, destinationJobPort, LinkPortType.valueOf(destinationType), position);
    }
  }
  
}
