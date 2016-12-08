package org.rabix.backend.local;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.rabix.backend.local.download.LocalDownloadServiceImpl;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.ProtocolType;
import org.rabix.bindings.helper.URIHelper;
import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.bindings.model.Application;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.DataType;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.bindings.model.Resources;
import org.rabix.common.config.ConfigModule;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.logging.VerboseLogger;
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
import org.rabix.engine.rest.service.JobService;
import org.rabix.engine.rest.service.JobServiceException;
import org.rabix.engine.rest.service.impl.BackendServiceImpl;
import org.rabix.engine.rest.service.impl.JobServiceImpl;
import org.rabix.executor.ExecutorModule;
import org.rabix.executor.config.StorageConfiguration;
import org.rabix.executor.config.impl.DefaultStorageConfiguration;
import org.rabix.executor.pathmapper.InputFileMapper;
import org.rabix.executor.pathmapper.OutputFileMapper;
import org.rabix.executor.pathmapper.local.LocalPathMapper;
import org.rabix.executor.service.ExecutorService;
import org.rabix.executor.status.ExecutorStatusCallback;
import org.rabix.executor.status.impl.NoOpExecutorStatusCallback;
import org.rabix.ftp.SimpleFTPModule;
import org.rabix.transport.backend.BackendPopulator;
import org.rabix.transport.backend.impl.BackendLocal;
import org.rabix.transport.mechanism.TransportPluginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;

/**
 * Local command line executor
 */
public class BackendCommandLine {

  private static final Logger logger = LoggerFactory.getLogger(BackendCommandLine.class);
  private static String configDir = "/.bunny/config";
  

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
      if (!checkCommandLine(commandLine)) {
        printUsageAndExit(posixOptions);
      }

      String appPath = commandLine.getArgList().get(0);
      File appFile = new File(URIHelper.extractBase(appPath));
      if (!appFile.exists()) {
        VerboseLogger.log(String.format("Application file %s does not exist.", appFile.getCanonicalPath()));
        printUsageAndExit(posixOptions);
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
        configOverrides.put("backend.docker.enabled", false);
      }

      ConfigModule configModule = new ConfigModule(configDir, configOverrides);
      Injector injector = Guice.createInjector(
          new SimpleFTPModule(), 
          new EngineModule(),
          new ExecutorModule(configModule), 
          new AbstractModule() {
            @Override
            protected void configure() {
              bind(JobDB.class).in(Scopes.SINGLETON);
              bind(StorageConfiguration.class).to(DefaultStorageConfiguration.class).in(Scopes.SINGLETON);
              bind(BackendDB.class).in(Scopes.SINGLETON);
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
            }
          });

      String appUrl = URIHelper.createURI(URIHelper.FILE_URI_SCHEME, appPath);

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
        logger.error("Error: " + appUrl + " is not a valid app!");
        System.exit(10);
      }
      if (application == null) {
        VerboseLogger.log("Error reading the app file");
        System.exit(10);
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
        inputs = JSONHelper.readMap(JSONHelper.transformToJSON(inputsText));
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

      final JobService jobService = injector.getInstance(JobService.class);
      final BackendService backendService = injector.getInstance(BackendService.class);
      final ExecutorService executorService = injector.getInstance(ExecutorService.class);

      BackendLocal backendLocal = new BackendLocal();
      backendLocal = backendService.create(backendLocal);
      executorService.initialize(backendLocal);

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
    } catch (ParseException e) {
      logger.error("Encountered an error while parsing using Posix parser.", e);
      System.exit(10);
    } catch (IOException e) {
      logger.error("Encountered an error while reading a file.", e);
      System.exit(10);
    } catch (JobServiceException | TransportPluginException | InterruptedException e) {
      logger.error("Encountered an error while starting local backend.", e);
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
    options.addOption("v", "verbose", false, "verbose");
    options.addOption("b", "basedir", true, "execution directory");
    options.addOption("c", "configuration-dir", true, "configuration directory");
    options.addOption(null, "no-container", false, "don't use containers");
    options.addOption(null, "tmp-outdir-prefix", true, "doesn't do anything");
    options.addOption(null, "tmpdir-prefix", true, "doesn't do anything");
    options.addOption(null, "outdir", true, "doesn't do anything");
    options.addOption(null, "quiet", false, "quiet");
    options.addOption("h", "help", false, "help");
    return options;
  }

  /**
   * Check for missing options
   */
  private static boolean checkCommandLine(CommandLine commandLine) {
    if (commandLine.getArgList().size() == 1 || commandLine.getArgList().size() == 2) {
      return true;
    }
    VerboseLogger.log("Invalid number of arguments\n");
    return false;
  }

  /**
   * Prints command line usage
   */
  private static void printUsageAndExit(Options options) {
    new HelpFormatter().printHelp("rabix <tool> <job> [OPTION] [-- {inputs}...]", options);
    System.exit(10);
  }

  private static void printAppUsageAndExit(Options options) {
    HelpFormatter h = new HelpFormatter();
    h.setSyntaxPrefix("");
    h.printHelp("Inputs for selected tool are: ", options);
    System.exit(10);
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

    logger.debug("Config path: " + config.getCanonicalPath());
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

}
