package org.rabix.engine;

import org.rabix.engine.db.DAGNodeDB;
import org.rabix.engine.db.JobDB;
import org.rabix.engine.jdbi.JDBIRepositoryModule;
import org.rabix.engine.jdbi.JDBIRepositoryRegistry;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.handler.HandlerFactory;
import org.rabix.engine.processor.handler.impl.RootJobStatusEventHandler;
import org.rabix.engine.processor.handler.impl.InitEventHandler;
import org.rabix.engine.processor.handler.impl.InputEventHandler;
import org.rabix.engine.processor.handler.impl.JobStatusEventHandler;
import org.rabix.engine.processor.handler.impl.OutputEventHandler;
import org.rabix.engine.processor.handler.impl.ScatterHandler;
import org.rabix.engine.processor.impl.MultiEventProcessorImpl;
import org.rabix.engine.repository.TransactionHelper;
import org.rabix.engine.service.CacheService;
import org.rabix.engine.service.RootJobService;
import org.rabix.engine.service.JobRecordService;
import org.rabix.engine.service.LinkRecordService;
import org.rabix.engine.service.VariableRecordService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class EngineModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new JDBIRepositoryModule());
    
    bind(CacheService.class).in(Scopes.SINGLETON);
    
    bind(JobDB.class).in(Scopes.SINGLETON);
    bind(DAGNodeDB.class).in(Scopes.SINGLETON);
    bind(TransactionHelper.class).to(JDBIRepositoryRegistry.class).in(Scopes.SINGLETON);
    
    bind(JobRecordService.class).in(Scopes.SINGLETON);
    bind(VariableRecordService.class).in(Scopes.SINGLETON);
    bind(LinkRecordService.class).in(Scopes.SINGLETON);
    bind(RootJobService.class).in(Scopes.SINGLETON);

    bind(ScatterHandler.class).in(Scopes.SINGLETON);
    bind(InitEventHandler.class).in(Scopes.SINGLETON);
    bind(InputEventHandler.class).in(Scopes.SINGLETON);
    bind(OutputEventHandler.class).in(Scopes.SINGLETON);
    bind(JobStatusEventHandler.class).in(Scopes.SINGLETON);
    bind(RootJobStatusEventHandler.class).in(Scopes.SINGLETON);
    
    bind(HandlerFactory.class).in(Scopes.SINGLETON);
    bind(EventProcessor.class).to(MultiEventProcessorImpl.class).in(Scopes.SINGLETON);
  }
  
}
