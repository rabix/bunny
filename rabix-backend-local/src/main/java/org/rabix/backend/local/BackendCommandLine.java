package org.rabix.backend.local;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import org.rabix.backend.local.download.LocalDownloadServiceImpl;
import org.rabix.backend.local.tes.client.TESHttpClient;
import org.rabix.backend.local.tes.service.TESStorageService;
import org.rabix.backend.local.tes.service.impl.LocalTESExecutorServiceImpl;
import org.rabix.backend.local.tes.service.impl.LocalTESStorageServiceImpl;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.ProtocolType;
import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.bindings.model.Resources;
import org.rabix.common.config.ConfigModule;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.logging.VerboseLogger;
import org.rabix.common.retry.RetryInterceptorModule;
import org.rabix.common.service.download.DownloadService;
import org.rabix.common.service.upload.UploadService;
import org.rabix.common.service.upload.impl.NoOpUploadServiceImpl;
import org.rabix.engine.EngineModule;
import org.rabix.engine.rest.api.BackendHTTPService;
import org.rabix.engine.rest.api.JobHTTPService;
import org.rabix.engine.rest.api.impl.BackendHTTPServiceImpl;
import org.rabix.engine.rest.api.impl.JobHTTPServiceImpl;
import org.rabix.engine.rest.backend.BackendDispatcher;
import org.rabix.engine.rest.db.BackendDB;
import org.rabix.engine.rest.db.JobDB;
import org.rabix.engine.rest.service.BackendService;
import org.rabix.engine.rest.service.IntermediaryFilesService;
import org.rabix.engine.rest.service.JobService;
import org.rabix.engine.rest.service.JobServiceException;
import org.rabix.engine.rest.service.impl.BackendServiceImpl;
import org.rabix.engine.rest.service.impl.IntermediaryFilesServiceLocalImpl;
import org.rabix.engine.rest.service.impl.JobServiceImpl;
import org.rabix.executor.config.StorageConfiguration;
import org.rabix.executor.config.impl.LocalStorageConfiguration;
import org.rabix.executor.container.impl.DockerContainerHandler.DockerClientLockDecorator;
import org.rabix.executor.execution.JobHandlerCommandDispatcher;
import org.rabix.executor.handler.JobHandler;
import org.rabix.executor.handler.JobHandlerFactory;
import org.rabix.executor.handler.impl.JobHandlerImpl;
import org.rabix.executor.pathmapper.InputFileMapper;
import org.rabix.executor.pathmapper.OutputFileMapper;
import org.rabix.executor.pathmapper.local.LocalPathMapper;
import org.rabix.executor.service.*;
import org.rabix.executor.service.impl.*;
import org.rabix.executor.status.ExecutorStatusCallback;
import org.rabix.executor.status.impl.NoOpExecutorStatusCallback;
import org.rabix.ftp.SimpleFTPModule;
import org.rabix.transport.backend.BackendPopulator;
import org.rabix.transport.backend.impl.BackendLocal;
import org.rabix.transport.mechanism.TransportPluginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/*
 * Local command line executor
 */
public class BackendCommandLine {

    private static final Logger logger = LoggerFactory.getLogger(BackendCommandLine.class);
    private static String configDir = "/.bunny/config";


