package org.rabix.engine.store.postgres.jdbi;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.apache.commons.configuration.Configuration;
import org.postgresql.jdbc3.Jdbc3PoolingDataSource;
import org.rabix.engine.store.repository.*;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.logging.SLF4JLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class JDBIRepositoryModule extends AbstractModule {

  private static final Logger logger = LoggerFactory.getLogger(JDBIRepositoryModule.class);

  private static final String DBINIT_SQL = "org/rabix/engine/jdbi/dbinit.sql";

  public JDBIRepositoryModule() {
  }

  @Override
  public void configure() {
  }

  @Singleton
  @Provides
  public DBI provideDBI(Configuration configuration) {
    Jdbc3PoolingDataSource source = new Jdbc3PoolingDataSource();
    source.setDataSourceName("Data Source");
    source.setServerName(configuration.getString("postgres.server"));
    source.setSsl(configuration.getBoolean("postgres.ssl", true));
    source.setPortNumber(configuration.getInt("postgres.port"));
    source.setDatabaseName(configuration.getString("postgres.database"));
    source.setUser(configuration.getString("postgres.user"));
    source.setPassword(configuration.getString("postgres.password"));
    source.setMaxConnections(configuration.getInt("postgres.pool_max_connections"));

    try {
      JdbcConnection dbcon = new JdbcConnection(source.getConnection());
      Liquibase lb = new Liquibase(DBINIT_SQL, new ClassLoaderResourceAccessor(), dbcon);
      lb.update(new Contexts());
    } catch (SQLException | LiquibaseException e) {
      logger.error(e.getMessage());
      System.exit(1);
    }

    DBI dbi = new DBI(source);
    dbi.setSQLLog(new SLF4JLog());
    return dbi;
  }

  @Singleton
  @Provides
  public JDBIRepositoryRegistry provideJDBIRepositoryRegistry(DBI dbi) {
    return dbi.onDemand(JDBIRepositoryRegistry.class);
  }

  @Provides
  @Singleton
  public AppRepository provideAppRepository(JDBIRepositoryRegistry repositoryRegistry) {
    return repositoryRegistry.applicationRepository();
  }

  @Provides
  @Singleton
  public BackendRepository provideBackendRepository(JDBIRepositoryRegistry repositoryRegistry) {
    return repositoryRegistry.backendRepository();
  }

  @Provides
  @Singleton
  public DAGRepository provideDAGRepository(JDBIRepositoryRegistry repositoryRegistry) {
    return repositoryRegistry.dagRepository();
  }

  @Provides
  @Singleton
  public JobRepository provideJobRepository(JDBIRepositoryRegistry repositoryRegistry) {
    return repositoryRegistry.jobRepository();
  }

  @Provides
  @Singleton
  public EventRepository provideEventRepository(JDBIRepositoryRegistry repositoryRegistry) {
    return repositoryRegistry.eventRepository();
  }

  @Provides
  @Singleton
  public JobRecordRepository provideJobRecordRepository(JDBIRepositoryRegistry repositoryRegistry) {
    return repositoryRegistry.jobRecordRepository();
  }

  @Provides
  @Singleton
  public LinkRecordRepository provideLinkRecordRepository(JDBIRepositoryRegistry repositoryRegistry) {
    return repositoryRegistry.linkRecordRepository();
  }

  @Provides
  @Singleton
  public VariableRecordRepository provideVariableRecordRepository(JDBIRepositoryRegistry repositoryRegistry) {
    return repositoryRegistry.variableRecordRepository();
  }

  @Provides
  @Singleton
  public ContextRecordRepository provideContextRecordRepository(JDBIRepositoryRegistry repositoryRegistry) {
    return repositoryRegistry.contextRecordRepository();
  }

  @Provides
  @Singleton
  public JobStatsRecordRepository provideJobStatsRecordRepository(JDBIRepositoryRegistry repositoryRegistry) {
    return repositoryRegistry.jobStatsRecordRepository();
  }

  @Provides
  @Singleton
  public IntermediaryFilesRepository provideIntermediaryFilesRepository(JDBIRepositoryRegistry repositoryRegistry) {
    return repositoryRegistry.intermediaryFilesRepository();
  }
}