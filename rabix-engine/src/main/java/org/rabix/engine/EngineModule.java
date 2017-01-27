package org.rabix.engine;

import org.postgresql.jdbc3.Jdbc3PoolingDataSource;
import org.rabix.engine.db.BackendDB;
import org.rabix.engine.db.DAGNodeDB;
import org.rabix.engine.db.JobDB;
import org.rabix.engine.db.ReadyJobGroupsDB;
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
import org.rabix.engine.service.ContextRecordService;
import org.rabix.engine.service.JobRecordService;
import org.rabix.engine.service.LinkRecordService;
import org.rabix.engine.service.VariableRecordService;
import org.skife.jdbi.v2.DBI;

import com.github.mlk.guice.JdbiModule;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class EngineModule extends AbstractModule {

  @Override
  protected void configure() {
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
    
    install(JdbiModule.builder().scan("org.rabix.engine.dao").build());
    Jdbc3PoolingDataSource source = new Jdbc3PoolingDataSource();
    source.setDataSourceName("Data Source");
    source.setServerName("localhost");
    source.setPortNumber(5433);
    source.setDatabaseName("bunny");
    source.setUser("postgres");
    source.setPassword("postgres");
    source.setMaxConnections(10);

    bind(DBI.class).toInstance(new DBI(source));
  }

}
