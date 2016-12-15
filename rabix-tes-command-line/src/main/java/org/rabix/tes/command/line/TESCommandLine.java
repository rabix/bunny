package org.rabix.tes.command.line;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.rabix.bindings.model.Job;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.logging.VerboseLogger;
import org.rabix.tes.command.line.service.TESCommandLineService;
import org.rabix.tes.command.line.service.impl.TESFinalizeService;
import org.rabix.tes.command.line.service.impl.TESInitializeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TESCommandLine {

private final static Logger logger = LoggerFactory.getLogger(TESCommandLine.class);
  
  private final static String WORKING_DIR_PATH = "/tmp";

  public static enum TESCommandLineMode {
    INITIALIZE,
    FINALIZE
  }

  public static void main(String[] commandLineArguments) {
    final CommandLineParser commandLineParser = new DefaultParser();
    final Options posixOptions = createOptions();

    CommandLine mainCommandLine;
    try {
      mainCommandLine = commandLineParser.parse(posixOptions, commandLineArguments);
      if (mainCommandLine.hasOption("h")) {
        printUsageAndExit(posixOptions);
      }
      
      String jobPath = mainCommandLine.getOptionValue("job");
      if (jobPath == null) {
        VerboseLogger.log("Job file is not specified.");
        printUsageAndExit(posixOptions);
      }
      
      File jobFile = new File(jobPath);
      if (!jobFile.exists()) {
        VerboseLogger.log(String.format("Job file %s does not exist.", jobFile.getCanonicalPath()));
        printUsageAndExit(posixOptions);
      }
      
      String workingDirPath = mainCommandLine.getOptionValue("working-dir");
      if (workingDirPath == null) {
        workingDirPath = WORKING_DIR_PATH;
      }
      
      File workingDir = new File(workingDirPath);
      if (!workingDir.exists()) {
        VerboseLogger.log(String.format("Working directory file %s does not exist.", workingDir.getCanonicalPath()));
        printUsageAndExit(posixOptions);
      }
      
      String modeStr = mainCommandLine.getOptionValue("mode");
      if (modeStr == null) {
        VerboseLogger.log("Command line tool mode is not specified.");
        printUsageAndExit(posixOptions);
      }
      
      TESCommandLineMode mode = null;
      try {
        mode = TESCommandLineMode.valueOf(modeStr.toUpperCase().trim());
      } catch (Exception e) {
        VerboseLogger.log(String.format("Unknown command line mode %s.", modeStr));
        printUsageAndExit(posixOptions);
      }
      
      TESCommandLineService tesService = null;
      
      switch (mode) {
      case INITIALIZE:
        tesService = new TESInitializeService();
        break;
      case FINALIZE:
        tesService = new TESFinalizeService();        
        break;
      default:
        logger.error("Unknown TES command " + mode);
        System.exit(10);
      }
      
      Job job = JSONHelper.readObject(FileUtils.readFileToString(jobFile), Job.class);
      tesService.execute(job, workingDir);
    } catch (Exception e) {
      logger.error("Encountered an error while initializing TES backend.", e);
      e.printStackTrace(System.err);
      System.exit(10);
    }
  }
  
  /**
   * Create command line options
   */
  private static Options createOptions() {
    Options options = new Options();
    options.addOption("j", "job", true, "job file");
    options.addOption("w", "working-dir", true, "working directory file");
    options.addOption("m", "mode", true, "command line tool mode (initialize/finalize)");
    options.addOption("h", "help", false, "help");
    return options;
  }

  /**
   * Prints command line usage
   */
  private static void printUsageAndExit(Options options) {
    new HelpFormatter().printHelp("tes-initialize -job <job> -working-dir <working dir> -mode <initialize|finalize> [OPTION]", options);
    System.exit(10);
  }
  
}
