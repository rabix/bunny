package org.rabix.tes.command.line.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.mapper.FileMappingException;
import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Resources;
import org.rabix.bindings.model.requirement.EnvironmentVariableRequirement;
import org.rabix.bindings.model.requirement.FileRequirement;
import org.rabix.bindings.model.requirement.FileRequirement.SingleFileRequirement;
import org.rabix.bindings.model.requirement.FileRequirement.SingleInputDirectoryRequirement;
import org.rabix.bindings.model.requirement.FileRequirement.SingleInputFileRequirement;
import org.rabix.bindings.model.requirement.FileRequirement.SingleTextFileRequirement;
import org.rabix.bindings.model.requirement.Requirement;
import org.rabix.common.logging.VerboseLogger;
import org.rabix.tes.command.line.service.TESCommandLineException;
import org.rabix.tes.command.line.service.TESCommandLineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TESInitializeService implements TESCommandLineService {

  private final static Logger logger = LoggerFactory.getLogger(TESInitializeService.class);
  
  public static final String HOME_ENV_VAR = "HOME";
  public static final String TMPDIR_ENV_VAR = "TMPDIR";

  public void execute(Job job, File workingDir) throws TESCommandLineException {
    try {
      Bindings bindings = BindingsFactory.create(job);

      List<Requirement> combinedRequirements = new ArrayList<>();
      combinedRequirements.addAll(bindings.getHints(job));
      combinedRequirements.addAll(bindings.getRequirements(job));

      stageFileRequirements(workingDir, combinedRequirements);

      if (bindings.isSelfExecutable(job)) {
        VerboseLogger.log("Do not generate command.sh file. Tool is an expression tool.");
        return;
      }
      
      String commandLine = bindings.buildCommandLineObject(job, workingDir, new FilePathMapper() {
        @Override
        public String map(String path, Map<String, Object> config) throws FileMappingException {
          return path;
        }
      }).build();

      File resultFile = new File(workingDir.getParentFile(), "command.sh");
      FileUtils.writeStringToFile(resultFile, commandLine);
      VerboseLogger.log(String.format("File %s created.", resultFile.getAbsolutePath()));
      
      EnvironmentVariableRequirement environmentVariableResource = getRequirement(combinedRequirements, EnvironmentVariableRequirement.class);
      Map<String, String> environmentVariables = environmentVariableResource != null ? environmentVariableResource.getVariables() : new HashMap<String, String>();
      Resources resources = job.getResources();
      if (resources != null) {
        if (resources.getWorkingDir() != null) {
          environmentVariables.put(HOME_ENV_VAR, resources.getWorkingDir());
        }
        if (resources.getTmpDir() != null) {
          environmentVariables.put(TMPDIR_ENV_VAR, resources.getTmpDir());
        }
      }

      StringBuilder envVarContentBuilder = new StringBuilder();
      if (environmentVariables != null) {
        for (Entry<String, String> variableEntry : environmentVariables.entrySet()) {
          envVarContentBuilder.append("export ").append(variableEntry.getKey()).append("=").append(variableEntry.getValue()).append("\n");
        }
      }
      File environmentFile = new File(workingDir.getParentFile(), "environment.sh");
      FileUtils.writeStringToFile(environmentFile, envVarContentBuilder.toString());
      VerboseLogger.log(String.format("File %s created.", environmentFile.getAbsolutePath()));
    } catch (BindingException e) {
      logger.error("Failed to use Bindings", e);
      throw new TESCommandLineException("Failed to use Bindings", e);
    } catch (IOException e) {
      logger.error("Failed to use Bindings", e);
      throw new TESCommandLineException("Failed to use Bindings", e);
    }
  }

  private void stageFileRequirements(File workingDir, List<Requirement> requirements) throws BindingException {
    try {
      FileRequirement fileRequirementResource = getRequirement(requirements, FileRequirement.class);
      if (fileRequirementResource == null) {
        return;
      }

      List<SingleFileRequirement> fileRequirements = fileRequirementResource.getFileRequirements();
      if (fileRequirements == null) {
        return;
      }
      for (SingleFileRequirement fileRequirement : fileRequirements) {
        logger.info("Process file requirement {}", fileRequirement);

        File destinationFile = new File(workingDir, fileRequirement.getFilename());
        if (fileRequirement instanceof SingleTextFileRequirement) {
          FileUtils.writeStringToFile(destinationFile, ((SingleTextFileRequirement) fileRequirement).getContent());
          continue;
        }
        if (fileRequirement instanceof SingleInputFileRequirement || fileRequirement instanceof SingleInputDirectoryRequirement) {
          String path = ((SingleInputFileRequirement) fileRequirement).getContent().getPath();
          File file = new File(path);
          if (!file.exists()) {
            continue;
          }
          if (file.isFile()) {
            FileUtils.copyFile(file, destinationFile);
          } else {
            FileUtils.copyDirectory(file, destinationFile);
          }
        }
      }
    } catch (IOException e) {
      logger.error("Failed to process file requirements.", e);
      throw new BindingException("Failed to process file requirements.");
    }
  }

  @SuppressWarnings("unchecked")
  private <T extends Requirement> T getRequirement(List<Requirement> requirements, Class<T> clazz) {
    for (Requirement requirement : requirements) {
      if (requirement.getClass().equals(clazz)) {
        return (T) requirement;
      }
    }
    return null;
  }

}
