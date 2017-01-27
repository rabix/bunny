package org.rabix.engine;

import org.apache.commons.configuration.Configuration;
import org.postgresql.jdbc3.Jdbc3PoolingDataSource;
import org.rabix.engine.db.BackendDB;
import org.rabix.engine.db.DAGNodeDB;
import org.rabix.engine.db.JobDB;
import org.rabix.engine.db.ReadyJobGroupsDB;
import org.rabix.engine.jdbi.JDBIRepositoryRegistry;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.dispatcher.EventDispatcherFactory;
import org.rabix.engine.processor.handler.HandlerFactory;
import org.rabix.engine.processor.handler.impl.ContextStatusEventHandler;
import org.rabix.engine.processor.handler.impl.InitEventHandler;
import org.rabix.engine.processor.handler.impl.InputEventHandler;
import org.rabix.engine.processor.handler.impl.JobStatusEventHandler;
import org.rabix.engine.processor.handler.impl.OutputEventHandler;
import org.rabix.engine.processor.handler.impl.ScatterHandler;
import org.rabix.engine.processor.impl.MultiEventProcessorImpl;
import org.rabix.engine.repository.AppRepository;
import org.rabix.engine.repository.BackendRepository;
import org.rabix.engine.repository.ContextRecordRepository;
import org.rabix.engine.repository.DAGRepository;
import org.rabix.engine.repository.JobBackendRepository;
import org.rabix.engine.repository.JobRecordRepository;
import org.rabix.engine.repository.JobRepository;
import org.rabix.engine.repository.LinkRecordRepository;
import org.rabix.engine.repository.VariableRecordRepository;
import org.rabix.engine.service.ContextRecordService;
import org.rabix.engine.service.JobRecordService;
import org.rabix.engine.service.LinkRecordService;
import org.rabix.engine.service.VariableRecordService;
import org.skife.jdbi.v2.DBI;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;

public class EngineModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(JDBIRepositoryModule.class).in(Scopes.SINGLETON);
    
    bind(JobDB.class).in(Scopes.SINGLETON);
    bind(BackendDB.class).in(Scopes.SINGLETON);
    bind(DAGNodeDB.class).in(Scopes.SINGLETON);
    bind(ReadyJobGroupsDB.class).in(Scopes.SINGLETON);
    
    bind(JobRecordService.class).in(Scopes.SINGLETON);
    bind(VariableRecordService.class).in(Scopes.SINGLETON);
    bind(LinkRecordService.class).in(Scopes.SINGLETON);
    bind(ContextRecordService.class).in(Scopes.SINGLETON);

    bind(ScatterHandler.class).in(Scopes.SINGLETON);
    bind(InitEventHandler.class).in(Scopes.SINGLETON);
    bind(InputEventHandler.class).in(Scopes.SINGLETON);
    bind(OutputEventHandler.class).in(Scopes.SINGLETON);
    bind(JobStatusEventHandler.class).in(Scopes.SINGLETON);
    bind(ContextStatusEventHandler.class).in(Scopes.SINGLETON);
    
    bind(HandlerFactory.class).in(Scopes.SINGLETON);
    bind(EventDispatcherFactory.class).in(Scopes.SINGLETON);
    bind(EventProcessor.class).to(MultiEventProcessorImpl.class).in(Scopes.SINGLETON);
    
    install(new JDBIRepositoryModule());
  }
  
  public static class JDBIRepositoryModule extends AbstractModule {

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
      source.setPortNumber(configuration.getInt("postgres.port"));
      source.setDatabaseName(configuration.getString("postgres.database"));
      source.setUser(configuration.getString("postgres.user"));
      source.setPassword(configuration.getString("postgres.password"));
      source.setMaxConnections(configuration.getInt("postgres.pool_max_connections"));
      return new DBI(source);
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
    public JobBackendRepository provideJobBackendRepository(JDBIRepositoryRegistry repositoryRegistry) {
      return repositoryRegistry.jobBackendRepository();
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
  }


}
