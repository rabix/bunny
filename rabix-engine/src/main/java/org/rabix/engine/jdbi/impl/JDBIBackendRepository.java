package org.rabix.engine.jdbi.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import org.rabix.common.json.BeanSerializer;
import org.rabix.engine.jdbi.bindings.BindJson;
import org.rabix.engine.jdbi.impl.JDBIBackendRepository.BackendMapper;
import org.rabix.engine.repository.BackendRepository;
import org.rabix.transport.backend.Backend;
import org.rabix.transport.backend.Backend.BackendStatus;
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
  void insert(@Bind("id") UUID id, @BindBean Backend backend, @Bind("heartbeat_info") Timestamp heartbeatInfo);
  
  @Override
  @SqlUpdate("update backend set configuration=:configuration where id=:id")
  void update(@Bind("id") UUID id, @BindJson("configuration") Backend configuration);
  
  @Override
  @SqlQuery("select * from backend where id=:id")
  Backend get(@Bind("id") UUID id);
  
  @Override
  @SqlQuery("select * from backend where status=:status::backend_status")
  List<Backend> getByStatus(@Bind("status") BackendStatus status);
  
  @Override
  @SqlUpdate("update backend set status=:status::backend_status where id=:id")
  void updateStatus(@Bind("id") UUID id, @Bind("status") BackendStatus status);
  
  @Override
  @SqlUpdate("update backend set heartbeat_info=:heartbeat_info where id=:id")
  void updateHeartbeatInfo(@Bind("id") UUID id, @Bind("heartbeat_info") Timestamp heartbeatInfo);
  
  @Override
  @SqlQuery("select heartbeat_info from backend where id=:id")
  Timestamp getHeartbeatInfo(@Bind("id") UUID id);
  
  public static class BackendMapper implements ResultSetMapper<Backend> {
    public Backend map(int index, ResultSet r, StatementContext ctx) throws SQLException {
      Backend b = BeanSerializer.deserialize(r.getString("configuration"), Backend.class);
      b.setId(r.getObject("id", UUID.class));
      b.setName(r.getString("name"));
      b.setStatus(BackendStatus.valueOf(r.getString("status")));
      return b;
    }
  }
  
}