package org.rabix.storage.postgres.jdbi.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.rabix.common.helper.JSONHelper;
import org.rabix.common.json.BeanSerializer;
import org.rabix.storage.model.BackendRecord;
import org.rabix.storage.postgres.jdbi.bindings.BindJson;
import org.rabix.storage.postgres.jdbi.impl.JDBIBackendRepository.BackendMapper;
import org.rabix.storage.repository.BackendRepository;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

@RegisterMapper(BackendMapper.class)
public interface JDBIBackendRepository extends BackendRepository {

  @Override
  @SqlUpdate("insert into backend (id,name,type,configuration,heartbeat_info,status) values (:id,:name,:type::backend_type,:configuration::jsonb,:heartbeat_info,:status::backend_status)")
  void insert(@BindBean BackendRecord backend);
  
  @Override
  @SqlUpdate("update backend set configuration=:configuration where id=:id")
  void updateConfiguration(@Bind("id") UUID id, @BindJson("configuration") Map<String, ?> configuration);
  
  @Override
  @SqlQuery("select * from backend where id=:id")
  BackendRecord get(@Bind("id") UUID id);
  
  @Override
  @SqlQuery("select * from backend")
  List<BackendRecord> getAll();
  
  @Override
  @SqlQuery("select * from backend where status=:status::backend_status")
  List<BackendRecord> getByStatus(@Bind("status") BackendRecord.Status status);
  
  @Override
  @SqlUpdate("update backend set status=:status::backend_status where id=:id")
  void updateStatus(@Bind("id") UUID id, @Bind("status") BackendRecord.Status status);
  
  @Override
  @SqlUpdate("update backend set heartbeat_info=:heartbeat_info where id=:id")
  void updateHeartbeatInfo(@Bind("id") UUID id, @Bind("heartbeat_info") Instant heartbeatInfo);
  
  @Override
  @SqlQuery("select heartbeat_info from backend where id=:id")
  Instant getHeartbeatInfo(@Bind("id") UUID id);
  
  public static class BackendMapper implements ResultSetMapper<BackendRecord> {
    public BackendRecord map(int index, ResultSet r, StatementContext ctx) throws SQLException {
      Map<String, ?> configuration = JSONHelper.readMap(r.getString("configuration"));
      UUID id = r.getObject("id", UUID.class);
      String name = r.getString("name");
      BackendRecord.Status status = BackendRecord.Status.valueOf(r.getString("status"));
      Instant heartbit = r.getObject("heartbit_info", Instant.class);
      return new BackendRecord(id, name, heartbit, configuration, status);
    }
  }
  
}