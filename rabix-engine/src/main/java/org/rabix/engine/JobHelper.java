package org.rabix.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.helper.URIHelper;
import org.rabix.bindings.model.Application;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.bindings.model.Resources;
import org.rabix.bindings.model.dag.DAGLinkPort;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.SystemEnvironmentHelper;
import org.rabix.common.helper.CloneHelper;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.common.helper.JSONHelper;
import org.rabix.engine.service.AppService;
import org.rabix.engine.service.ContextRecordService;
import org.rabix.engine.service.DAGNodeService;
import org.rabix.engine.service.JobRecordService;
import org.rabix.engine.service.VariableRecordService;
import org.rabix.engine.store.model.ContextRecord;
import org.rabix.engine.store.model.JobRecord;
import org.rabix.engine.store.model.VariableRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class JobHelper {

  @Inject
  private JobRecordService jobRecordService;
  @Inject
  private VariableRecordService variableRecordService;
  @Inject
  private ContextRecordService contextRecordService;
  @Inject
  private DAGNodeService dagNodeService;
  @Inject
  private AppService appService;
  
  private static Logger logger = LoggerFactory.getLogger(JobHelper.class);
  
  public static String generateId() {
    return UUID.randomUUID().toString();
  }
  
  public static JobStatus transformStatus(JobRecord.JobState state) {
    switch (state) {
    case COMPLETED:
      return JobStatus.COMPLETED;
    case FAILED:
      return JobStatus.FAILED;
    case RUNNING:
      return JobStatus.RUNNING;
    case READY:
      return JobStatus.READY;
    case PENDING:
      return JobStatus.PENDING;
    default:
      break;
    }
    return null;
  }
  
  public static JobRecord.JobState transformStatus(JobStatus status) {
    switch (status) {
    case COMPLETED:
      return JobRecord.JobState.COMPLETED;
    case FAILED:
      return JobRecord.JobState.FAILED;
    case RUNNING:
      return JobRecord.JobState.RUNNING;
    case READY:
      return JobRecord.JobState.READY;
    case PENDING:
      return JobRecord.JobState.PENDING;
    case ABORTED:
      return JobRecord.JobState.ABORTED;
    default:
      break;
    }
    return null;
  }
  
  public Set<Job> createReadyJobs(UUID rootId, boolean setResources) {
    Set<Job> jobs = new HashSet<>();
    List<JobRecord> jobRecords = jobRecordService.findReady(rootId);

    if (!jobRecords.isEmpty()) {
      for (JobRecord job : jobRecords) {
        try {
          jobs.add(createReadyJob(job, JobStatus.READY, setResources));
        } catch (BindingException e) {
          logger.debug("Failed to create job", e);
        }
        
      }
    }
    return jobs;
  }
  
  public Job createReadyJob(JobRecord jobRecord, JobStatus status, boolean setResources) throws BindingException {
    Job job = createJob(jobRecord, status, true);
    if (setResources) {
      long numberOfCores;
      long memory;
      if (job.getConfig() != null) {
        numberOfCores = job.getConfig().get("allocatedResources.cpu") != null ? Long.parseLong((String) job.getConfig().get("allocatedResources.cpu")) : SystemEnvironmentHelper.getNumberOfCores();
        memory = job.getConfig().get("allocatedResources.mem") != null ? Long.parseLong((String) job.getConfig().get("allocatedResources.mem")) : SystemEnvironmentHelper.getTotalPhysicalMemorySizeInMB();
      } else {
        numberOfCores = SystemEnvironmentHelper.getNumberOfCores();
        memory = SystemEnvironmentHelper.getTotalPhysicalMemorySizeInMB();
      }
      Resources resources = new Resources(numberOfCores, memory, null, true, null, null, null, null);
      return Job.cloneWithResources(job, resources);
    }
    return job;
  }
  
  public Job createJob(JobRecord job, JobStatus status) throws BindingException {
    return createJob(job, status, getOutputs(job));
  }
  
  public Job createJob(JobRecord job, JobStatus status, Map<String, Object> outputs) throws BindingException {
    Job completedJob;
    if(job.isContainer() || job.isScatterWrapper()) {
      completedJob = createJob(job, status, false);
    }
    else {
      completedJob = createJob(job, status, true);
    }
    return Job.cloneWithOutputs(completedJob, outputs);
  }

  public Job createJob(JobRecord job, JobStatus status, boolean processVariables) throws BindingException {
    DAGNode node = dagNodeService.get(InternalSchemaHelper.normalizeId(job.getId()), job.getRootId(), job.getDagHash());

    
    Map<String, Object> inputs = new HashMap<>();
    
    List<VariableRecord> inputVariables = variableRecordService.find(job.getId(), LinkPortType.INPUT, job.getRootId());
    
    Map<String, Object> preprocesedInputs = new HashMap<>();
    for (VariableRecord inputVariable : inputVariables) {
      Object value = variableRecordService.getValue(inputVariable);
      preprocesedInputs.put(inputVariable.getPortId(), value);
    }
    
    ContextRecord contextRecord = contextRecordService.find(job.getRootId());
    String encodedApp = URIHelper.createDataURI(JSONHelper.writeObject(appService.get(node.getAppHash())));
    
    Job newJob = new Job(job.getExternalId(), job.getParentId(), job.getRootId(), job.getId(), encodedApp, status, null, preprocesedInputs, null, contextRecord.getConfig(), null, null);
    if (processVariables) {
      inputs = processVariables(node, inputVariables, encodedApp, newJob);
    } else {
      inputs = preprocesedInputs;
    }
    return new Job(job.getExternalId(), job.getParentId(), job.getRootId(), job.getId(), encodedApp, status, null, inputs, null, contextRecord.getConfig(), null, null);
  }

  private Map<String, Object> processVariables(DAGNode node, List<VariableRecord> inputVariables, String encodedApp, Job newJob) throws BindingException {

    boolean autoBoxingEnabled = false;   // get from configuration
    
    Application application = appService.get(node.getAppHash());

    Map<String, Object> inputs = new HashMap<>();
    try {
        Bindings bindings = BindingsFactory.create(encodedApp);
        
        for (VariableRecord inputVariable : inputVariables) {
          Object value = CloneHelper.deepCopy(variableRecordService.getValue(inputVariable));
          ApplicationPort port = application.getInput(inputVariable.getPortId());
          if (port == null) {
            continue;
          }
          if (value == null && node.getDefaults() != null) {
            value = node.getDefaults().get(port.getId());
          }
          for (DAGLinkPort p : node.getInputPorts()) {
            if (p.getId().equals(inputVariable.getPortId())) {
              if (p.getTransform() != null) {
                Object transform = p.getTransform();
                if (transform != null) {
                  value = bindings.transformInputs(value, newJob, transform);
                }
              }
            }
          }
          if (port != null && autoBoxingEnabled) {
            if (port.isList() && !(value instanceof List)) {
              List<Object> transformed = new ArrayList<>();
              transformed.add(value);
              value = transformed;
            }
          }
          inputs.put(inputVariable.getPortId(), value);
        }
    } catch (BindingException e) {
      throw new BindingException("Failed to transform inputs", e);
    }
    return inputs;
  }
  
  public Job fillOutputs(Job job) {
    JobRecord jobRecord = jobRecordService.findRoot(job.getRootId());
    Map<String, Object> outputs = getOutputs(jobRecord);
    return Job.cloneWithOutputs(job, outputs);
  }

  private Map<String, Object> getOutputs(JobRecord jobRecord) {
    List<VariableRecord> outputVariables = variableRecordService.find(jobRecord.getId(), LinkPortType.OUTPUT, jobRecord.getRootId());
    
    Map<String, Object> outputs = new HashMap<>();
    for (VariableRecord outputVariable : outputVariables) {
      outputs.put(outputVariable.getPortId(), variableRecordService.getValue(outputVariable));
    }
    return outputs;
  }

}
