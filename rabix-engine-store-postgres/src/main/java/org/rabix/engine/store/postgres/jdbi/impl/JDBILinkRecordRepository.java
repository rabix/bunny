package org.rabix.engine.store.postgres.jdbi.impl;

import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.engine.store.model.LinkRecord;
import org.rabix.engine.store.postgres.jdbi.impl.JDBILinkRecordRepository.LinkRecordMapper;
import org.rabix.engine.store.repository.LinkRecordRepository;
import org.skife.jdbi.v2.SQLStatement;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.lang.annotation.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@RegisterMapper(LinkRecordMapper.class)
public abstract class JDBILinkRecordRepository extends LinkRecordRepository {

  @Override
  @SqlUpdate("insert into link_record (context_id,source_job_id,source_job_port_id,source_type,destination_job_id,destination_job_port_id,destination_type,position,created_at,modified_at) values (:context_id,:source_job_id,:source_job_port_id,:source_type::port_type,:destination_job_id,:destination_job_port_id,:destination_type::port_type,:position,:created_at,:modified_at)")
  public abstract int insert(@BindLinkRecord LinkRecord linkRecord);

  @Override
  @SqlUpdate("update link_record set context_id=:context_id,source_job_id=:source_job_id,source_job_port_id=:source_job_port_id,source_type=:source_type::port_type,destination_job_id=:destination_job_id,destination_job_port_id=:destination_job_port_id,destination_type=:destination_type::port_type,position=:position,modified_at='now' where context_id=:context_id and source_job_id=:source_job_id and source_job_port_id=:source_job_port_id and source_type=:source_type and destination_job_id=:destination_job_id and destination_job_port_id=:destination_job_port_id and destination_type=:destination_type")
  public abstract int update(@BindLinkRecord LinkRecord linkRecord);

  @Override
  @SqlBatch("insert into link_record (context_id,source_job_id,source_job_port_id,source_type,destination_job_id,destination_job_port_id,destination_type,position,created_at,modified_at) values (:context_id,:source_job_id,:source_job_port_id,:source_type::port_type,:destination_job_id,:destination_job_port_id,:destination_type::port_type,:position,:created_at,:modified_at)")
  public abstract void insertBatch(@BindLinkRecord Iterator<LinkRecord> records);

  @Override
  @SqlBatch("update link_record set context_id=:context_id,source_job_id=:source_job_id,source_job_port_id=:source_job_port_id,source_type=:source_type::port_type,destination_job_id=:destination_job_id,destination_job_port_id=:destination_job_port_id,destination_type=:destination_type::port_type,position=:position,modified_at='now' where context_id=:context_id and source_job_id=:source_job_id and source_job_port_id=:source_job_port_id and source_type=:source_type::port_type and destination_job_id=:destination_job_id and destination_job_port_id=:destination_job_port_id and destination_type=:destination_type::port_type")
  public abstract void updateBatch(@BindLinkRecord Iterator<LinkRecord> records);

  @Override
  @SqlBatch("delete from link_record where destination_job_id=:id and destination_type=:source_type::port_type and context_id=:root_id")
  public abstract void deleteByDestinationIdAndType(@Bind("id") String destinationId, @Bind("source_type") LinkPortType linkPortType, @Bind("root_id") UUID rootId);

  @Override
  @SqlUpdate("delete from link_record where context_id=:root_id")
  public abstract void deleteByRootId(@Bind("root_id") UUID rootId);

  @Override
  @SqlUpdate("delete from link_record where context_id=:root_id and (destination_job_id=:id or source_job_id=:id)")
  public abstract void delete(@Bind("id") String jobId, @Bind("root_id") UUID rootId);

  @Override
  @SqlQuery("select * from link_record where source_job_id=:source_job_id and source_job_port_id=:source_job_port_id and context_id=:context_id")
  public abstract List<LinkRecord> getBySource(@Bind("source_job_id") String sourceJobId, @Bind("source_job_port_id") String sourceJobPortId, @Bind("context_id") UUID rootId);

  @SqlQuery("select * from link_record where source_job_id=:source_job_id and context_id=:context_id")
  public abstract List<LinkRecord> getBySource(@Bind("source_job_id") String sourceJobId, @Bind("context_id") UUID rootId);

  @Override
  @SqlQuery("select * from link_record where source_job_id=:source_job_id and context_id=:context_id")
  public abstract List<LinkRecord> getBySourceJobId(@Bind("source_job_id") String sourceJobId, @Bind("context_id") UUID rootId);

  @Override
  @SqlQuery("select count(*) from link_record where source_job_id=:source_job_id and context_id=:context_id")
  public abstract int getBySourceCount(@Bind("source_job_id") String sourceJobId, @Bind("source_job_port_id") String sourceJobPortId, @Bind("context_id") UUID rootId);

  @Override
  @SqlQuery("select * from link_record where source_job_id=:source_job_id and source_type=:source_type::port_type and context_id=:context_id")
  public abstract List<LinkRecord> getBySourceAndSourceType(@Bind("source_job_id") String sourceJobId, @Bind("source_type") LinkPortType sourceType, @Bind("context_id") UUID rootId);

  @Override
  @SqlQuery("select * from link_record where source_job_id=:source_job_id and source_job_port_id=:source_job_port_id and destination_type=:destination_type::port_type and context_id=:context_id")
  public abstract List<LinkRecord> getBySourceAndDestinationType(@Bind("source_job_id") String sourceJobId, @Bind("source_job_port_id") String sourceJobPortId, @Bind("destination_type") LinkPortType destinationType, @Bind("context_id") UUID rootId);

  @Override
  @SqlQuery("select * from link_record where source_job_id=:source_job_id and source_job_port_id=:source_job_port_id and source_type=:source_type::port_type and context_id=:context_id")
  public abstract List<LinkRecord> getBySourceAndSourceType(@Bind("source_job_id") String jobId, @Bind("source_job_port_id") String portId, @Bind("source_type") LinkPortType varType, @Bind("context_id") UUID rootId);

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
            q.bind("created_at", Timestamp.valueOf(linkRecord.getCreatedAt()));
            q.bind("modified_at", Timestamp.valueOf(linkRecord.getModifiedAt()));
          }
        };
      }
    }
  }

  public static class LinkRecordMapper implements ResultSetMapper<LinkRecord> {
    public LinkRecord map(int index, ResultSet resultSet, StatementContext ctx) throws SQLException {
      UUID contextId = resultSet.getObject("context_id", UUID.class);
      String sourceJobId = resultSet.getString("source_job_id");
      String sourceJobPortId = resultSet.getString("source_job_port_id");
      String sourceType = resultSet.getString("source_type");
      String destinationJobId = resultSet.getString("destination_job_id");
      String destinationJobPortId = resultSet.getString("destination_job_port_id");
      String destinationType = resultSet.getString("destination_type");
      Integer position = resultSet.getInt("position");
      LocalDateTime createdAt = resultSet.getTimestamp("created_at").toLocalDateTime();
      LocalDateTime modifiedAt = resultSet.getTimestamp("modified_at").toLocalDateTime();
      return new LinkRecord(contextId, sourceJobId, sourceJobPortId, LinkPortType.valueOf(sourceType), destinationJobId, destinationJobPortId, LinkPortType.valueOf(destinationType), position, createdAt, modifiedAt);
    }
  }

}
