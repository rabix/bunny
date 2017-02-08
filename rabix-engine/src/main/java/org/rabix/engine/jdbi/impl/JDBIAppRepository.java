package org.rabix.engine.jdbi.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.rabix.bindings.model.Application;
import org.rabix.common.json.BeanSerializer;
import org.rabix.engine.jdbi.bindings.BindJson;
import org.rabix.engine.repository.AppRepository;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

public interface JDBIAppRepository extends AppRepository {

  @SqlUpdate("INSERT INTO APPLICATION (ID,APP) SELECT :id,:app WHERE NOT EXISTS (SELECT ID FROM APPLICATION WHERE ID=:id)")
  void insert(@Bind("id") UUID id, @BindJson("app") Application app);
  
  @SqlQuery("SELECT APP FROM APPLICATION WHERE ID=:id;")
  Application get(@Bind("id") UUID id);
  
  public static class ApplicationMapper implements ResultSetMapper<Application> {
    public Application map(int index, ResultSet r, StatementContext ctx) throws SQLException {
      return BeanSerializer.deserialize(r.getString("app"), Application.class);
    }
  }
}
