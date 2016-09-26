package org.rabix.executor;

import org.rabix.common.config.ConfigModule;
import org.rabix.common.retry.RetryInterceptorModule;
import org.rabix.executor.container.impl.DockerContainerHandler.DockerClientLockDecorator;
import org.rabix.executor.execution.JobHandlerCommandDispatcher;
import org.rabix.executor.handler.JobHandler;
import org.rabix.executor.handler.JobHandlerFactory;
import org.rabix.executor.handler.impl.JobHandlerImpl;
import org.rabix.executor.service.ResultCacheService;
import org.rabix.executor.service.ExecutorService;
import org.rabix.executor.service.FilePermissionService;
import org.rabix.executor.service.FileService;
import org.rabix.executor.service.JobDataService;
import org.rabix.executor.service.JobFitter;
import org.rabix.executor.service.impl.ResultCacheServiceImpl;
import org.rabix.executor.service.impl.ExecutorServiceImpl;
import org.rabix.executor.service.impl.FilePermissionServiceImpl;
import org.rabix.executor.service.impl.FileServiceImpl;
import org.rabix.executor.service.impl.JobDataServiceImpl;
import org.rabix.executor.service.impl.JobFitterImpl;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
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
    bind(ExecutorService.class).to(ExecutorServiceImpl.class).in(Scopes.SINGLETON);
    bind(FilePermissionService.class).to(FilePermissionServiceImpl.class).in(Scopes.SINGLETON);
    bind(ResultCacheService.class).to(ResultCacheServiceImpl.class).in(Scopes.SINGLETON);
  }

}
