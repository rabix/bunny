package org.rabix.cli;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.rabix.backend.api.BackendModule;
import org.rabix.backend.api.callback.WorkerStatusCallback;
import org.rabix.backend.api.callback.impl.NoOpWorkerStatusCallback;
import org.rabix.backend.tes.TESModule;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.ProtocolType;
import org.rabix.bindings.helper.URIHelper;
import org.rabix.bindings.model.Application;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.DataType;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.bindings.model.Resources;
import org.rabix.cli.service.LocalDownloadServiceImpl;
import org.rabix.common.config.ConfigModule;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.json.BeanSerializer;
import org.rabix.common.jvm.ClasspathScanner;
import org.rabix.common.logging.VerboseLogger;
import org.rabix.common.service.download.DownloadService;
import org.rabix.common.service.upload.UploadService;
import org.rabix.common.service.upload.impl.NoOpUploadServiceImpl;
import org.rabix.engine.EngineModule;
import org.rabix.engine.service.BackendService;
import org.rabix.engine.service.BootstrapService;
import org.rabix.engine.service.BootstrapServiceException;
import org.rabix.engine.service.ContextRecordService;
import org.rabix.engine.service.IntermediaryFilesHandler;
import org.rabix.engine.service.IntermediaryFilesService;
import org.rabix.engine.service.JobService;
import org.rabix.engine.service.JobServiceException;
import org.rabix.engine.service.SchedulerService;
import org.rabix.engine.service.SchedulerService.SchedulerJobBackendAssigner;
import org.rabix.engine.service.SchedulerService.SchedulerMessageCreator;
import org.rabix.engine.service.SchedulerService.SchedulerMessageSender;
import org.rabix.engine.service.impl.BackendServiceImpl;
import org.rabix.engine.service.impl.BootstrapServiceImpl;
import org.rabix.engine.service.impl.IntermediaryFilesLocalHandler;
import org.rabix.engine.service.impl.IntermediaryFilesServiceImpl;
import org.rabix.engine.service.impl.JobReceiverImpl;
import org.rabix.engine.service.impl.JobServiceImpl;
import org.rabix.engine.service.impl.SchedulerServiceImpl;
import org.rabix.engine.status.EngineStatusCallback;
import org.rabix.engine.status.impl.DefaultEngineStatusCallback;
import org.rabix.engine.store.model.ContextRecord;
import org.rabix.engine.stub.BackendStubFactory;
import org.rabix.engine.stub.impl.BackendStubFactoryImpl;
import org.rabix.transport.mechanism.TransportPlugin.ReceiveCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;

/**
 * Local command line executor
 */
public class BackendCommandLine {

  private static final Logger logger = LoggerFactory.getLogger(BackendCommandLine.class);
  private static String configDir = "/.bunny/config";
  

