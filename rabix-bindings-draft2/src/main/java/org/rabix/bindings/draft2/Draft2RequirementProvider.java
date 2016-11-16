package org.rabix.bindings.draft2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolRequirementProvider;
import org.rabix.bindings.draft2.bean.Draft2Job;
import org.rabix.bindings.draft2.bean.Draft2JobApp;
import org.rabix.bindings.draft2.bean.resource.Draft2Resource;
import org.rabix.bindings.draft2.bean.resource.requirement.Draft2CreateFileRequirement;
import org.rabix.bindings.draft2.bean.resource.requirement.Draft2DockerResource;
import org.rabix.bindings.draft2.bean.resource.requirement.Draft2EnvVarRequirement;
import org.rabix.bindings.draft2.bean.resource.requirement.Draft2EnvVarRequirement.EnvironmentDef;
import org.rabix.bindings.draft2.expression.Draft2ExpressionException;
import org.rabix.bindings.draft2.expression.helper.Draft2ExpressionBeanHelper;
import org.rabix.bindings.draft2.helper.Draft2FileValueHelper;
import org.rabix.bindings.draft2.helper.Draft2JobHelper;
import org.rabix.bindings.draft2.helper.Draft2SchemaHelper;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.requirement.CustomRequirement;
import org.rabix.bindings.model.requirement.DockerContainerRequirement;
import org.rabix.bindings.model.requirement.EnvironmentVariableRequirement;
import org.rabix.bindings.model.requirement.FileRequirement;
import org.rabix.bindings.model.requirement.FileRequirement.SingleFileRequirement;
import org.rabix.bindings.model.requirement.Requirement;
import org.rabix.bindings.model.requirement.ResourceRequirement;

public class Draft2RequirementProvider implements ProtocolRequirementProvider {

  private DockerContainerRequirement getDockerRequirement(Draft2DockerResource draft2DockerResource) {
    if (draft2DockerResource == null) {
      return null;
    }
    return new DockerContainerRequirement(draft2DockerResource.getDockerPull(), draft2DockerResource.getImageId(), null);
  }

  private EnvironmentVariableRequirement getEnvironmentVariableRequirement(Draft2Job draft2Job, Draft2EnvVarRequirement envVarRequirement) throws BindingException {
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

      if (Draft2ExpressionBeanHelper.isExpression(value)) {
        try {
          value = Draft2ExpressionBeanHelper.evaluate(draft2Job, value);
        } catch (Draft2ExpressionException e) {
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

  private FileRequirement getFileRequirement(Draft2Job draft2Job, Draft2CreateFileRequirement createFileRequirement) throws BindingException {
    if (createFileRequirement == null) {
      return null;
    }

    List<Draft2CreateFileRequirement.Draft2FileRequirement> fileRequirements = createFileRequirement.getFileRequirements();
    if (fileRequirements == null) {
      return null;
    }

    List<SingleFileRequirement> result = new ArrayList<>();
    for (Draft2CreateFileRequirement.Draft2FileRequirement fileRequirement : fileRequirements) {
      try {
        String filename = (String) fileRequirement.getFilename(draft2Job);

        Object content = fileRequirement.getContent(draft2Job);

        if (Draft2SchemaHelper.isFileFromValue(content)) {
          FileValue fileValue = Draft2FileValueHelper.createFileValue(content);
          result.add(new FileRequirement.SingleInputFileRequirement(filename, fileValue, true));
        } else {
          result.add(new FileRequirement.SingleTextFileRequirement(filename, (String) content));
        }
      } catch (Draft2ExpressionException e) {
        throw new BindingException(e);
      }
    }
    return new FileRequirement(result);
  }

  @Override
  public List<Requirement> getRequirements(Job job) throws BindingException {
    Draft2Job draft2Job = Draft2JobHelper.getDraft2Job(job);
    Draft2JobApp draft2JobApp = draft2Job.getApp();
    return convertRequirements(job, draft2JobApp.getRequirements());
  }
  
  @Override
  public List<Requirement> getHints(Job job) throws BindingException {
    Draft2Job draft2Job = Draft2JobHelper.getDraft2Job(job);
    Draft2JobApp draft2JobApp = draft2Job.getApp();
    return convertRequirements(job, draft2JobApp.getHints());
  }
  
  private List<Requirement> convertRequirements(Job job, List<Draft2Resource> resources) throws BindingException {
    if (resources == null) {
      return Collections.<Requirement> emptyList();
    }
    Draft2Job draft2Job = Draft2JobHelper.getDraft2Job(job);

    List<Requirement> result = new ArrayList<>();
    for (Draft2Resource draft2Resource : resources) {
      if (draft2Resource instanceof Draft2DockerResource) {
        result.add(getDockerRequirement((Draft2DockerResource) draft2Resource));
        continue;
      }
      if (draft2Resource instanceof Draft2EnvVarRequirement) {
        result.add(getEnvironmentVariableRequirement(draft2Job, (Draft2EnvVarRequirement) draft2Resource));
        continue;
      }
      if (draft2Resource instanceof Draft2CreateFileRequirement) {
        result.add(getFileRequirement(draft2Job, (Draft2CreateFileRequirement) draft2Resource));
        continue;
      }
      result.add(new CustomRequirement(draft2Resource.getType(), draft2Resource.getRaw()));
    }
    return result;
  }

  @Override
  public ResourceRequirement getResourceRequirement(Job job) throws BindingException {
    Draft2Job draft2Job = Draft2JobHelper.getDraft2Job(job);
    
    try {
      Long cpu = draft2Job.getCPU() != null ? draft2Job.getCPU().longValue() : null ;
      Long memory = draft2Job.getMemory() != null ? draft2Job.getMemory().longValue() : null;
      return new ResourceRequirement(cpu, null, memory, null, null, null, null);
    } catch (Draft2ExpressionException e) {
      throw new BindingException(e);
    }
  }

}
