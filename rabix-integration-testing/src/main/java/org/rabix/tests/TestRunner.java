package org.rabix.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.rabix.common.helper.JSONHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestRunner {

  private static String testDirPath;
  private static String cmdPrefix;
  private static String buildFilePath;
  private static String buildFileDirPath = "./rabix-backend-local/target/";
  private static String currentTestSuite;
  private static String integrationTempResultPath = "./rabix-backend-local/target/result.yaml";
  private static String workingdir;
  private static String cwlTestWorkingdir;
  private static String draftName;
  private static String[] drafts = { "draft-sb", "draft-2", "draft-3", "cwl" };
  private static final Logger logger = LoggerFactory.getLogger(TestRunner.class);

  public static void main(String[] commandLineArguments) throws Exception {
    PropertiesConfiguration configuration = getConfig();
    setupIntegrationCommandPrefix(configuration);
    extractBuildFile();
    copyTestsInWorkingDir();

    for (String draft : drafts) {
      draftName = draft;
      startIntegrationTests(draftName);

      if (draftName.equals("cwl")) {
        startConformanceTests(draftName);
      }
    }
  }

  private static void startConformanceTests(String draftName) throws Exception {
    logger.info("Conformance tests started:  " + draftName);
    PropertiesConfiguration configuration = getConfig();
    cwlTestWorkingdir = getStringFromConfig(configuration, draftName);

    String starterScriptName = draftName + "_starter.sh";
    String commandCopyCwlStarter = "cp " + System.getProperty("user.dir") + "/rabix-integration-testing/cwlstarter/" + starterScriptName + " .";

    command(commandCopyCwlStarter, cwlTestWorkingdir);

    logger.info("Conformance working dir: " + cwlTestWorkingdir);
    logger.info("Conformance starter script: " + starterScriptName);

    command("chmod +x " + starterScriptName, cwlTestWorkingdir);

    executeConformanceSuite("./" + starterScriptName, cwlTestWorkingdir);
    logger.info("Conformance test ended: " + draftName);
  }

  private static void startIntegrationTests(String draftName) throws RabixTestException, IOException {
    boolean allTestsPassed = true;
    boolean testPassed = false;
    List<Object> failedTests = new ArrayList<Object>();

    logger.info("Integration tests started: " + draftName);

    PropertiesConfiguration configuration = getConfig();
    setupIntegrationTestDirPath(configuration, draftName);

    File dir = new File(testDirPath);

    if (!dir.isDirectory()) {
      logger.error("Test directory path is not valid directory path: " + testDirPath);
      System.exit(-1);
    }

    File[] directoryListing = dir.listFiles();

    if (directoryListing == null) {
      logger.error("Problem with test directory: Test directory is empty.");
    }

    logger.info("Test directory used: " + testDirPath);

    for (File child : directoryListing) {
      if (!child.getPath().endsWith(".test.yaml")) {
        continue;
      }
      try {
        currentTestSuite = child.getPath();
        logger.info("Executing test suite: " + currentTestSuite);
        String currentTest = readFile(child.getAbsolutePath(), Charset.defaultCharset());
        Map<String, Object> inputSuite = JSONHelper.readMap(JSONHelper.readJsonNode(currentTest));
        Iterator<?> entries = inputSuite.entrySet().iterator();

        while (entries.hasNext()) {
          Entry thisEntry = (Entry) entries.next();
          Object testName = thisEntry.getKey();
          Object test = thisEntry.getValue();

          logger.info("Running test: " + testName + " with given parameters:");

          @SuppressWarnings({ "rawtypes", "unchecked" })
          Map<String, Map<String, LinkedHashMap>> currentTestDetails = (Map<String, Map<String, LinkedHashMap>>) test;

          logger.info("  app: " + currentTestDetails.get("app"));
          logger.info("  inputs: " + currentTestDetails.get("inputs"));
          logger.info("  expected: " + currentTestDetails.get("expected"));

          String cmd = cmdPrefix + " " + currentTestDetails.get("app") + " " + currentTestDetails.get("inputs")
              + " > result.yaml";

          logger.info("->Running cmd: " + cmd);
          command(cmd, workingdir);

          File integrationTempResultFile = new File(integrationTempResultPath);

          String resultText = readFile(integrationTempResultFile.getAbsolutePath(), Charset.defaultCharset());
          logger.info("\nGenerated result file:");
          logger.info(resultText);
          Map<String, Object> actualResult = JSONHelper.readMap(JSONHelper.readJsonNode(resultText));
          testPassed = validateTestCase(currentTestDetails, actualResult);
          logger.info("Test result: ");
          if (testPassed) {
            logger.info(testName + " PASSED");
          } else {
            logger.info(testName + " FAILED");
            failedTests.add(testName);
            allTestsPassed = false;
          }
        }

        if (allTestsPassed) {
          logger.info("");
          logger.info("Test suite: " + currentTestSuite + ", passed successfully.");
        } else {
          logger.info("");
          logger.info("Test suite " + currentTestSuite + ", failed:");
          logger.info("Failed test number: " + failedTests.size());
          logger.info("Failed tests:");
          for (Object test : failedTests) {
            logger.info(test.toString());
          }

        }

      } catch (IOException e) {
        logger.error("Test suite: " + currentTestSuite + ", execution failed. ", e);
        System.exit(-1);
      }
    }
    logger.info("Integration tests finished:  " + draftName);
  }

  private static void copyTestsInWorkingDir() throws RabixTestException {
    String commandCopyTestbacklog = "cp -a " + System.getProperty("user.dir") + "/rabix-integration-testing/testbacklog .";
    logger.info("Working dir user in copy method: " + workingdir);
    command(commandCopyTestbacklog, workingdir);
    logger.info("Copying testbacklog command: " + commandCopyTestbacklog);
    logger.info("Copying testbacklog dir: done ");
  }

  private static void extractBuildFile() throws RabixTestException {
    File buildFileDir = new File(buildFileDirPath);
    buildFileDirPath = buildFileDir.getAbsolutePath();
    File[] directoryListing = buildFileDir.listFiles();

    if (directoryListing != null) {
      for (File child : directoryListing) {
        if (child.getPath().contains("tar.gz")) {
          logger.info("Found build file with given path: " + child.getAbsolutePath());
          buildFilePath = child.getAbsolutePath();
        }
      }
    } else {
      throw new RabixTestException("Build folder is empty. Check build status.");
    }
    logger.info("Extracting build file: started");

    String commandUntarBuildFile = "tar -zxvf " + buildFilePath;
    logger.info("Extracting build file command: " + commandUntarBuildFile);
    command(commandUntarBuildFile, buildFileDirPath);

    File[] dirListingAfterUnpac = buildFileDir.listFiles();
    for (File child : dirListingAfterUnpac) {
      if (child.isDirectory() && child.getName().startsWith("rabix")) {
        workingdir = child.getAbsolutePath();
        logger.info("Working dir set: " + workingdir);

        integrationTempResultPath = workingdir + "/result.yaml";
      }
    }

    logger.info("Create rabix symlink");
    command("ln -s " + workingdir + "/rabix .", buildFileDirPath);

    logger.info("Extracting build file: ended");
  
  }

  private static void setupBuildFilePath(PropertiesConfiguration configuration) {
    buildFilePath = getStringFromConfig(configuration, "buildFile");
  }

  private static void setupIntegrationCommandPrefix(PropertiesConfiguration configuration) {
    cmdPrefix = getStringFromConfig(configuration, "cmdPrefix");
  }

  private static void setupIntegrationTestDirPath(PropertiesConfiguration configuration, String draftName) {
    testDirPath = getStringFromConfig(configuration, "testDirPath_" + draftName);
  }

  private static boolean validateTestCase(Map<String, Map<String, LinkedHashMap>> mapTest, Map<String, Object> resultData) {

    String resultFileName;
    int resultFileSize;
    String resultFileClass;
    String resultFileChecksum;
    LinkedHashMap resultMetadata;
    LinkedHashMap expectedMetadata;

    Map<String, Object> resultValues = null;

    resultValues = ((Map<String, Object>) resultData.get("output"));

    resultFileName = resultValues.get("path").toString();
    resultFileName = resultFileName.split("/")[resultFileName.split("/").length - 1];
    resultFileSize = (int) resultValues.get("size");
    resultFileClass = resultValues.get("class").toString();
    resultFileChecksum = (String) resultValues.get("checksum");
    resultMetadata = (LinkedHashMap) resultValues.get("metadata");
    expectedMetadata = (LinkedHashMap) mapTest.get("expected").get("outfile").get("metadata");
    boolean fileMetadataEqual = true;

    boolean fileNamesEqual = resultFileName.equals(mapTest.get("expected").get("outfile").get("name"));
    boolean fileSizesEqual = resultFileSize == (int) mapTest.get("expected").get("outfile").get("size");
    boolean fileClassesEqual = resultFileClass.equals(mapTest.get("expected").get("outfile").get("class"));
    boolean fileChecksumsEqual = resultFileChecksum.equals(mapTest.get("expected").get("outfile").get("checksum"));

    logger.info("Test validation:");
    logger.info("result file name: " + resultFileName + ", expected file name: "
        + mapTest.get("expected").get("outfile").get("name"));
    logger.info("result file size: " + resultFileSize + ", expected file size: "
        + mapTest.get("expected").get("outfile").get("size"));
    logger.info("result file class: " + resultFileClass + ", expected file class: "
        + mapTest.get("expected").get("outfile").get("class"));
    logger.info("result file checksum: " + resultFileChecksum + ", expected file checksum: "
        + mapTest.get("expected").get("outfile").get("checksum"));

    if (expectedMetadata != null) {
      fileMetadataEqual = expectedMetadata.equals(resultMetadata);

      logger.info("result file metadata: " + resultMetadata.entrySet() + ", expected file metadata: "
          + expectedMetadata.entrySet());
      validateMetadata(fileMetadataEqual);
    }

    validateName(fileNamesEqual);
    validateSize(fileSizesEqual);
    validateClass(fileClassesEqual);
    validateChecksum(fileChecksumsEqual);

    boolean validationResult = fileNamesEqual && fileSizesEqual && fileClassesEqual && fileChecksumsEqual
        && fileMetadataEqual;

    return validationResult;
  }

  private static void validateMetadata(boolean fileMetadataEqual) {
    if (!fileMetadataEqual) {
      logger.error("result and expected file metadata are not equal!");
    }

  }

  private static void validateChecksum(boolean fileChecksumsEqual) {
    if (!fileChecksumsEqual) {
      logger.error("result and expected file checksums are not equal!");
    }

  }

  private static void validateClass(boolean fileClassesEqual) {
    if (!fileClassesEqual) {
      logger.error("result and expected file class are not equal!");
    }

  }

  private static void validateSize(boolean fileSizesEqual) {
    if (!fileSizesEqual) {
      logger.error("result and expected file size are not equal!");
    }

  }

  private static void validateName(boolean fileNamesEqual) {
    if (!fileNamesEqual) {
      logger.error("result and expected file name are not equal!");
    }
  }

  public static void command(final String cmdline, final String directory) throws RabixTestException {
    logger.debug("Executing " + cmdline);
    try {
      Process process = new ProcessBuilder(new String[] { "bash", "-c", cmdline }).inheritIO()
          .directory(new File(directory)).start();

      int exitCode = process.waitFor();

      if (0 != exitCode) {
        File resultFile = new File(integrationTempResultPath);
        String stdErr = readFile(resultFile.getAbsolutePath(), Charset.defaultCharset());
        logger.error(stdErr);
        throw new RabixTestException("Error while executing command: Non zero exit code " + exitCode);
      }

    } catch (Exception e) {
      logger.error("Error while executing command. ", e);
      throw new RabixTestException("Error while executing command: " + e.getMessage());
    }
  }

  public static void executeConformanceSuite(final String cmdline, final String directory) throws Exception {
    ProcessBuilder processBuilder = new ProcessBuilder(new String[] { "bash", "-c", cmdline }).inheritIO()
          .directory(new File(directory));

      Map<String, String> env = processBuilder.environment();
      env.put("LC_ALL", "C");
      env.put("buildFileDirPath", buildFileDirPath);

      Process process = processBuilder.start();

      int exitCode = process.waitFor();

      if (0 != exitCode) {
        logger.error("Error while executing command: Non zero exit code " + exitCode);
        System.exit(exitCode);
      }

  }

  /**
   * Reads content from a file
   */
  static String readFile(String path, Charset encoding) throws IOException {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded, encoding);
  }

  @SuppressWarnings("unchecked")
  private static PropertiesConfiguration getConfig() throws RabixTestException {
    PropertiesConfiguration configuration = new PropertiesConfiguration();
    String userDir = System.getProperty("user.dir");
    if (userDir == null) {
      throw new RabixTestException("null value for user.dir property");
    }
    File configDir = new File(userDir + "/rabix-integration-testing/config/test");
    logger.info("Config directory set: " + configDir.toString());
    try {
      Iterator<File> iterator = FileUtils.iterateFiles(configDir, new String[] { "properties" }, true);
      while (iterator.hasNext()) {
        File configFile = iterator.next();
        configuration.load(configFile);
      }
      return configuration;
    } catch (ConfigurationException e) {
      logger.error("Failed to load configuration properties", e);
      throw new RabixTestException("Failed to load configuration properties");
    }
  }

  private static String getStringFromConfig(PropertiesConfiguration configuration, String key) {
    return configuration.getString(key);
  }
}
