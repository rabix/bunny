package org.rabix.backend.local;

import org.apache.commons.cli.*;
import org.apache.commons.lang.NotImplementedException;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.helper.URIHelper;
import org.rabix.bindings.model.Application;
import org.rabix.common.json.BeanSerializer;
import org.rabix.common.logging.VerboseLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Command line arguments parser for bunny local executor
 */
public class BackendCommandLineParser {
    private static final Logger logger = LoggerFactory.getLogger(BackendCommandLine.class);
    private String configDirPath;


    public boolean isTesEnabled() {
        return isTesEnabled;
    }

    private boolean isTesEnabled;
    private String appPath;
    private String appUrl;
    private Map<String, Object> configOverrides;
    private List<String> commandLineArray;
    final private CommandLineParser commandLineParser = new DefaultParser();
    private String[] inputArguments;


    private File configDir;
    private File inputsFile;


    public BackendCommandLineParser(String configDirPath, String[] commandLineArguments) {
        this.configDirPath = configDirPath;
//        final CommandLineParser commandLineParser = new DefaultParser();
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

            appPath = commandLine.getArgList().get(0);
            File appFile = new File(URIHelper.extractBase(appPath));
            if (!appFile.exists()) {
                VerboseLogger.log(String.format("Application file %s does not exist.", appFile.getCanonicalPath()));
                printUsageAndExit(posixOptions);
            }

            appUrl = URIHelper.createURI(URIHelper.FILE_URI_SCHEME, appPath);
            if (commandLine.hasOption("resolve-app")) {
                printResolvedAppAndExit(appUrl);
            }

            inputsFile = null;
            if (commandLine.getArgList().size() > 1) {
                String inputsPath = commandLine.getArgList().get(1);
                inputsFile = new File(inputsPath);
                if (!inputsFile.exists()) {
                    VerboseLogger.log(String.format("Inputs file %s does not exist.", inputsFile.getCanonicalPath()));
                    printUsageAndExit(posixOptions);
                }
            }

            configDir = getConfigDir(commandLine, posixOptions);

            if (!configDir.exists() || !configDir.isDirectory()) {
                VerboseLogger.log(String.format("Config directory %s doesn't exist or is not a directory.", configDir.getCanonicalPath()));
                printUsageAndExit(posixOptions);
            }

            configOverrides = new HashMap<>();
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
            if (commandLine.hasOption("cache-dir")) {
                String cacheDir = commandLine.getOptionValue("cache-dir");
                File cacheDirFile = new File(cacheDir);
                if (!cacheDirFile.exists()) {
                    VerboseLogger.log(String.format("Cache directory %s does not exist.", cacheDirFile.getCanonicalPath()));
                    printUsageAndExit(posixOptions);
                }
                configOverrides.put("cache.is_enabled", true);
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

            isTesEnabled = tesURL != null;

        } catch (ParseException e) {
            logger.error("Encountered an error while parsing using Posix parser.", e);
            System.exit(10);
        } catch (IOException e) {
            logger.error("Encountered an error while reading a file.", e);
            System.exit(10);
        }
    }


    public static void main(String[] commandLineArguments) {

    }

    public List<String> getCommandLineArray() { return commandLineArray; }

    public boolean getTesEnabled() { return isTesEnabled; }

    public String getAppPath() { return appPath; }

    public String getAppUrl() { return appUrl; }

    public Map<String, Object> getConfigOverrides() { return configOverrides; }

    public CommandLineParser getCommandLineParser() { return commandLineParser; }

    public String[] getInputArguments() { return inputArguments; }

    public File getConfigDir() { return configDir; }

    public File getInputsFile() { return inputsFile; }


    /**
     * Prints resolved application on standard out
     */

    private void printResolvedAppAndExit(String appUrl) {
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
     * Create command line options
     */
    private Options createOptions() {
        Options options = new Options();
        options.addOption("v", "verbose", false, "print more information on the standard output");
        options.addOption("b", "basedir", true, "execution directory");
        options.addOption("c", "configuration-dir", true, "configuration directory");
        options.addOption("r", "resolve-app", false, "resolve all referenced fragments and print application as a single JSON document");
        options.addOption(null, "cache-dir", true, "basic tool result caching (experimental)");
        options.addOption(null, "no-container", false, "don't use containers");
        options.addOption(null, "tmp-outdir-prefix", true, "doesn't do anything");
        options.addOption(null, "tmpdir-prefix", true, "doesn't do anything");
        options.addOption(null, "outdir", true, "doesn't do anything");
        options.addOption(null, "quiet", false, "don't print anything except final result on standard output");
        options.addOption(null, "tes-url", true, "url of the ga4gh task execution server instance (experimental)");
        options.addOption(null, "version", false, "print program version and exit");
        options.addOption("h", "help", false, "print this help message and exit");
        return options;
    }

    /**
     * Check for missing options
     */
    private boolean checkCommandLine(CommandLine commandLine) {
        if (commandLine.getArgList().size() == 1 || commandLine.getArgList().size() == 2) {
            return true;
        }
        logger.info("Invalid number of arguments\n");
        return false;
    }

    /**
     * Prints command line usage
     */
    private void printUsageAndExit(Options options) {
        HelpFormatter hf = new HelpFormatter();
        hf.setWidth(80);
        hf.setSyntaxPrefix("Usage: ");
        final String usage = "rabix [OPTION]... <tool> <job> [-- {inputs}...]";
        final String header = "Executes CWL application with provided inputs.\n\n";
        final String footer = "\nYou can add/override additional input parameters after -- parameter.\n\n" +
                "Rabix suite homepage: https://rabix.org\n" +
                "Source and issue tracker: https://github.com/rabix/bunny.";
        hf.printHelp(usage, header, options, footer);
        System.exit(10);
    }


    private void printVersionAndExit(Options posixOptions) {
        System.out.println("Rabix 1.0.0-RC2");
        System.exit(0);
    }


    private File getConfigDir(CommandLine commandLine, Options options) throws IOException {
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

        config = new File(homeDir, configDirPath);
        if (!config.exists() || !config.isDirectory()) {
            logger.info("Config directory doesn't exist or is not a directory");
            printUsageAndExit(options);
        }
        return config;
    }

}
