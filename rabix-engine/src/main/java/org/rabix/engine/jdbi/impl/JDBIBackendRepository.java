package org.rabix.engine.jdbi.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.rabix.common.json.BeanSerializer;
import org.rabix.engine.jdbi.bindings.BindJson;
import org.rabix.engine.jdbi.impl.JDBIBackendRepository.BackendMapper;
import org.rabix.engine.repository.BackendRepository;
import org.rabix.transport.backend.Backend;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

@RegisterMapper(BackendMapper.class)
public interface JDBIBackendRepository extends BackendRepository {

  @SqlUpdate("insert into backend (id,configuration) values (:id,:configuration)")
  void insert(@Bind("id") String id, @BindJson("configuration") Backend backend);
  
  @SqlUpdate("update backend set configuration=:configuration where id=:id")
  void update(@Bind("id") String id, @BindJson("configuration") Backend configuration);
  
  @SqlQuery("select * from backend where id=:id")
  Backend get(@Bind("id") String id);
  
  public static class BackendMapper implements ResultSetMapper<Backend> {
    public Backend map(int index, ResultSet r, StatementContext ctx) throws SQLException {
      return BeanSerializer.deserialize(r.getString("configuration"), Backend.class);
    }
  }
  
}