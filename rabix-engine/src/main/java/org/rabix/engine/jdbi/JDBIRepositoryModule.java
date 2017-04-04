package org.rabix.engine.jdbi;

import org.apache.commons.configuration.Configuration;
import org.postgresql.jdbc3.Jdbc3PoolingDataSource;
import org.rabix.engine.repository.AppRepository;
import org.rabix.engine.repository.BackendRepository;
import org.rabix.engine.repository.ContextRecordRepository;
import org.rabix.engine.repository.DAGRepository;
import org.rabix.engine.repository.EventRepository;
import org.rabix.engine.repository.JobRecordRepository;
import org.rabix.engine.repository.JobRepository;
import org.rabix.engine.repository.LinkRecordRepository;
import org.rabix.engine.repository.VariableRecordRepository;
import org.rabix.engine.repository.JobStatsRecordRepository;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.logging.SLF4JLog;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class JDBIRepositoryModule extends AbstractModule {

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
    source.setSsl(configuration.getBoolean("postgres.ssl", false));
    source.setPortNumber(configuration.getInt("postgres.port"));
    source.setDatabaseName(configuration.getString("postgres.database"));
    source.setUser(configuration.getString("postgres.user"));
    source.setPassword(configuration.getString("postgres.password"));
    source.setMaxConnections(configuration.getInt("postgres.pool_max_connections"));
    
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
  public AppRepository provideAppRepository(JDBIRepositoryRegistry repositoryRegistry) {
    return repositoryRegistry.applicationRepository();
  }
  
  @Provides
  public BackendRepository provideBackendRepository(JDBIRepositoryRegistry repositoryRegistry) {
    return repositoryRegistry.backendRepository();
  }
  
  @Provides
  public DAGRepository provideDAGRepository(JDBIRepositoryRegistry repositoryRegistry) {
    return repositoryRegistry.dagRepository();
  }
  
  @Provides
  public JobRepository provideJobRepository(JDBIRepositoryRegistry repositoryRegistry) {
    return repositoryRegistry.jobRepository();
  }

  @Provides
  public EventRepository provideEventRepository(JDBIRepositoryRegistry repositoryRegistry) {
    return repositoryRegistry.eventRepository();
  }
  
  @Provides
  public JobRecordRepository provideJobRecordRepository(JDBIRepositoryRegistry repositoryRegistry) {
    return repositoryRegistry.jobRecordRepository();
  }
  
  @Provides
  public LinkRecordRepository provideLinkRecordRepository(JDBIRepositoryRegistry repositoryRegistry) {
    return repositoryRegistry.linkRecordRepository();
  }
  
  @Provides
  public VariableRecordRepository provideVariableRecordRepository(JDBIRepositoryRegistry repositoryRegistry) {
    return repositoryRegistry.variableRecordRepository();
  }
  
  @Provides
  public ContextRecordRepository provideContextRecordRepository(JDBIRepositoryRegistry repositoryRegistry) {
    return repositoryRegistry.contextRecordRepository();
  }

  @Provides
  public JobStatsRecordRepository provideJobStatsRecordRepository(JDBIRepositoryRegistry repositoryRegistry) {
    return repositoryRegistry.jobStatsRecordRepository();
  }
}