  @SuppressWarnings("unchecked")
  public static void main(String[] commandLineArguments) {
    final CommandLineParser commandLineParser = new DefaultParser();
    final Options posixOptions = createOptions();

    CommandLine commandLine;
    List<String> commandLineArray = Arrays.asList(commandLineArguments);
    String[] inputArguments = null;
    
    if (commandLineArray.contains("--")) {
      commandLineArguments = commandLineArray.subList(0, commandLineArray.indexOf("--")).toArray(new String[0]);
      inputArguments = commandLineArray.subList(commandLineArray.indexOf("--") + 1, commandLineArray.size()).toArray(new String[0]);
    }

    try {
      commandLine = commandLineParser.parse(posixOptions, commandLineArguments);
      if (commandLine.hasOption("h")) {
        printUsageAndExit(posixOptions);
      }
      if (commandLine.hasOption("version")) {
        printVersionAndExit(posixOptions);
      }
      if (!checkCommandLine(commandLine)) {
        printUsageAndExit(posixOptions);
      }
      
      final String appPath = commandLine.getArgList().get(0);
      File appFile = new File(URIHelper.extractBase(appPath));
      if (!appFile.exists()) {
        VerboseLogger.log(String.format("Application file %s does not exist.", appFile.getCanonicalPath()));
        printUsageAndExit(posixOptions);
      }
      
      String appUrl = URIHelper.createURI(URIHelper.FILE_URI_SCHEME, appPath);
      if (commandLine.hasOption("resolve-app")) {
        printResolvedAppAndExit(appUrl);
      }

      File inputsFile = null;
      if (commandLine.getArgList().size() > 1) {
        String inputsPath = commandLine.getArgList().get(1);
        inputsFile = new File(inputsPath);
        if (!inputsFile.exists()) {
          VerboseLogger.log(String.format("Inputs file %s does not exist.", inputsFile.getCanonicalPath()));
          printUsageAndExit(posixOptions);
        }
      }

      File configDir = getConfigDir(commandLine, posixOptions);

      if (!configDir.exists() || !configDir.isDirectory()) {
        VerboseLogger.log(String.format("Config directory %s doesn't exist or is not a directory.", configDir.getCanonicalPath()));
        printUsageAndExit(posixOptions);
      }

      Map<String, Object> configOverrides = new HashMap<>();
      configOverrides.put("cleaner.backend.period", 5000L);
      
      String directoryName = generateDirectoryName(appPath);
      configOverrides.put("backend.execution.directory.name", directoryName);
          
      String executionDirPath = commandLine.getOptionValue("basedir");
      if (executionDirPath != null) {
        File executionDir = new File(executionDirPath);
        if (!executionDir.exists() || !executionDir.isDirectory()) {
          VerboseLogger.log(String.format("Execution directory %s doesn't exist or is not a directory", executionDirPath));
          System.exit(10);
        } else {
          configOverrides.put("backend.execution.directory", executionDir.getCanonicalPath());
        }
      } else {
        String workingDir = null;
        try {
          workingDir = inputsFile.getParentFile().getCanonicalPath();
        } catch (Exception e) {
          workingDir = new File(".").getCanonicalPath();
        }
        configOverrides.put("backend.execution.directory", workingDir);
      }
      if (commandLine.hasOption("no-container")) {
        configOverrides.put("docker.enabled", false);
      }
      if (commandLine.hasOption("cache-dir")) {
        String cacheDir = commandLine.getOptionValue("cache-dir");
        File cacheDirFile = new File(cacheDir);
        if (!cacheDirFile.exists()) {
          VerboseLogger.log(String.format("Cache directory %s does not exist.", cacheDirFile.getCanonicalPath()));
          printUsageAndExit(posixOptions);
        }
        configOverrides.put("cache.enabled", true);
        configOverrides.put("cache.directory", cacheDirFile.getCanonicalPath());
      }

      String tesURL = commandLine.getOptionValue("tes-url");
      if (tesURL != null) {
        if (tesURL.trim().isEmpty()) {
          VerboseLogger.log("TES URL is empty");
          System.exit(10);
        }
        
        try {
          URL url = new URL(tesURL);
          String host = url.getHost();
          if (host != null) {
            configOverrides.put("rabix.tes.client-host", host);
          }
          Integer port = url.getPort();
          if (port != null) {
            configOverrides.put("rabix.tes.client-port", port);
          }
          String scheme = url.getProtocol();
          if (scheme != null) {
            configOverrides.put("rabix.tes.client-scheme", scheme);
          }
        } catch (Exception e) {
          VerboseLogger.log("TES URL is invalid");
          System.exit(-10);
        }
      }      
      String tesStorageURL = commandLine.getOptionValue("tes-storage");
      if (tesStorageURL != null) {
        configOverrides.put("rabix.tes.storage.base", tesStorageURL);
      }
      
      final ConfigModule configModule = new ConfigModule(configDir, configOverrides);
      Injector injector = Guice.createInjector(
          new EngineModule(configModule),
          new TESModule(configModule),
          new AbstractModule() {
            @Override
            protected void configure() {
              install(configModule);
              
              bind(IntermediaryFilesService.class).to(IntermediaryFilesServiceImpl.class).in(Scopes.SINGLETON);
              bind(IntermediaryFilesHandler.class).to(IntermediaryFilesLocalHandler.class).in(Scopes.SINGLETON);
              
              bind(JobService.class).to(JobServiceImpl.class).in(Scopes.SINGLETON);
              bind(BackendService.class).to(BackendServiceImpl.class).in(Scopes.SINGLETON);
              bind(SchedulerService.class).to(SchedulerServiceImpl.class).in(Scopes.SINGLETON);
              bind(SchedulerMessageCreator.class).to(SchedulerServiceImpl.class).in(Scopes.SINGLETON);
              bind(SchedulerJobBackendAssigner.class).to(SchedulerServiceImpl.class).in(Scopes.SINGLETON);
              bind(SchedulerMessageSender.class).to(SchedulerServiceImpl.class).in(Scopes.SINGLETON);
              bind(EngineStatusCallback.class).to(DefaultEngineStatusCallback.class).in(Scopes.SINGLETON);
              bind(DownloadService.class).to(LocalDownloadServiceImpl.class).in(Scopes.SINGLETON);
              bind(UploadService.class).to(NoOpUploadServiceImpl.class).in(Scopes.SINGLETON);
              bind(WorkerStatusCallback.class).to(NoOpWorkerStatusCallback.class).in(Scopes.SINGLETON);

              bind(BackendStubFactory.class).to(BackendStubFactoryImpl.class).in(Scopes.SINGLETON);
              bind(new TypeLiteral<ReceiveCallback<Job>>(){}).to(JobReceiverImpl.class).in(Scopes.SINGLETON);
              
              Set<Class<BackendModule>> backendModuleClasses = ClasspathScanner.<BackendModule>scanSubclasses(BackendModule.class);
              for (Class<BackendModule> backendModuleClass : backendModuleClasses) {
                try {
                  install(backendModuleClass.getConstructor(ConfigModule.class).newInstance(configModule));
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                  logger.error("Failed to instantiate BackendModule " + backendModuleClass, e);
                  System.exit(33);
                }
              }
              bind(BootstrapService.class).to(BootstrapServiceImpl.class).in(Scopes.SINGLETON);
            }
          });

      // Load app from JSON
      Bindings bindings = null;
      Application application = null;
      
      try {
        bindings = BindingsFactory.create(appUrl);
        application = bindings.loadAppObject(appUrl);
      } catch (NotImplementedException e) {
        logger.error("Not implemented feature");
        System.exit(33);
      } catch (BindingException e) {
        logger.error("Error: " + appUrl + " is not a valid app! {}", e.getMessage());
        System.exit(10);
      }
      if (application == null) {
        VerboseLogger.log("Error reading the app file");
        System.exit(10);
      }
      if (application.getRaw().containsKey("$schemas")) {
        LoggerFactory.getLogger(BackendCommandLine.class).error("Unsupported feature: $schemas.");
      }
      Options appInputOptions = new Options();

      // Create appInputOptions for parser
      for (ApplicationPort schemaInput : application.getInputs()) {
        boolean hasArg = !schemaInput.getDataType().isType(DataType.Type.BOOLEAN);
        appInputOptions.addOption(null, schemaInput.getId().replaceFirst("^#", ""), hasArg, schemaInput.getDescription());
      }

      Map<String, Object> inputs;
      if (inputsFile != null) {
        String inputsText = readFile(inputsFile.getAbsolutePath(), Charset.defaultCharset());
        inputs = (Map<String, Object>) JSONHelper.transform(JSONHelper.readJsonNode(inputsText), true);
      } else {
        inputs = new HashMap<>();
        // No inputs file. If we didn't provide -- at the end, just print app help and exit
        if (!commandLineArray.contains("--"))
          printAppUsageAndExit(appInputOptions);
      }


      if (inputArguments != null) {
        // Parse input values and update inputs map with them
        try {
          CommandLine commandLineInputs = commandLineParser.parse(appInputOptions, inputArguments);

          if (commandLineInputs.getArgList().size() > 0) {
            printAppInvalidUsageAndExit(appInputOptions);
          }

          for (ApplicationPort schemaInput : application.getInputs()) {
            String id = schemaInput.getId().replaceFirst("^#", "");

            if (!commandLineInputs.hasOption(id))
              continue;

            String[] values = commandLineInputs.getOptionValues(id);

            // We have option, but no value for it. That means it's boolean flag.
            if (values == null) {
              inputs.put(id, true);
              continue;
            }

            if (!schemaInput.getDataType().isArray() && values.length>1) {
              VerboseLogger.log(String.format("Input port %s doesn't accept multiple values", id));
              System.exit(10);
            }

            if (schemaInput.getDataType().isFile() ||
                (schemaInput.getDataType().isArray() && schemaInput.getDataType().getSubtype().isFile())) {
              String[] remappedValues = new String[values.length];

              for (int i = 0; i < values.length; i++) {
                File file = new File(values[i]);

                try {
                  remappedValues[i] = file.getCanonicalPath();
                  if (!file.exists()) {
                    VerboseLogger.log(String.format("File %s doesn't exist", file.getCanonicalPath()));
                    System.exit(10);
                  }
                } catch (IOException e) {
                  VerboseLogger.log(String.format("Can't access file %s.", values[i]));
                  System.exit(10);
                }
              }
              values = remappedValues;
            }

            inputs.put(id, createInputValue(values, schemaInput.getDataType()));
          }
        } catch (ParseException e) {
          printAppInvalidUsageAndExit(appInputOptions);
        }
      }

      // Check for required inputs
      List<String> missingRequiredFields = new ArrayList<>();
      for (ApplicationPort schemaInput : application.getInputs()) {
        String id = schemaInput.getId().replaceFirst("^#", "");

        if (schemaInput.isRequired() && schemaInput.getDefaultValue() == null && !inputs.containsKey(id)) {
          missingRequiredFields.add(id);
        }
      }
      if (!missingRequiredFields.isEmpty()) {
        VerboseLogger.log("Required inputs missing: " + StringUtils.join(missingRequiredFields, ", "));
        printAppUsageAndExit(appInputOptions);
      }
      
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

      final BootstrapService bootstrapService = injector.getInstance(BootstrapService.class);
      
      final JobService jobService = injector.getInstance(JobService.class);
      final ContextRecordService contextRecordService = injector.getInstance(ContextRecordService.class);
      
      bootstrapService.start();
      Object commonInputs = null;
      try {
        commonInputs = bindings.translateToCommon(inputs);
      } catch (BindingException e1) {
        VerboseLogger.log("Failed to translate inputs to the common Rabix format");
        System.exit(10);
      }
      
      @SuppressWarnings("unchecked")
      final Job job = jobService.start(new Job(appUrl, (Map<String, Object>) commonInputs), contextConfig);

      final Bindings finalBindings = bindings;
      Thread checker = new Thread(new Runnable() {
        @Override
        @SuppressWarnings("unchecked")
        public void run() {
          ContextRecord contextRecord = contextRecordService.find(job.getId());
          
          while (contextRecord == null || contextRecord.getStatus().equals(ContextRecord.ContextStatus.RUNNING)) {
            try {
              Thread.sleep(1000);
              contextRecord = contextRecordService.find(job.getId());
            } catch (InterruptedException e) {
              logger.error("Failed to wait for root Job to finish", e);
              throw new RuntimeException(e);
            }
          }
          Job rootJob = jobService.get(job.getId());
          if (rootJob.getStatus().equals(JobStatus.COMPLETED)) {
            try {
              try {
                Map<String, Object> outputs = (Map<String, Object>) finalBindings.translateToSpecific(rootJob.getOutputs());
                System.out.println(JSONHelper.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(outputs));
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
    } catch (ParseException e) {
      logger.error("Encountered an error while parsing using Posix parser.", e);
      System.exit(10);
    } catch (IOException e) {
      logger.error("Encountered an error while reading a file.", e);
      System.exit(10);
    } catch (JobServiceException | InterruptedException e) {
      logger.error("Encountered an error while starting local backend.", e);
      System.exit(10);
    } catch (BootstrapServiceException e) {
      logger.error("Encountered an error while starting local backend.", e);
      System.exit(10);
    }
  }

  /**
   * Prints resolved application on standard out 
   */
  private static void printResolvedAppAndExit(String appUrl) {
    Bindings bindings = null;
    Application application = null;
    try {
      bindings = BindingsFactory.create(appUrl);
      application = bindings.loadAppObject(appUrl);
      
      System.out.println(BeanSerializer.serializePartial(application));
      System.exit(0);
    } catch (NotImplementedException e) {
      logger.error("Not implemented feature");
      System.exit(33);
    } catch (BindingException e) {
      logger.error("Error: " + appUrl + " is not a valid app!");
      System.exit(10);
    }
  }
  
  /**
   * Reads content from a file
   */
  static String readFile(String path, Charset encoding) throws IOException {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded, encoding);
  }

  /**
   * Create command line options
   */
  private static Options createOptions() {
    Options options = new Options();
    options.addOption("v", "verbose", false, "print more information on the standard output");
    options.addOption("b", "basedir", true, "execution directory");
    options.addOption("c", "configuration-dir", true, "configuration directory");
    options.addOption("r", "resolve-app", false, "resolve all referenced fragments and print application as a single JSON document");
    options.addOption(null, "cache-dir", true, "basic tool result caching (experimental)");
    options.addOption(null, "no-container", false, "don't use containers");
    options.addOption(null, "tmp-outdir-prefix", true, "legacy compatibility parameter, doesn't do anything");
    options.addOption(null, "tmpdir-prefix", true, "legacy compatibility parameter, doesn't do anything");
    options.addOption(null, "outdir", true, "legacy compatibility parameter, doesn't do anything");
    options.addOption(null, "quiet", false, "don't print anything except final result on standard output");
    options.addOption(null, "tes-url", true, "url of the ga4gh task execution server instance (experimental)");
    options.addOption(null, "tes-storage", true, "path to the storage used by the ga4gh tes server (currently supports locall dirs and google storage cloud paths)");
    // TODO: implement useful cli overrides for config options
//    options.addOption(null, "set-ownership", false, "");
//    options.addOption(null, "ownership-uid", true, "");
//    options.addOption(null, "ownership-gid", true, "");
//    options.addOption(null, "remove-containers", false, "");
//    options.addOption(null, "keep-containers", false, "");
//    options.addOption(null, "delete-intermediary-files", false, "");
//    options.addOption(null, "keep-intermediary-files", false, "");
    options.addOption(null, "version", false, "print program version and exit");
    options.addOption("h", "help", false, "print this help message and exit");
    return options;
  }

  /**
   * Check for missing options
   */
  private static boolean checkCommandLine(CommandLine commandLine) {
    if (commandLine.getArgList().size() == 1 || commandLine.getArgList().size() == 2) {
      return true;
    }
    logger.info("Invalid number of arguments\n");
    return false;
  }

  /**
   * Prints command line usage
   */
  private static void printUsageAndExit(Options options) {
    HelpFormatter hf =new HelpFormatter();
    hf.setWidth(87);
    hf.setSyntaxPrefix("Usage: \n");
    final String usage = "    rabix [OPTIONS]... <app> <inputs> [-- input_parameters...]\n" +
        "    rabix [OPTIONS]... <app> -- input_parameters...\n\n" +
        "where:\n" +
        " <app>               is the path to a CWL document that describes the app.\n" +
        " <inputs>            is the JSON or YAML file that provides the values of app inputs.\n" +
        " input_parameters... are the app input values specified directly from the command line\n\n";
    final String header = "Executes CWL application with provided inputs.\n\nOptions:\n";
    final String footer = "\nInput parameters are specified at the end of the command, after the -- delimiter. You can specify values for each input, using the following format:\n" +
        "  --<input_port_id> <value>\n\n" +
            "Rabix suite homepage: http://rabix.io\n" +
            "Source and issue tracker: https://github.com/rabix/bunny.";
    hf.printHelp(usage, header, options, footer);
    System.exit(10);
  }

  private static void printAppUsageAndExit(Options options) {
    HelpFormatter h = new HelpFormatter();
    h.setSyntaxPrefix("");
    h.printHelp("Inputs for selected tool are: ", options);
    System.exit(10);
  }
  
  private static void printVersionAndExit(Options posixOptions) {
    System.out.println("Rabix 1.0.1");
    System.exit(0);
  }

  private static void printAppInvalidUsageAndExit(Options options) {
    HelpFormatter h = new HelpFormatter();
    h.setSyntaxPrefix("");
    h.printHelp("You have invalid inputs for the tool you provided. Valid inputs are: ", options);
    System.exit(10);
  }

  private static File getConfigDir(CommandLine commandLine, Options options) throws IOException {
    String configPath = commandLine.getOptionValue("configuration-dir");
    if (configPath != null) {
      File config = new File(configPath);
      if (config.exists() && config.isDirectory()) {
        return config;
      } else {
        logger.debug("Configuration directory {} doesn't exist or is not a directory.", configPath);
      }
    }
    File config = new File(new File(BackendCommandLine.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile().getParentFile() + "/config");

    logger.debug("Config path: {}", config.getCanonicalPath());
    if (config.exists() && config.isDirectory()) {
      logger.debug("Configuration directory found localy.");
      return config;
    }
    String homeDir = System.getProperty("user.home");

    config = new File(homeDir, configDir);
    if (!config.exists() || !config.isDirectory()) {
      logger.info("Config directory doesn't exist or is not a directory");
      printUsageAndExit(options);
    }
    return config;
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

  private static Object createInputValue(String[] value, DataType inputType) {
    if (inputType.isArray()) {
      if (inputType.getSubtype().isFile()) {
        List<FileValue> ret = new ArrayList<>();
        for (String s : value) {
          ret.add(new FileValue(null, s, null, null, null, null, null));
        }
        return ret;
      } else {
        return Arrays.asList(value);
      }
    }

    if (inputType.isFile()) {
      return new FileValue(null, value[0], null, null, null, null, null);
    } else {
      return value[0];
    }
  }
  
  /**
   * Returns a directory name containing the current date and app name
   */
  private static String generateDirectoryName(String path) {
    String name = FilenameUtils.getBaseName(path);
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd-HHmmss.S");
    return name + "-" + df.format(new Date());
  }

}
