package org.rabix.engine.jdbi.impl;

import org.rabix.engine.repository.AppRepository;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface JDBIAppRepository extends AppRepository {

  @Override
  @SqlUpdate("INSERT INTO APPLICATION (HASH,APP) SELECT :hash,:app WHERE NOT EXISTS (SELECT HASH FROM APPLICATION WHERE HASH=:hash)")
  void insert(@Bind("hash") String hash, @Bind("app") String app);
  
  @Override
  @SqlQuery("SELECT APP FROM APPLICATION WHERE HASH=:hash;")
  String get(@Bind("hash") String hash);
  
}
