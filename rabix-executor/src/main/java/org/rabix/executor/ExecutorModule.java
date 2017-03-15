package org.rabix.executor;

import org.apache.commons.configuration.Configuration;
import org.rabix.common.config.ConfigModule;
import org.rabix.common.retry.RetryInterceptorModule;
import org.rabix.executor.container.impl.DockerContainerHandler.DockerClientLockDecorator;
import org.rabix.executor.execution.JobHandlerCommandDispatcher;
import org.rabix.executor.handler.JobHandler;
import org.rabix.executor.handler.JobHandlerFactory;
import org.rabix.executor.handler.impl.JobHandlerImpl;
import org.rabix.executor.service.CacheService;
import org.rabix.executor.service.ExecutorService;
import org.rabix.executor.service.FilePermissionService;
import org.rabix.executor.service.FileService;
import org.rabix.executor.service.JobDataService;
import org.rabix.executor.service.JobFitter;
import org.rabix.executor.service.impl.CacheServiceImpl;
import org.rabix.executor.service.impl.ExecutorServiceImpl;
import org.rabix.executor.service.impl.FilePermissionServiceImpl;
import org.rabix.executor.service.impl.FileServiceImpl;
import org.rabix.executor.service.impl.JobDataServiceImpl;
import org.rabix.executor.service.impl.JobFitterImpl;
import org.rabix.executor.service.impl.MockExecutorServiceImpl;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class ExecutorModule extends AbstractModule {

  private final ConfigModule configModule;

  public ExecutorModule(ConfigModule configModule) {
    this.configModule = configModule;
  }

  @Override
  protected void configure() {
    install(configModule);
    install(new RetryInterceptorModule());
    install(new FactoryModuleBuilder().implement(JobHandler.class, JobHandlerImpl.class).build(JobHandlerFactory.class));

    bind(DockerClientLockDecorator.class).in(Scopes.SINGLETON);

    bind(JobFitter.class).to(JobFitterImpl.class).in(Scopes.SINGLETON);
    bind(JobDataService.class).to(JobDataServiceImpl.class).in(Scopes.SINGLETON);
    bind(JobHandlerCommandDispatcher.class).in(Scopes.SINGLETON);

    bind(FileService.class).to(FileServiceImpl.class).in(Scopes.SINGLETON);
    bind(FilePermissionService.class).to(FilePermissionServiceImpl.class).in(Scopes.SINGLETON);
    bind(CacheService.class).to(CacheServiceImpl.class).in(Scopes.SINGLETON);
  }

  @Provides
  @Singleton
  public ExecutorService provideExecutorService(Injector injector, Configuration configuration) {
    boolean mockBackendEnabled = configuration.getBoolean("mock_backend.enabled", false);
    if (mockBackendEnabled) {
      return injector.getInstance(MockExecutorServiceImpl.class);
    } else {
      return injector.getInstance(ExecutorServiceImpl.class);
    }
  }
  
}
