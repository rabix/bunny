package org.rabix.engine.helper;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rabix.bindings.helper.FileValueHelper;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.engine.model.LinkRecord;
import org.rabix.engine.service.IntermediaryFilesService;
import org.rabix.engine.service.LinkRecordService;

public class IntermediaryFilesHelper {
  
  public static void handleContainerReady(Job containerJob, LinkRecordService linkRecordService, IntermediaryFilesService intermediaryFilesService, boolean keepInputFiles) {
    Integer increment = 0;
    if(keepInputFiles && containerJob.isRoot()) {
      increment = 1;
    }
    for(Map.Entry<String, Object> entry : containerJob.getInputs().entrySet()) {
      List<FileValue> files = FileValueHelper.getFilesFromValue(entry.getValue());
      if(!files.isEmpty()) {
        Integer count = linkRecordService.findBySource(containerJob.getName(), entry.getKey(), containerJob.getRootId()).size();
        for(FileValue file: files) {
          if(count > 0) {
            intermediaryFilesService.addOrIncrement(containerJob.getRootId(), file, count + increment);
          }
        }
      }
    }
  }
  
  public static void handleJobCompleted(Job job, LinkRecordService linkRecordService, IntermediaryFilesService intermediaryFilesService) {
    if(!job.isRoot()) {
      boolean isScattered = false;
      for (Map.Entry<String, Object> entry : job.getOutputs().entrySet()) {
        List<FileValue> files = FileValueHelper.getFilesFromValue(entry.getValue());
        if (!files.isEmpty()) {
          List<LinkRecord> links = linkRecordService.findBySource(job.getName(), entry.getKey(), job.getRootId());
          Set<String> destinationJobs = new HashSet<String>();
          Integer count = links.size();
          for (LinkRecord link : links) {
            if(destinationJobs.contains(link.getDestinationJobId())) {
              count--;
              continue;
            }
            destinationJobs.add(link.getDestinationJobId());
            if(link.getDestinationJobId().equals(InternalSchemaHelper.getJobIdFromScatteredId(job.getName())) && InternalSchemaHelper.getScatteredNumber(job.getName()) != null) {
              isScattered = true;
              break;
            }
            if(!link.getDestinationJobId().equals(InternalSchemaHelper.ROOT_NAME) && link.getDestinationVarType().equals(LinkPortType.OUTPUT)) {
              count--;
            }
          }
          for (FileValue file : files) {
            if(count > 0) {
              intermediaryFilesService.addOrIncrement(job.getRootId(), file, count);
            }
          }
        }
      }
      if(!isScattered) {
        Set<String> inputs = new HashSet<String>();
        for (Map.Entry<String, Object> entry : job.getInputs().entrySet()) {
          List<FileValue> files = FileValueHelper.getFilesFromValue(entry.getValue());
          for (FileValue file : files) {
            IntermediaryFilesHelper.extractPathsFromFileValue(inputs, file);
          }
        }
        intermediaryFilesService.decrementFiles(job.getRootId(), inputs);
        intermediaryFilesService.handleUnusedFiles(job);
      }
    }
  }
  
  public static void handleJobFailed(Job job, Job rootJob, IntermediaryFilesService intermediaryFilesService, boolean keepInputFiles) {
    Set<String> rootInputs = new HashSet<String>();
    if(keepInputFiles) {
      for(Map.Entry<String, Object> entry : rootJob.getInputs().entrySet()) {
      List<FileValue> files = FileValueHelper.getFilesFromValue(entry.getValue());
        for (FileValue file : files) {
          IntermediaryFilesHelper.extractPathsFromFileValue(rootInputs, file);
        }
      }
    }
    intermediaryFilesService.jobFailed(job.getRootId(), rootInputs);
    intermediaryFilesService.handleUnusedFiles(job);
  }
  
  public static void extractPathsFromFileValue(Set<String> paths, FileValue file) {
    paths.add(file.getPath());
    for(FileValue f: file.getSecondaryFiles()) {
      extractPathsFromFileValue(paths, f);
    }
  }
}
