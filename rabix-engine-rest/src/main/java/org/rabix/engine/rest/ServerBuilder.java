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
import org.rabix.engine.rest.backend.stub.BackendStubFactory;
import org.rabix.engine.rest.service.BackendService;
import org.rabix.engine.rest.service.IntermediaryFilesService;
import org.rabix.engine.rest.service.JobService;
import org.rabix.engine.rest.service.SchedulerService;
import org.rabix.engine.rest.service.impl.BackendServiceImpl;
import org.rabix.engine.rest.service.impl.JobServiceImpl;
import org.rabix.engine.rest.service.impl.NoOpIntermediaryFilesServiceImpl;
import org.rabix.engine.rest.service.impl.SchedulerServiceImpl;
import org.rabix.transport.backend.BackendPopulator;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.ServletModule;
import com.squarespace.jersey2.guice.BootstrapUtils;

public class ServerBuilder {

  private final static String ENGINE_PORT_KEY = "engine.port";

  private File configDir;

  public ServerBuilder(File configDir) {
    this.configDir = configDir;
  }

  public Server build() {
    ServiceLocator locator = BootstrapUtils.newServiceLocator();

    Injector injector = BootstrapUtils.newInjector(locator,
        Arrays.asList(new ServletModule(), new ConfigModule(configDir, null), new EngineModule(), new AbstractModule() {
          @Override
          protected void configure() {
            bind(JobService.class).to(JobServiceImpl.class).in(Scopes.SINGLETON);
            bind(BackendPopulator.class).in(Scopes.SINGLETON);
            bind(BackendService.class).to(BackendServiceImpl.class).in(Scopes.SINGLETON);
            bind(BackendStubFactory.class).in(Scopes.SINGLETON);
            bind(SchedulerService.class).to(SchedulerServiceImpl.class).in(Scopes.SINGLETON);
            bind(JobHTTPService.class).to(JobHTTPServiceImpl.class);
            bind(IntermediaryFilesService.class).to(NoOpIntermediaryFilesServiceImpl.class).in(Scopes.SINGLETON);
            bind(BackendHTTPService.class).to(BackendHTTPServiceImpl.class).in(Scopes.SINGLETON);
          }
        }));
    BootstrapUtils.install(locator);

    Configuration configuration = injector.getInstance(Configuration.class);

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
