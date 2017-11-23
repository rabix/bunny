package org.rabix.engine;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import org.apache.commons.configuration.Configuration;
import org.rabix.common.config.ConfigModule;
import org.rabix.engine.metrics.MetricsModule;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.handler.HandlerFactory;
import org.rabix.engine.processor.handler.impl.*;
import org.rabix.engine.processor.impl.MultiEventProcessorImpl;
import org.rabix.engine.service.*;
import org.rabix.engine.service.impl.*;
import org.rabix.engine.store.event.sourcing.EventSourcingModule;
import org.rabix.engine.store.lru.dag.DAGCache;
import org.rabix.engine.store.memory.InMemoryRepositoryModule;
import org.rabix.engine.store.memory.InMemoryRepositoryRegistry;
import org.rabix.engine.store.postgres.jdbi.JDBIRepositoryModule;
import org.rabix.engine.store.postgres.jdbi.JDBIRepositoryRegistry;
import org.rabix.engine.store.repository.TransactionHelper;

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
    } else if (persistence.equals("EVENT_SOURCING")) {
      install(new EventSourcingModule(new InMemoryRepositoryModule(), new JDBIRepositoryModule()));
      bind(TransactionHelper.class).to(InMemoryRepositoryRegistry.class).in(Scopes.SINGLETON);
    }
    bind(IntermediaryFilesService.class).to(IntermediaryFilesServiceImpl.class).in(Scopes.SINGLETON);

    bind(DAGCache.class).in(Scopes.SINGLETON);
    bind(DAGNodeService.class).to(DAGNodeServiceImpl.class).in(Scopes.SINGLETON);
    bind(AppService.class).to(AppServiceImpl.class).in(Scopes.SINGLETON);
    bind(JobRecordService.class).to(JobRecordServiceImpl.class).in(Scopes.SINGLETON);
    bind(VariableRecordService.class).to(VariableRecordServiceImpl.class).in(Scopes.SINGLETON);
    bind(LinkRecordService.class).to(LinkRecordServiceImpl.class).in(Scopes.SINGLETON);
    bind(ContextRecordService.class).to(ContextRecordServiceImpl.class).in(Scopes.SINGLETON);
    bind(JobStatsRecordService.class).to(JobStatsRecordServiceImpl.class).in(Scopes.SINGLETON);
    bind(GarbageCollectionService.class).to(GarbageCollectionServiceImpl.class).in(Scopes.SINGLETON);

    bind(JobHelper.class).in(Scopes.SINGLETON);
    bind(ScatterHandler.class).in(Scopes.SINGLETON);
    bind(InitEventHandler.class).in(Scopes.SINGLETON);
    bind(InputEventHandler.class).in(Scopes.SINGLETON);
    bind(OutputEventHandler.class).in(Scopes.SINGLETON);
    bind(JobStatusEventHandler.class).in(Scopes.SINGLETON);
    bind(ContextStatusEventHandler.class).in(Scopes.SINGLETON);

    bind(HandlerFactory.class).in(Scopes.SINGLETON);
    bind(EventProcessor.class).to(MultiEventProcessorImpl.class).in(Scopes.SINGLETON);

    install(new MetricsModule(configuration));
  }

}
