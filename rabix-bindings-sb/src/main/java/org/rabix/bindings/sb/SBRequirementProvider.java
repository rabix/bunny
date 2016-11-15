package org.rabix.bindings.sb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolRequirementProvider;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.requirement.CustomRequirement;
import org.rabix.bindings.model.requirement.DockerContainerRequirement;
import org.rabix.bindings.model.requirement.EnvironmentVariableRequirement;
import org.rabix.bindings.model.requirement.FileRequirement;
import org.rabix.bindings.model.requirement.FileRequirement.SingleFileRequirement;
import org.rabix.bindings.model.requirement.Requirement;
import org.rabix.bindings.model.requirement.ResourceRequirement;
import org.rabix.bindings.sb.bean.SBJob;
import org.rabix.bindings.sb.bean.SBJobApp;
import org.rabix.bindings.sb.bean.resource.SBResource;
import org.rabix.bindings.sb.bean.resource.requirement.SBCreateFileRequirement;
import org.rabix.bindings.sb.bean.resource.requirement.SBDockerResource;
import org.rabix.bindings.sb.bean.resource.requirement.SBEnvVarRequirement;
import org.rabix.bindings.sb.bean.resource.requirement.SBEnvVarRequirement.EnvironmentDef;
import org.rabix.bindings.sb.expression.SBExpressionException;
import org.rabix.bindings.sb.expression.helper.SBExpressionBeanHelper;
import org.rabix.bindings.sb.helper.SBFileValueHelper;
import org.rabix.bindings.sb.helper.SBJobHelper;
import org.rabix.bindings.sb.helper.SBSchemaHelper;

public class SBRequirementProvider implements ProtocolRequirementProvider {

  private DockerContainerRequirement getDockerRequirement(SBDockerResource sbDockerResource) {
    if (sbDockerResource == null) {
      return null;
    }
    return new DockerContainerRequirement(sbDockerResource.getDockerPull(), sbDockerResource.getImageId(), null);
  }

  private EnvironmentVariableRequirement getEnvironmentVariableRequirement(SBJob sbJob, SBEnvVarRequirement envVarRequirement) throws BindingException {
    if (envVarRequirement == null) {
      return null;
    }

    List<EnvironmentDef> envDefinitions = envVarRequirement.getEnvironmentDefinitions();
    if (envDefinitions == null) {
      return new EnvironmentVariableRequirement(Collections.<String, String> emptyMap());
    }
    Map<String, String> result = new HashMap<>();
    for (EnvironmentDef envDef : envDefinitions) {
      String key = envDef.getName();
      Object value = envDef.getValue();

      if (SBExpressionBeanHelper.isExpression(value)) {
        try {
          value = SBExpressionBeanHelper.evaluate(sbJob, value);
        } catch (SBExpressionException e) {
          throw new BindingException(e);
        }
      }
      if (value == null) {
        throw new BindingException("Environment variable for " + key + " is empty.");
      }
      result.put(key, value.toString());
    }
    return new EnvironmentVariableRequirement(result);
  }

  private FileRequirement getFileRequirement(SBJob sbJob, SBCreateFileRequirement createFileRequirement) throws BindingException {
    if (createFileRequirement == null) {
      return null;
    }

    List<SBCreateFileRequirement.SBFileRequirement> fileRequirements = createFileRequirement.getFileRequirements();
    if (fileRequirements == null) {
      return null;
    }

    List<SingleFileRequirement> result = new ArrayList<>();
    for (SBCreateFileRequirement.SBFileRequirement fileRequirement : fileRequirements) {
      try {
        String filename = (String) fileRequirement.getFilename(sbJob);

        Object content = fileRequirement.getContent(sbJob);

        if (SBSchemaHelper.isFileFromValue(content)) {
          FileValue fileValue = SBFileValueHelper.createFileValue(content);
          result.add(new FileRequirement.SingleInputFileRequirement(filename, fileValue, true));
        } else {
          result.add(new FileRequirement.SingleTextFileRequirement(filename, (String) content));
        }
      } catch (SBExpressionException e) {
        throw new BindingException(e);
      }
    }
    return new FileRequirement(result);
  }

  @Override
  public List<Requirement> getRequirements(Job job) throws BindingException {
    SBJob sbJob = SBJobHelper.getSBJob(job);
    SBJobApp sbJobApp = sbJob.getApp();
    return convertRequirements(job, sbJobApp.getRequirements());
  }
  
  @Override
  public List<Requirement> getHints(Job job) throws BindingException {
    SBJob sbJob = SBJobHelper.getSBJob(job);
    SBJobApp sbJobApp = sbJob.getApp();
    return convertRequirements(job, sbJobApp.getHints());
  }
  
  private List<Requirement> convertRequirements(Job job, List<SBResource> resources) throws BindingException {
    if (resources == null) {
      return Collections.<Requirement> emptyList();
    }
    SBJob sbJob = SBJobHelper.getSBJob(job);

    List<Requirement> result = new ArrayList<>();
    for (SBResource sbResource : resources) {
      if (sbResource instanceof SBDockerResource) {
        result.add(getDockerRequirement((SBDockerResource) sbResource));
        continue;
      }
      if (sbResource instanceof SBEnvVarRequirement) {
        result.add(getEnvironmentVariableRequirement(sbJob, (SBEnvVarRequirement) sbResource));
        continue;
      }
      if (sbResource instanceof SBCreateFileRequirement) {
        result.add(getFileRequirement(sbJob, (SBCreateFileRequirement) sbResource));
        continue;
      }
      result.add(new CustomRequirement(sbResource.getType(), sbResource.getRaw()));
    }
    return result;
  }

  @Override
  public ResourceRequirement getResourceRequirement(Job job) throws BindingException {
    SBJob sbJob = SBJobHelper.getSBJob(job);
    
    try {
      Long cpu = sbJob.getCPU() != null ? sbJob.getCPU().longValue() : null ;
      Long memory = sbJob.getMemory() != null ? sbJob.getMemory().longValue() : null;
      return new ResourceRequirement(cpu, null, memory, null, null, null, null);
    } catch (SBExpressionException e) {
      throw new BindingException(e);
    }
  }

}
