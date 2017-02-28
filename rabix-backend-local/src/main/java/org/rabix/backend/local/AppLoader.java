package org.rabix.backend.local;

import org.apache.commons.cli.*;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.model.Application;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.DataType;
import org.rabix.bindings.model.FileValue;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.logging.VerboseLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Bindings and application loader from command line arguments
 */

public class AppLoader {
    private static final Logger logger = LoggerFactory.getLogger(BackendCommandLine.class);

    private Bindings bindings = null;
    private Application application = null;
    private Map<String, Object> inputs;


    public AppLoader(String appUrl,
                     File inputsFile,
                     List<String> commandLineArray,
                     String[] inputArguments,
                     CommandLineParser commandLineParser) {
        try {
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

                        if (!schemaInput.getDataType().isArray() && values.length > 1) {
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
        } catch (IOException e) {
            logger.error("Encountered an error while reading a file.", e);
            System.exit(10);
        }
    }

    public Bindings getBindings() { return bindings; }

    public Application getApplication() { return application; }

    public Map<String, Object> getInputs() { return inputs; }

    /**
     * Reads content from a file
     */
    private static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    /**
     * Prints command line usage
     */

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
