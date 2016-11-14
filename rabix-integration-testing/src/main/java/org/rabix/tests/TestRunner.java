package org.rabix.tests;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
	private static String buildFile;
	private static String currentTestSuite;
	private static String resultPath = "./rabix-backend-local/target/result.yaml";
	private static String workingdir = "./rabix-backend-local/target/";
	private static final Logger logger = LoggerFactory.getLogger(TestRunner.class);

	public static void main(String[] commandLineArguments) {
		try {
			
			logger.info("Integration testing: started");
			PropertiesConfiguration configuration = getConfig();
			testDirPath = getStringFromConfig(configuration, "testDirPath");
			cmdPrefix = getStringFromConfig(configuration, "cmdPrefix");
			buildFile = getStringFromConfig(configuration, "buildFile");
			startTestExecution();
			logger.info("Integration testing: finished");
			
			logger.info("Conformance testing: started");
			startConformanceTests();
			logger.info("Conformance testing: finished");
			
		} catch (RabixTestException e) {
			logger.error("Error occuerred:", e);
			System.exit(-1);
		}
	}

	private static void startConformanceTests() throws RabixTestException {
		
		
		
		String commandCwlTest = "cwltest --test conformance_test_draft-2.yaml --tool " + System.getProperty("user.dir")
		+ "/rabix-backend-local/target/rabix-backend-local-0.6.1-SNAPSHOT-id3/rabix";
		
		String cwltestWorkingDir = System.getProperty("user.dir") + "/rabix-integration-testing/common-workflow-language/draft-2";
		
		command("virtualenv env", cwltestWorkingDir);
		command("source env/bin/activate", cwltestWorkingDir);
		command("pip install -e git+https://github.com/common-workflow-language/cwltest.git@master#egg=cwltest", cwltestWorkingDir);
		
		
		logger.info("work dir check1:");
		command("pwd", workingdir);
		logger.info("work dir check2:");
		command("pwd", cwltestWorkingDir);
		
		logger.info("debugging draft-2 test dir:");
		command("ls -ltra ", cwltestWorkingDir);
		
		logger.info("Executing cwltest command: " + commandCwlTest);
		command(commandCwlTest, cwltestWorkingDir);
	
	}

	private static void startTestExecution() throws RabixTestException {
		boolean allTestsPassed = true;
		boolean testPassed = false;
		File dir = new File(testDirPath);
		File[] directoryListing = dir.listFiles();
		ArrayList<Object> failedTests = new ArrayList<Object>();

		if (!dir.isDirectory()) {
			logger.error("Problem with test directory path: Test directory path is not valid directory path.");
			System.exit(-1);
		}
		if (directoryListing == null) {
			logger.error("Problem with provided test directory: Test directory is empty.");
		}

		logger.info("Extracting build file: started");
		
		String commandUntarBuildFile = "tar -zxvf " + System.getProperty("user.dir") + buildFile;
		
		File extractDir = new File(System.getProperty("user.dir") + buildFile);

		if (!extractDir.isDirectory()) {
			logger.error("Problem with extract directory path: Test extract directory path is not valid directory path.");
			System.exit(-1);
		}
		
		logger.info("Extracting build file command: " + commandUntarBuildFile);
		command(commandUntarBuildFile, System.getProperty("user.dir") + "/rabix-backend-local/target/");
		logger.info("Extracting build file: done");

		String commandCopyTestbacklog = "cp -a " + System.getProperty("user.dir")
				+ "/rabix-integration-testing/testbacklog .";
		command(commandCopyTestbacklog, workingdir);
		logger.info("Copying testbacklog command: " + commandCopyTestbacklog);
		logger.info("Copying testbacklog dir: done ");

		for (File child : directoryListing) {
			if (!child.getPath().endsWith(".test.yaml"))
				continue;
			try {

				currentTestSuite = child.getPath();
				logger.info(" ### ");
				logger.info("Executing test suite: " + currentTestSuite);
				String currentTest = readFile(child.getAbsolutePath(), Charset.defaultCharset());
				Map<String, Object> inputSuite = JSONHelper.readMap(JSONHelper.transformToJSON(currentTest));
				Iterator entries = inputSuite.entrySet().iterator();

				while (entries.hasNext()) {
					Entry thisEntry = (Entry) entries.next();
					Object testName = thisEntry.getKey();
					Object test = thisEntry.getValue();
					logger.info(" --- ");
					logger.info("Running test: " + testName + " with given parameters:");
					@SuppressWarnings({ "rawtypes", "unchecked" })
					Map<String, Map<String, LinkedHashMap>> mapTest = (Map<String, Map<String, LinkedHashMap>>) test;
					logger.info("  app: " + mapTest.get("app"));
					logger.info("  inputs: " + mapTest.get("inputs"));
					logger.info("  expected: " + mapTest.get("expected"));
					String cmd = cmdPrefix + " " + mapTest.get("app") + " " + mapTest.get("inputs") + " > result.yaml";
					logger.info("->Running cmd: " + cmd);
					command(cmd, workingdir);

					File resultFile = new File(resultPath);

					String resultText = readFile(resultFile.getAbsolutePath(), Charset.defaultCharset());
					Map<String, Object> resultData = JSONHelper.readMap(JSONHelper.transformToJSON(resultText));
					logger.info("\nGenerated result file:");
					logger.info(resultText);
					testPassed = validateTestCase(mapTest, resultData);
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

	}

	private static boolean validateTestCase(Map<String, Map<String, LinkedHashMap>> mapTest,
			Map<String, Object> resultData) {
		String resultFileName;
		int resultFileSize;
		String resultFileClass;
		Map<String, Object> resultValues = ((Map<String, Object>) resultData.get("outfile"));
		resultFileName = resultValues.get("path").toString();
		resultFileName = resultFileName.split("/")[resultFileName.split("/").length - 1];
		resultFileSize = (int) resultValues.get("size");
		resultFileClass = resultValues.get("class").toString();
		logger.info("Test validation:");
		logger.info("result file name: " + resultFileName + ", expected file name: "
				+ mapTest.get("expected").get("outfile").get("name"));
		logger.info("result file size: " + resultFileSize + ", expected file size: "
				+ mapTest.get("expected").get("outfile").get("size"));
		logger.info("result file class: " + resultFileClass + ", expected file class: "
				+ mapTest.get("expected").get("outfile").get("class"));

		boolean fileNamesEqual = resultFileName.equals(mapTest.get("expected").get("outfile").get("name"));
		boolean fileSizesEqual = resultFileSize == (int) mapTest.get("expected").get("outfile").get("size");
		boolean fileClassesEqual = resultFileClass.equals(mapTest.get("expected").get("outfile").get("class"));

		if (!fileNamesEqual) {
			logger.error("result and expected file name are not equal!");
		} else {
			if (!fileSizesEqual) {
				logger.error("result and expected file size are not equal!");
			} else {
				if (!fileClassesEqual) {
					logger.error("result and expected file class are not equal!");
				} else {
					logger.info("Test case passed.");
					return true;
				}
			}
		}

		return false;
	}

	public static void command(final String cmdline, final String directory) throws RabixTestException {
		try {
			Process process = new ProcessBuilder(new String[] {cmdline }).inheritIO()
					.directory(new File(directory)).start();

			BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = null;
			while ((line = br.readLine()) != null)
				logger.info(line);

			int exitCode = process.waitFor();

			if (0 != exitCode) {
				File resultFile = new File(resultPath);
				String stdErr = readFile(resultFile.getAbsolutePath(), Charset.defaultCharset());
				logger.error(stdErr);
				throw new RabixTestException("Error while executing command: Non zero exit code " + exitCode);
			}

		} catch (Exception e) {
			logger.error("Error while executing command. ", e);
			throw new RabixTestException("Error while executing command: " + e.getMessage());
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
