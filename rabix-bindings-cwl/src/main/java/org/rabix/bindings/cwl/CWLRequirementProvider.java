package org.rabix.bindings.cwl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolRequirementProvider;
import org.rabix.bindings.cwl.bean.CWLJob;
import org.rabix.bindings.cwl.bean.CWLJobApp;
import org.rabix.bindings.cwl.bean.resource.CWLResource;
import org.rabix.bindings.cwl.bean.resource.requirement.CWLDockerResource;
import org.rabix.bindings.cwl.bean.resource.requirement.CWLEnvVarRequirement;
import org.rabix.bindings.cwl.bean.resource.requirement.CWLEnvVarRequirement.EnvironmentDef;
import org.rabix.bindings.cwl.bean.resource.requirement.CWLInitialWorkDirRequirement;
import org.rabix.bindings.cwl.bean.resource.requirement.CWLInitialWorkDirRequirement.CWLDirent;
import org.rabix.bindings.cwl.bean.resource.requirement.CWLResourceRequirement;
import org.rabix.bindings.cwl.expression.CWLExpressionException;
import org.rabix.bindings.cwl.expression.CWLExpressionResolver;
import org.rabix.bindings.cwl.helper.CWLDirectoryValueHelper;
import org.rabix.bindings.cwl.helper.CWLFileValueHelper;
import org.rabix.bindings.cwl.helper.CWLJobHelper;
import org.rabix.bindings.cwl.helper.CWLSchemaHelper;
import org.rabix.bindings.model.DirectoryValue;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.requirement.CustomRequirement;
import org.rabix.bindings.model.requirement.DockerContainerRequirement;
import org.rabix.bindings.model.requirement.EnvironmentVariableRequirement;
import org.rabix.bindings.model.requirement.FileRequirement;
import org.rabix.bindings.model.requirement.FileRequirement.SingleFileRequirement;
import org.rabix.bindings.model.requirement.Requirement;
import org.rabix.bindings.model.requirement.ResourceRequirement;

public class CWLRequirementProvider implements ProtocolRequirementProvider {

  private DockerContainerRequirement getDockerRequirement(CWLDockerResource cwlDockerResource) {
    if (cwlDockerResource == null) {
      return null;
    }
    return new DockerContainerRequirement(cwlDockerResource.getDockerPull(), cwlDockerResource.getImageId(), cwlDockerResource.getDockerOutputDirectory());
  }

