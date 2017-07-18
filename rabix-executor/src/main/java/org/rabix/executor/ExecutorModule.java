package org.rabix.executor;

import org.apache.commons.configuration.Configuration;
import org.rabix.backend.api.BackendModule;
import org.rabix.backend.api.WorkerService;
import org.rabix.common.config.ConfigModule;
import org.rabix.common.retry.RetryInterceptorModule;
import org.rabix.executor.container.impl.DockerContainerHandler.DockerClientLockDecorator;
import org.rabix.executor.execution.JobHandlerCommandDispatcher;
import org.rabix.executor.handler.JobHandler;
import org.rabix.executor.handler.JobHandlerFactory;
import org.rabix.executor.handler.impl.JobHandlerImpl;
import org.rabix.executor.service.CacheService;
import org.rabix.executor.service.FilePermissionService;
import org.rabix.executor.service.JobDataService;
import org.rabix.executor.service.JobFitter;
import org.rabix.executor.service.impl.CacheServiceImpl;
import org.rabix.executor.service.impl.FilePermissionServiceImpl;
import org.rabix.executor.service.impl.JobDataServiceImpl;
import org.rabix.executor.service.impl.JobFitterImpl;
import org.rabix.executor.service.impl.MockWorkerServiceImpl;
import org.rabix.executor.service.impl.WorkerServiceImpl;
import org.rabix.executor.service.impl.WorkerServiceImpl.LocalWorker;

import com.google.inject.Scopes;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class ExecutorModule extends BackendModule {

  public ExecutorModule(ConfigModule configModule) {
    super(configModule);
  }

  @Override
  protected void configure() {
    install(configModule);
    install(new RetryInterceptorModule());
    install(new FactoryModuleBuilder().implement(JobHandler.class, JobHandlerImpl.class).build(JobHandlerFactory.class));

    Configuration configuration = configModule.provideConfig();
    
    String[] backendTypes = configuration.getStringArray("backend.embedded.types");
    for (String backendType : backendTypes) {
      if (backendType.trim().equalsIgnoreCase("LOCAL") || backendType.trim().equalsIgnoreCase("TES") ||
          backendType.trim().equalsIgnoreCase("LSF")) {
        install(new LocalStorageModule(configModule));
        break;
      }
    }
    
    boolean mockBackendEnabled = configuration.getBoolean("backend.mock.enabled", false);
    if (mockBackendEnabled) {
      bind(WorkerService.class).annotatedWith(LocalWorker.class).to(MockWorkerServiceImpl.class).in(Scopes.SINGLETON);
    } else {
      bind(WorkerService.class).annotatedWith(LocalWorker.class).to(WorkerServiceImpl.class).in(Scopes.SINGLETON);
    }
    
    bind(DockerClientLockDecorator.class).in(Scopes.SINGLETON);

    bind(JobFitter.class).to(JobFitterImpl.class).in(Scopes.SINGLETON);
    bind(JobDataService.class).to(JobDataServiceImpl.class).in(Scopes.SINGLETON);
    bind(JobHandlerCommandDispatcher.class).in(Scopes.SINGLETON);

    bind(FilePermissionService.class).to(FilePermissionServiceImpl.class).in(Scopes.SINGLETON);
    bind(CacheService.class).to(CacheServiceImpl.class).in(Scopes.SINGLETON);
  }

}
