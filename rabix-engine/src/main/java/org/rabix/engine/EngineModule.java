package org.rabix.engine;

import org.rabix.engine.db.AppDB;
import org.rabix.engine.db.DAGNodeDB;
import org.rabix.engine.jdbi.JDBIRepositoryModule;
import org.rabix.engine.jdbi.JDBIRepositoryRegistry;
import org.rabix.engine.lru.dag.DAGCache;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.handler.HandlerFactory;
import org.rabix.engine.processor.handler.impl.ContextStatusEventHandler;
import org.rabix.engine.processor.handler.impl.InitEventHandler;
import org.rabix.engine.processor.handler.impl.InputEventHandler;
import org.rabix.engine.processor.handler.impl.JobStatusEventHandler;
import org.rabix.engine.processor.handler.impl.OutputEventHandler;
import org.rabix.engine.processor.handler.impl.ScatterHandler;
import org.rabix.engine.processor.impl.MultiEventProcessorImpl;
import org.rabix.engine.repository.TransactionHelper;
import org.rabix.engine.service.*;
import org.rabix.engine.service.impl.*;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class EngineModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new JDBIRepositoryModule());
    bind(CacheService.class).to(CacheServiceImpl.class).in(Scopes.SINGLETON);
    
    bind(DAGCache.class).in(Scopes.SINGLETON);
    bind(DAGNodeDB.class).in(Scopes.SINGLETON);
    bind(AppDB.class).in(Scopes.SINGLETON);
    bind(TransactionHelper.class).to(JDBIRepositoryRegistry.class).in(Scopes.SINGLETON);
    bind(JobRecordService.class).to(JobRecordServiceImpl.class).in(Scopes.SINGLETON);
    bind(VariableRecordService.class).to(VariableRecordServiceImpl.class).in(Scopes.SINGLETON);
    bind(LinkRecordService.class).to(LinkRecordServiceImpl.class).in(Scopes.SINGLETON);
    bind(ContextRecordService.class).to(ContextRecordServiceImpl.class).in(Scopes.SINGLETON);
    bind(JobStatsRecordService.class).to(JobStatsRecordServiceImpl.class).in(Scopes.SINGLETON);
    bind(StoreCleanupService.class).to(StoreCleanupServiceImpl.class).in(Scopes.SINGLETON);

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
