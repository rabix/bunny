package org.rabix.engine.rest;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.configuration.Configuration;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.rabix.common.config.ConfigModule;
import org.rabix.engine.EngineModule;
import org.rabix.engine.rest.api.BackendHTTPService;
import org.rabix.engine.rest.api.JobHTTPService;
import org.rabix.engine.rest.api.impl.BackendHTTPServiceImpl;
import org.rabix.engine.rest.api.impl.JobHTTPServiceImpl;
import org.rabix.engine.service.BackendService;
import org.rabix.engine.service.BootstrapService;
import org.rabix.engine.service.BootstrapServiceException;
import org.rabix.engine.service.IntermediaryFilesHandler;
import org.rabix.engine.service.IntermediaryFilesService;
import org.rabix.engine.service.JobService;
import org.rabix.engine.service.SchedulerService;
import org.rabix.engine.service.SchedulerService.SchedulerCallback;
import org.rabix.engine.service.impl.BackendServiceImpl;
import org.rabix.engine.service.impl.BootstrapServiceImpl;
import org.rabix.engine.service.impl.IntermediaryFilesServiceImpl;
import org.rabix.engine.service.impl.JobServiceImpl;
import org.rabix.engine.service.impl.NoOpIntermediaryFilesServiceHandler;
import org.rabix.engine.service.impl.SchedulerServiceImpl;
import org.rabix.engine.status.EngineStatusCallback;
import org.rabix.engine.status.impl.DefaultEngineStatusCallback;
import org.rabix.engine.stub.BackendStubFactory;
import org.rabix.engine.stub.impl.BackendStubFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.ServletModule;
import com.squarespace.jersey2.guice.BootstrapUtils;

public class ServerBuilder {

  private final static Logger logger = LoggerFactory.getLogger(ServerBuilder.class);
  
  private final static String ENGINE_PORT_KEY = "engine.port";

  private File configDir;

  public ServerBuilder(File configDir) {
    this.configDir = configDir;
  }

  public Server build() {
    ServiceLocator locator = BootstrapUtils.newServiceLocator();
    ConfigModule configModule = new ConfigModule(configDir, null);
    Injector injector = BootstrapUtils.newInjector(locator,
        Arrays.asList(new ServletModule(), new EngineModule(configModule), configModule, new AbstractModule() {
          @Override
          protected void configure() {
            bind(JobService.class).to(JobServiceImpl.class).in(Scopes.SINGLETON);
            bind(EngineStatusCallback.class).to(DefaultEngineStatusCallback.class).in(Scopes.SINGLETON);
            bind(SchedulerCallback.class).to(SchedulerServiceImpl.class).in(Scopes.SINGLETON);
            bind(BootstrapService.class).to(BootstrapServiceImpl.class).in(Scopes.SINGLETON);
            bind(BackendService.class).to(BackendServiceImpl.class).in(Scopes.SINGLETON);
            bind(BackendStubFactory.class).to(BackendStubFactoryImpl.class).in(Scopes.SINGLETON);
            bind(SchedulerService.class).to(SchedulerServiceImpl.class).in(Scopes.SINGLETON);
            bind(JobHTTPService.class).to(JobHTTPServiceImpl.class);
            bind(IntermediaryFilesService.class).to(IntermediaryFilesServiceImpl.class).in(Scopes.SINGLETON);
            bind(IntermediaryFilesHandler.class).to(NoOpIntermediaryFilesServiceHandler.class).in(Scopes.SINGLETON);
            bind(BackendHTTPService.class).to(BackendHTTPServiceImpl.class).in(Scopes.SINGLETON);
            bind(SchedulerCallback.class).to(SchedulerServiceImpl.class).in(Scopes.SINGLETON);
          }
        }));
    BootstrapUtils.install(locator);

    Configuration configuration = injector.getInstance(Configuration.class);

    SchedulerService schedulerService = injector.getInstance(SchedulerService.class);
    schedulerService.start();
    
    BootstrapService eventService = injector.getInstance(BootstrapService.class);
    try {
      eventService.replay();
    } catch (BootstrapServiceException e) {
      logger.error("Failed to bootstrap engine", e);
      System.exit(-1);
    }
    
    int enginePort = configuration.getInt(ENGINE_PORT_KEY);
    Server server = new Server(enginePort);

    ResourceConfig config = ResourceConfig.forApplication(new Application());
    config.register(CORSResponseFilter.class);
    
    ServletContainer servletContainer = new ServletContainer(config);
    
    ServletHolder sh = new ServletHolder(servletContainer);
    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    context.setContextPath("/");

    FilterHolder filterHolder = new FilterHolder(GuiceFilter.class);
    context.addFilter(filterHolder, "/*", EnumSet.allOf(DispatcherType.class));

    context.addServlet(sh, "/*");

    ResourceHandler resourceHandler = new ResourceHandler();
    resourceHandler.setDirectoriesListed(true);
    resourceHandler.setWelcomeFiles(new String[] { "index.html" });
    resourceHandler.setResourceBase("./web");

    HandlerList handlers = new HandlerList();
    handlers.setHandlers(new Handler[] { resourceHandler, context });
    server.setHandler(handlers);

    server.setHandler(handlers);
    return server;
  }

  public static class CORSResponseFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
      MultivaluedMap<String, Object> headers = responseContext.getHeaders();

      headers.add("Access-Control-Allow-Origin", "*");
      headers.add("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");
      headers.add("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-Codingpedia");
    }

  }

  @ApplicationPath("/")
  public class Application extends ResourceConfig {

    public Application() {
      packages("org.rabix.engine.rest.api");
    }
  }

}
