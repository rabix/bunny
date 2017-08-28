package org.rabix.tests;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.rabix.common.helper.JSONHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;

public class CacheTestRunner {

  private static String testDirPath;
  private static String cmdPrefix;
  private static String buildFilePath;
  private static String buildFileDirPath = "./rabix-cli/target/";
  private static String currentTestSuite;
  private static String integrationTempResultPath = "./rabix-cli/target/result.yaml";
  private static String workingdir;
  private static String cwlTestWorkingdir;
  private static String draftName;
  private static String[] drafts = { "draft-sb"};
  private static final Logger logger = LoggerFactory.getLogger(CacheTestRunner.class);

  public static void main(String[] commandLineArguments) throws Exception {
    PropertiesConfiguration configuration = getConfig();
    setupIntegrationCommandPrefix(configuration);
    setupBuildFiles();
    copyCacheConfig();

    for (String draft : drafts) {
      draftName = draft + "-cache";
      startIntegrationTests(draftName);
    }
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
          logger.info("  cache: " + currentTestDetails.get("cache"));
          logger.info("  expected: " + currentTestDetails.get("expected"));

          String cmd = cmdPrefix + " --cache-dir " + currentTestDetails.get("cache") + " " +
                  currentTestDetails.get("app") + " " + currentTestDetails.get("inputs")
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

  private static void setupBuildFiles() throws RabixTestException {
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

    File[] dirListingAfterUnpac = buildFileDir.listFiles();
    for (File child : dirListingAfterUnpac) {
      if (child.isDirectory() && child.getName().startsWith("rabix")) {
        workingdir = child.getAbsolutePath();
        logger.info("Working dir set: " + workingdir);

        integrationTempResultPath = workingdir + "/result.yaml";
      }
    }

  }

  private static void copyCacheConfig() throws RabixTestException {
    String commandCopyTestbacklog = "cp " + System.getProperty("user.dir") + "/rabix-integration-testing/config/test/test.core.properties config/core.properties";
    logger.info("Working dir user in copy method: " + workingdir);
    command(commandCopyTestbacklog, workingdir);
    logger.info("Copying testbacklog command: " + commandCopyTestbacklog);
    logger.info("Copying testbacklog dir: done ");
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

    ArrayList<String> validationResult = objectContainsObject(mapTest.get("expected"), resultData);
    if(validationResult.size() == 0) {
      return true;
    }
    else {
      StringBuilder keyMapToFault = new StringBuilder();
      for(int i=validationResult.size()-1; i>=0; i--) {
        keyMapToFault.append(validationResult.get(i));
        if(i!=0) {
          keyMapToFault.append(" => ");
        }
      }
      logger.error("Could not validate results! Problem with key: " + keyMapToFault.toString());
      return false;
    }
  }

  /**
   * Check whether a1 is included in a2.
   * a1 and a2 can be or include lists and maps that have string keys.
   *
   * @param a1
   * @param a2
   * @return true if a1 completely is inside of a2
   */
  private static <T, R> ArrayList<String> objectContainsObject(T a1, R a2) {
    ArrayList<String> result = new ArrayList<>();
    if(a1 == null && a2 == null) {
      return result;
    }
    else if(a1 instanceof Map && a2 instanceof Map) {
      try {
        Map<String, ?> a1Map = ((Map<String, ?>) a1);
        Map<String, ?> a2Map = ((Map<String, ?>) a2);

        for(String key:a1Map.keySet()) {
          if(!a2Map.containsKey(key)) {
            result.add(key);
            return result;
          }
          ArrayList<String> comparison = objectContainsObject(a1Map.get(key), a2Map.get(key));
          if(comparison.size() != 0) {
            comparison.add(key);
            return comparison;
          }
        }
      }catch (Exception e) {
        result.add("exception");
        return result;
      }
    }
    else if(a1 instanceof List && a2 instanceof List) {
      List a1List = (List) a1;
      List a2List = (List) a2;
      if(a1List.size() != a2List.size()) {
        result.add("list size");
        return result;
      }
      for(int i=0; i<a1List.size(); i++) {
        ArrayList<String> comparison = objectContainsObject(a1List.get(i), a2List.get(i));
        if(comparison.size() != 0) {
          comparison.add(String.valueOf(i));
          return comparison;
        }
      }
    }
    else{
      if(!a1.equals(a2)) {
        result.add("Comp failed");
        return result;
      }
    }
    return result;
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