    public static void main(String[] commandLineArguments) {
        try {
            BackendCommandLineParser backendCommandLineParser = new BackendCommandLineParser(configDir, commandLineArguments);
            final ConfigModule configModule = new ConfigModule(backendCommandLineParser.getConfigDir(), backendCommandLineParser.getConfigOverrides());
            Injector injector = Guice.createInjector(
                    new SimpleFTPModule(),
                    new EngineModule(),
                    new AbstractModule() {
                        @Override
                        protected void configure() {
                            install(configModule);

                            bind(JobDB.class).in(Scopes.SINGLETON);
                            bind(StorageConfiguration.class).toInstance(new LocalStorageConfiguration(backendCommandLineParser.getAppPath(), configModule.provideConfig()));
                            bind(BackendDB.class).in(Scopes.SINGLETON);
                            bind(IntermediaryFilesService.class).to(IntermediaryFilesServiceLocalImpl.class).in(Scopes.SINGLETON);
                            bind(JobService.class).to(JobServiceImpl.class).in(Scopes.SINGLETON);
                            bind(BackendPopulator.class).in(Scopes.SINGLETON);
                            bind(BackendService.class).to(BackendServiceImpl.class).in(Scopes.SINGLETON);
                            bind(BackendDispatcher.class).in(Scopes.SINGLETON);
                            bind(JobHTTPService.class).to(JobHTTPServiceImpl.class);
                            bind(DownloadService.class).to(LocalDownloadServiceImpl.class).in(Scopes.SINGLETON);
                            bind(UploadService.class).to(NoOpUploadServiceImpl.class).in(Scopes.SINGLETON);
                            bind(ExecutorStatusCallback.class).to(NoOpExecutorStatusCallback.class).in(Scopes.SINGLETON);
                            bind(BackendHTTPService.class).to(BackendHTTPServiceImpl.class).in(Scopes.SINGLETON);
                            bind(FilePathMapper.class).annotatedWith(InputFileMapper.class).to(LocalPathMapper.class);
                            bind(FilePathMapper.class).annotatedWith(OutputFileMapper.class).to(LocalPathMapper.class);

                            if (backendCommandLineParser.getTesEnabled()) {
                                bind(TESHttpClient.class).in(Scopes.SINGLETON);
                                bind(TESStorageService.class).to(LocalTESStorageServiceImpl.class).in(Scopes.SINGLETON);
                                bind(ExecutorService.class).to(LocalTESExecutorServiceImpl.class).in(Scopes.SINGLETON);
                            } else {
                                install(new RetryInterceptorModule());
                                install(new FactoryModuleBuilder().implement(JobHandler.class, JobHandlerImpl.class).build(JobHandlerFactory.class));

                                bind(DockerClientLockDecorator.class).in(Scopes.SINGLETON);

                                bind(JobFitter.class).to(JobFitterImpl.class).in(Scopes.SINGLETON);
                                bind(JobDataService.class).to(JobDataServiceImpl.class).in(Scopes.SINGLETON);
                                bind(JobHandlerCommandDispatcher.class).in(Scopes.SINGLETON);

                                bind(FileService.class).to(FileServiceImpl.class).in(Scopes.SINGLETON);
                                bind(ExecutorService.class).to(ExecutorServiceImpl.class).in(Scopes.SINGLETON);
                                bind(FilePermissionService.class).to(FilePermissionServiceImpl.class).in(Scopes.SINGLETON);
                                bind(CacheService.class).to(CacheServiceImpl.class).in(Scopes.SINGLETON);
                            }
                        }
                    });

            // Load app from JSON
            AppLoader appLoader = new AppLoader(backendCommandLineParser.getAppUrl(), backendCommandLineParser.getInputsFile(), backendCommandLineParser.getCommandLineArray(),
                    backendCommandLineParser.getInputArguments(), backendCommandLineParser.getCommandLineParser());
            Bindings bindings = appLoader.getBindings();
            Map<String, Object> inputs = appLoader.getInputs();

            Resources resources = null;
            Map<String, Object> contextConfig = null;

            resources = extractResources(inputs, bindings.getProtocolType());
            if (resources != null) {
                contextConfig = new HashMap<String, Object>();
                if (resources.getCpu() != null) {
                    contextConfig.put("allocatedResources.cpu", resources.getCpu().toString());
                }
                if (resources.getMemMB() != null) {
                    contextConfig.put("allocatedResources.mem", resources.getMemMB().toString());
                }
            }

            final JobService jobService = injector.getInstance(JobService.class);
            final BackendService backendService = injector.getInstance(BackendService.class);
            final ExecutorService executorService = injector.getInstance(ExecutorService.class);

            BackendLocal backendLocal = new BackendLocal();
            backendLocal = backendService.create(backendLocal);
            executorService.initialize(backendLocal);

            Object commonInputs = null;
            try {
                commonInputs = appLoader.getBindings().translateToCommon(inputs);
            } catch (BindingException e1) {
                VerboseLogger.log("Failed to translate inputs to the common Rabix format");
                System.exit(10);
            }

            @SuppressWarnings("unchecked")
            final Job job = jobService.start(new Job(backendCommandLineParser.getAppUrl(), (Map<String, Object>) commonInputs), contextConfig);

            final Bindings finalBindings = bindings;
            Thread checker = new Thread(new Runnable() {
                @Override
                @SuppressWarnings("unchecked")
                public void run() {
                    Job rootJob = jobService.get(job.getId());

                    while (!Job.isFinished(rootJob)) {
                        try {
                            Thread.sleep(1000);
                            rootJob = jobService.get(job.getId());
                        } catch (InterruptedException e) {
                            logger.error("Failed to wait for root Job to finish", e);
                            throw new RuntimeException(e);
                        }
                    }
                    if (rootJob.getStatus().equals(JobStatus.COMPLETED)) {
                        try {
                            try {
                                Map<String, Object> outputs = (Map<String, Object>) finalBindings.translateToSpecific(rootJob.getOutputs());
                                System.out.println(JSONHelper.mapperWithoutNulls.writerWithDefaultPrettyPrinter().writeValueAsString(outputs));
                                System.exit(0);
                            } catch (BindingException e) {
                                logger.error("Failed to translate common outputs to native", e);
                                throw new RuntimeException(e);
                            }
                        } catch (JsonProcessingException e) {
                            logger.error("Failed to write outputs to standard out", e);
                            System.exit(10);
                        }
                    } else {
                        VerboseLogger.log("Failed to execute a Job");
                        System.exit(10);
                    }
                }
            });
            checker.start();
            checker.join();
        } catch (JobServiceException | TransportPluginException | InterruptedException e) {
            logger.error("Encountered an error while starting local backend.", e);
            System.exit(10);
        }
    }

    @SuppressWarnings("unchecked")
    private static Resources extractResources(Map<String, Object> inputs, ProtocolType protocol) {
        switch (protocol) {
            case DRAFT2: {
                if (inputs.containsKey("allocatedResources")) {
                    Map<String, Object> allocatedResources = (Map<String, Object>) inputs.get("allocatedResources");
                    Long cpu = ((Integer) allocatedResources.get("cpu")).longValue();
                    Long mem = ((Integer) allocatedResources.get("mem")).longValue();
                    return new Resources(cpu, mem, null, false, null, null, null, null);
                }
            }
            case DRAFT3:
                return null;
            default:
                return null;
        }
    }

}
