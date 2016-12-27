package org.rabix.engine.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.rabix.bindings.model.Application;
import org.rabix.common.json.BeanSerializer;
import org.rabix.engine.dao.bindings.BindJson;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

public interface AppRepository {

  @SqlUpdate("INSERT INTO APPLICATION (ID,APP) SELECT :id,:app WHERE NOT EXISTS (SELECT ID FROM APPLICATION WHERE ID=:id)")
  void insert(@Bind("id") String id, @BindJson("app") String app);
  
  @SqlQuery("SELECT APP FROM APPLICATION WHERE ID=:id;")
  Application get(@Bind("id") String id);
  
  public static class ApplicationMapper implements ResultSetMapper<Application> {
    public Application map(int index, ResultSet r, StatementContext ctx) throws SQLException {
      return BeanSerializer.deserialize(r.getString("app"), Application.class);
    }
  }
}
