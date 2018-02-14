package org.rabix.engine.store.postgres.jdbi.impl;

import org.rabix.engine.store.postgres.jdbi.impl.JDBIIntermediaryFilesRepository.IntermediaryFileEntityMapper;
import org.rabix.engine.store.repository.IntermediaryFilesRepository;
import org.skife.jdbi.v2.SQLStatement;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import org.skife.jdbi.v2.unstable.BindIn;

import java.lang.annotation.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.UUID;


@RegisterMapper(IntermediaryFileEntityMapper.class)
@UseStringTemplate3StatementLocator
public interface JDBIIntermediaryFilesRepository extends IntermediaryFilesRepository {

  @SqlUpdate("insert into intermediary_files (root_id,filename,count) values (:root_id,:filename,:count)")
  void insert(@Bind("root_id") UUID root_id, @Bind("filename") String filename, @Bind("count") Integer count);

  @SqlUpdate("insert into intermediary_files (root_id,filename,count) values (:root_id,:filename,:count) on conflict(root_id, filename) do nothing")
  void insertIfNotExists(@Bind("root_id") UUID root_id, @Bind("filename") String filename, @Bind("count") Integer count);

  @SqlUpdate("update intermediary_files set count=:count where root_id=:root_id and filename=:filename")
  void update(@Bind("root_id") UUID root_id, @Bind("filename") String filename, @Bind("count") Integer count);

  @SqlUpdate("insert into intermediary_files (root_id,filename,count) values (:root_id,:filename,0) on conflict (root_id, filename) do update set count=intermediary_files.count-1")
  void decrement(@Bind("root_id") UUID root_id, @Bind("filename") String filename);
  @Override
  @SqlUpdate("insert into intermediary_files (root_id,filename,count) values (:root_id,:filename,1) on conflict (root_id, filename) do update set count=intermediary_files.count+1")
  void increment(@Bind("root_id") UUID rootId, @Bind("filename") String filename);

  @SqlUpdate("delete from intermediary_files where root_id=:root_id and filename=:filename")
  void delete(@Bind("root_id") UUID rootId, @Bind("filename") String filename);

  @SqlUpdate("delete from intermediary_files where root_id=:root_id")
  void delete(@Bind("root_id") UUID root_id);

  @Override
  @SqlUpdate("delete from intermediary_files where root_id in (<ids>)")
  void deleteByRootIds(@BindIn("ids") Set<UUID> rootIds);

  @Override
  @SqlQuery("select * from intermediary_files where root_id=:root_id")
  List<IntermediaryFileEntity> get(@Bind("root_id") UUID root_id);

  @BindingAnnotation(BindIntermediaryFileEntity.IntermediaryFileEntityBinderFactory.class)
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ ElementType.PARAMETER })
  public static @interface BindIntermediaryFileEntity {
    public static class IntermediaryFileEntityBinderFactory implements BinderFactory<Annotation> {
      public Binder<BindIntermediaryFileEntity, IntermediaryFileEntity> build(Annotation annotation) {
        return new Binder<BindIntermediaryFileEntity, IntermediaryFileEntity>() {
          public void bind(SQLStatement<?> q, BindIntermediaryFileEntity bind, IntermediaryFileEntity intermediaryFile) {
            q.bind("root_id", intermediaryFile.getRootId());
            q.bind("filename", intermediaryFile.getFilename());
            q.bind("count", intermediaryFile.getCount());}
        };
      }
    }
  }

  public static class IntermediaryFileEntityMapper implements ResultSetMapper<IntermediaryFileEntity> {
    public IntermediaryFileEntity map(int index, ResultSet resultSet, StatementContext ctx) throws SQLException {
      UUID rootId = resultSet.getObject("root_id", UUID.class);
      String filename = resultSet.getString("filename");
      Integer count = resultSet.getInt("count");
      return new IntermediaryFileEntity(rootId, filename, count);
    }
  }

}