  private EnvironmentVariableRequirement getEnvironmentVariableRequirement(CWLJob cwlJob, CWLEnvVarRequirement envVarRequirement) throws BindingException {
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

      try {
        value = CWLExpressionResolver.resolve(value, cwlJob, null);
      } catch (CWLExpressionException e) {
        throw new BindingException(e);
      }
      if (value == null) {
        throw new BindingException("Environment variable for " + key + " is empty.");
      }
      result.put(key, value.toString());
    }
    return new EnvironmentVariableRequirement(result);
  }
  private void processFileValue(List<SingleFileRequirement> result, boolean linkEnabled, String entryname, FileValue fileValue) {
    result.add(new FileRequirement.SingleInputFileRequirement((String) entryname, fileValue, linkEnabled));        
    if(fileValue.getSecondaryFiles() !=null)
      for(FileValue secondary: fileValue.getSecondaryFiles()){
        processFileValue(result, linkEnabled, secondary.getName(), secondary);
    }
  }
  private FileRequirement getFileRequirement(CWLJob cwlJob, CWLInitialWorkDirRequirement initialWorkDirRequirement) throws BindingException {
    if (initialWorkDirRequirement == null) {
      return null;
    }

    List<Object> listing = null;
    try {
      listing = initialWorkDirRequirement.getListing(cwlJob);
    } catch (CWLExpressionException e) {
      throw new BindingException(e);
    }
    
    List<SingleFileRequirement> result = new ArrayList<>();
    
    for (Object listingObj : listing) {
      if (listingObj instanceof CWLDirent) {
        CWLDirent dirent = (CWLDirent) listingObj;
        
        Object entry = dirent.getEntry();
        Object entryname = dirent.getEntryname(); // TODO explicit cast
        if (CWLSchemaHelper.isFileFromValue(entry)) {
          processFileValue(result, !dirent.isWritable(), (String) entryname, CWLFileValueHelper.createFileValue(((CWLDirent) listingObj).getEntry()));
          continue;
        }
        if (CWLSchemaHelper.isDirectoryFromValue(entry)) {
          DirectoryValue directoryValue = CWLDirectoryValueHelper.createDirectoryValue(((CWLDirent) listingObj).getEntry());
          result.add(new FileRequirement.SingleInputDirectoryRequirement((String) entryname, directoryValue, !dirent.isWritable()));
          continue;
        }
        if (entry instanceof String) {
          result.add(new FileRequirement.SingleTextFileRequirement((String) entryname, (String) entry));    // TODO discuss cast
        }
        continue;
      }
      if (listingObj instanceof FileValue) {
        FileValue fileValue = (FileValue) listingObj;
        processFileValue(result, false, (String) fileValue.getName(), (FileValue) listingObj);
        continue;
      }
      if (listingObj instanceof DirectoryValue) {
        DirectoryValue directoryValue = (DirectoryValue) listingObj;
        result.add(new FileRequirement.SingleInputDirectoryRequirement(directoryValue.getName(), directoryValue, false));
        continue;
      }
      throw new BindingException("Failed to create file requirements. Unknown value " + listingObj);
    }
    return new FileRequirement(result);
  }

  @Override
  public List<Requirement> getRequirements(Job job) throws BindingException {
    CWLJob cwlJob = CWLJobHelper.getCWLJob(job);
    CWLJobApp cwlJobApp = cwlJob.getApp();
    return convertRequirements(job, cwlJobApp.getRequirements());
  }

  @Override
  public List<Requirement> getHints(Job job) throws BindingException {
    CWLJob cwlJob = CWLJobHelper.getCWLJob(job);
    CWLJobApp cwlJobApp = cwlJob.getApp();
    return convertRequirements(job, cwlJobApp.getHints());
  }

  private List<Requirement> convertRequirements(Job job, List<CWLResource> resources) throws BindingException {
    EnvironmentVariableRequirement environmentVariableRequirement = null;
    if (resources == null) {
      return Collections.<Requirement> emptyList();
    }
    CWLJob cwlJob = CWLJobHelper.getCWLJob(job);

    List<Requirement> result = new ArrayList<>();
    for (CWLResource cwlResource : resources) {
      if (cwlResource instanceof CWLDockerResource) {
        result.add(getDockerRequirement((CWLDockerResource) cwlResource));
        continue;
      }
      if (cwlResource instanceof CWLEnvVarRequirement) {
        environmentVariableRequirement = getEnvironmentVariableRequirement(cwlJob, (CWLEnvVarRequirement) cwlResource);
        result.add(environmentVariableRequirement);
        continue;
      }
      if (cwlResource instanceof CWLInitialWorkDirRequirement) {
        result.add(getFileRequirement(cwlJob, (CWLInitialWorkDirRequirement) cwlResource));
        continue;
      }
      result.add(new CustomRequirement(cwlResource.getType(), cwlResource.getRaw()));
    }
    return result;
  }
  

  @Override
  public ResourceRequirement getResourceRequirement(Job job) throws BindingException {
    CWLJob cwlJob = CWLJobHelper.getCWLJob(job);
    
    CWLResourceRequirement cwlResourceRequirement = cwlJob.getApp().getResourceRequirement();

    if (cwlResourceRequirement == null) {
      return null;
    }
    try {
      return new ResourceRequirement(cwlResourceRequirement.getCoresMin(cwlJob), null, cwlResourceRequirement.getRamMin(cwlJob), null, null, null, null);
    } catch (CWLExpressionException e) {
      throw new BindingException(e);
    }
  }

}
