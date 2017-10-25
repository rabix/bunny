package org.rabix.engine;

import org.apache.commons.configuration.Configuration;
import org.rabix.common.config.ConfigModule;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.handler.HandlerFactory;
import org.rabix.engine.processor.handler.impl.ContextStatusEventHandler;
import org.rabix.engine.processor.handler.impl.InitEventHandler;
import org.rabix.engine.processor.handler.impl.InputEventHandler;
import org.rabix.engine.processor.handler.impl.JobStatusEventHandler;
import org.rabix.engine.processor.handler.impl.OutputEventHandler;
import org.rabix.engine.processor.handler.impl.ScatterHandler;
import org.rabix.engine.processor.impl.MultiEventProcessorImpl;
import org.rabix.engine.service.AppService;
import org.rabix.engine.service.CacheService;
import org.rabix.engine.service.ContextRecordService;
import org.rabix.engine.service.DAGNodeService;
import org.rabix.engine.service.IntermediaryFilesHandler;
import org.rabix.engine.service.IntermediaryFilesService;
import org.rabix.engine.service.JobRecordService;
import org.rabix.engine.service.JobStatsRecordService;
import org.rabix.engine.service.LinkRecordService;
import org.rabix.engine.service.StoreCleanupService;
import org.rabix.engine.service.VariableRecordService;
import org.rabix.engine.service.impl.AppServiceImpl;
import org.rabix.engine.service.impl.CacheServiceImpl;
import org.rabix.engine.service.impl.ContextRecordServiceImpl;
import org.rabix.engine.service.impl.DAGNodeServiceImpl;
import org.rabix.engine.service.impl.IntermediaryFilesLocalHandler;
import org.rabix.engine.service.impl.IntermediaryFilesServiceImpl;
import org.rabix.engine.service.impl.JobRecordServiceImpl;
import org.rabix.engine.service.impl.JobStatsRecordServiceImpl;
import org.rabix.engine.service.impl.LinkRecordServiceImpl;
import org.rabix.engine.service.impl.NoOpIntermediaryFilesServiceHandler;
import org.rabix.engine.service.impl.StoreCleanupServiceImpl;
import org.rabix.engine.service.impl.VariableRecordServiceImpl;
import org.rabix.engine.store.lru.dag.DAGCache;
import org.rabix.engine.store.memory.InMemoryRepositoryModule;
import org.rabix.engine.store.memory.InMemoryRepositoryRegistry;
import org.rabix.engine.store.postgres.jdbi.JDBIRepositoryModule;
import org.rabix.engine.store.postgres.jdbi.JDBIRepositoryRegistry;
import org.rabix.engine.store.repository.TransactionHelper;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class EngineModule extends AbstractModule {
  
  ConfigModule config;
  
  public EngineModule(ConfigModule configModule) {
    config = configModule;
  }

  @Override
  protected void configure() {
    Configuration configuration = this.config.provideConfig();
    String persistence = configuration.getString("engine.store", "IN_MEMORY");
    
    if (persistence.equals("POSTGRES")) {
      install(new JDBIRepositoryModule());
      bind(TransactionHelper.class).to(JDBIRepositoryRegistry.class).in(Scopes.SINGLETON);
    } else if(persistence.equals("IN_MEMORY")) {
      install(new InMemoryRepositoryModule());
      bind(TransactionHelper.class).to(InMemoryRepositoryRegistry.class).in(Scopes.SINGLETON);
    }

    if (configuration.getBoolean("engine.delete_intermediary_files", false)) {
      bind(IntermediaryFilesHandler.class).to(IntermediaryFilesLocalHandler.class).in(Scopes.SINGLETON);
    } else {
      bind(IntermediaryFilesHandler.class).to(NoOpIntermediaryFilesServiceHandler.class).in(Scopes.SINGLETON);
    }

    bind(IntermediaryFilesService.class).to(IntermediaryFilesServiceImpl.class).in(Scopes.SINGLETON);       
    
    bind(CacheService.class).to(CacheServiceImpl.class).in(Scopes.SINGLETON);
    
    bind(DAGCache.class).in(Scopes.SINGLETON);
    bind(DAGNodeService.class).to(DAGNodeServiceImpl.class).in(Scopes.SINGLETON);
    bind(AppService.class).to(AppServiceImpl.class).in(Scopes.SINGLETON);
    bind(JobRecordService.class).to(JobRecordServiceImpl.class).in(Scopes.SINGLETON);
    bind(VariableRecordService.class).to(VariableRecordServiceImpl.class).in(Scopes.SINGLETON);
    bind(LinkRecordService.class).to(LinkRecordServiceImpl.class).in(Scopes.SINGLETON);
    bind(ContextRecordService.class).to(ContextRecordServiceImpl.class).in(Scopes.SINGLETON);
    bind(JobStatsRecordService.class).to(JobStatsRecordServiceImpl.class).in(Scopes.SINGLETON);
    bind(StoreCleanupService.class).to(StoreCleanupServiceImpl.class).in(Scopes.SINGLETON);

    bind(JobHelper.class).in(Scopes.SINGLETON);
    bind(ScatterHandler.class).in(Scopes.SINGLETON);
    bind(InitEventHandler.class).in(Scopes.SINGLETON);
    bind(InputEventHandler.class).in(Scopes.SINGLETON);
    bind(OutputEventHandler.class).in(Scopes.SINGLETON);
    bind(JobStatusEventHandler.class).in(Scopes.SINGLETON);
    bind(ContextStatusEventHandler.class).in(Scopes.SINGLETON);
    
    bind(HandlerFactory.class).in(Scopes.SINGLETON);
    bind(EventProcessor.class).to(MultiEventProcessorImpl.class).in(Scopes.SINGLETON);
  }

}
