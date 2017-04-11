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
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.bindings.model.dag.DAGLinkPort;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.helper.CloneHelper;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.common.logging.DebugAppender;
import org.rabix.engine.db.AppDB;
import org.rabix.engine.db.DAGNodeDB;
import org.rabix.engine.model.ContextRecord;
import org.rabix.engine.model.JobRecord;
import org.rabix.engine.model.JobRecord.PortCounter;
import org.rabix.engine.model.LinkRecord;
import org.rabix.engine.model.VariableRecord;
import org.rabix.engine.service.ContextRecordService;
import org.rabix.engine.service.JobRecordService;
import org.rabix.engine.service.LinkRecordService;
import org.rabix.engine.service.VariableRecordService;
import org.rabix.engine.service.impl.JobRecordServiceImpl.JobState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobHelper {

  private static Logger logger = LoggerFactory.getLogger(JobHelper.class);
  
  public static String generateId() {
    return UUID.randomUUID().toString();
  }
  
  public static JobStatus transformStatus(JobState state) {
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
  
  public static JobState transformStatus(JobStatus status) {
    switch (status) {
    case COMPLETED:
      return JobState.COMPLETED;
    case FAILED:
      return JobState.FAILED;
    case RUNNING:
      return JobState.RUNNING;
    case READY:
      return JobState.READY;
    case PENDING:
      return JobState.PENDING;
    case ABORTED:
      return JobState.ABORTED;
    default:
      break;
    }
    return null;
  }
  
  public static Set<Job> createReadyJobs(JobRecordService jobRecordService, VariableRecordService variableRecordService, LinkRecordService linkRecordService, ContextRecordService contextRecordService, DAGNodeDB dagNodeDB, AppDB appDB, UUID rootId) {
    Set<Job> jobs = new HashSet<>();
    List<JobRecord> jobRecords = jobRecordService.findReady(rootId);

    if (!jobRecords.isEmpty()) {
      for (JobRecord job : jobRecords) {
        try {
          jobs.add(createReadyJob(job, JobStatus.READY, jobRecordService, variableRecordService, linkRecordService, contextRecordService, dagNodeDB, appDB));
        } catch (BindingException e) {
          logger.debug("Failed to create job", e);
        }
        
      }
    }
    return jobs;
  }
  
  public static Job createReadyJob(JobRecord job, JobStatus status, JobRecordService jobRecordService, VariableRecordService variableRecordService, LinkRecordService linkRecordService, ContextRecordService contextRecordService, DAGNodeDB dagNodeDB, AppDB appDB) throws BindingException {
    return createJob(job, status, jobRecordService, variableRecordService, linkRecordService, contextRecordService, dagNodeDB, appDB, true);
  }
  
  public static Job createCompletedJob(JobRecord job, JobStatus status, JobRecordService jobRecordService, VariableRecordService variableRecordService, LinkRecordService linkRecordService, ContextRecordService contextRecordService, DAGNodeDB dagNodeDB, AppDB appDB) throws BindingException {
    Job completedJob;
    if(job.isContainer() || job.isScatterWrapper()) {
      completedJob = createJob(job, status, jobRecordService, variableRecordService, linkRecordService, contextRecordService, dagNodeDB, appDB, false);
    }
    else {
      completedJob = createJob(job, status, jobRecordService, variableRecordService, linkRecordService, contextRecordService, dagNodeDB, appDB, true);
    }
    List<VariableRecord> outputVariables = variableRecordService.find(job.getId(), LinkPortType.OUTPUT, job.getRootId());
    
    Map<String, Object> outputs = new HashMap<>();
    for (VariableRecord outputVariable : outputVariables) {
      outputs.put(outputVariable.getPortId(), variableRecordService.getValue(outputVariable));
    }
    return Job.cloneWithOutputs(completedJob, outputs);
  }
  
  public static Job createJob(JobRecord job, JobStatus status, JobRecordService jobRecordService, VariableRecordService variableRecordService, LinkRecordService linkRecordService, ContextRecordService contextRecordService, DAGNodeDB dagNodeDB, AppDB appDB, boolean processVariables) throws BindingException {
    DAGNode node = dagNodeDB.get(InternalSchemaHelper.normalizeId(job.getId()), job.getRootId(), job.getDagHash());

    boolean autoBoxingEnabled = false;   // get from configuration
    
    DebugAppender inputsLogBuilder = new DebugAppender(logger);

    inputsLogBuilder.append("\n ---- JobRecord ", job.getId(), "\n");
    
    Map<String, Object> inputs = new HashMap<>();
    
    List<VariableRecord> inputVariables = variableRecordService.find(job.getId(), LinkPortType.INPUT, job.getRootId());
    
    Map<String, Object> preprocesedInputs = new HashMap<>();
    for (VariableRecord inputVariable : inputVariables) {
      Object value = variableRecordService.getValue(inputVariable);
      preprocesedInputs.put(inputVariable.getPortId(), value);
    }
    
    ContextRecord contextRecord = contextRecordService.find(job.getRootId());
    String encodedApp = URIHelper.createDataURI(appDB.get(node.getAppHash()).serialize());
    
    Set<String> visiblePorts = findVisiblePorts(job, jobRecordService, linkRecordService, variableRecordService);
    Job newJob = new Job(job.getExternalId(), job.getParentId(), job.getRootId(), job.getId(), encodedApp, status, null, preprocesedInputs, null, contextRecord.getConfig(), null, visiblePorts);
    try {
      if (processVariables) {
        Bindings bindings = null;
        if (node.getProtocolType() != null) {
          bindings = BindingsFactory.create(node.getProtocolType());
        } else {
          bindings = BindingsFactory.create(encodedApp);
        }
        
        for (VariableRecord inputVariable : inputVariables) {
          Object value = CloneHelper.deepCopy(variableRecordService.getValue(inputVariable));
          ApplicationPort port = appDB.get(node.getAppHash()).getInput(inputVariable.getPortId());
          if (port == null) {
            continue;
          }
          for (DAGLinkPort p : node.getInputPorts()) {
            if (p.getId() == inputVariable.getPortId()) {
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
          inputsLogBuilder.append(" ---- Input ", inputVariable.getPortId(), ", value ", value, "\n");
          inputs.put(inputVariable.getPortId(), value);
        }
      }
      else {
        inputs = preprocesedInputs;
      }
    } catch (BindingException e) {
      throw new BindingException("Failed to transform inputs", e);
    }
    
    logger.debug(inputsLogBuilder.toString());
    return new Job(job.getExternalId(), job.getParentId(), job.getRootId(), job.getId(), encodedApp, status, null, inputs, null, contextRecord.getConfig(), null, visiblePorts);
  }
  
  public static Job createRootJob(JobRecord job, JobStatus status, JobRecordService jobRecordService, VariableRecordService variableRecordService, LinkRecordService linkRecordService, ContextRecordService contextRecordService, DAGNodeDB dagNodeDB, AppDB appDB, Map<String, Object> outputs) {
    DAGNode node = dagNodeDB.get(InternalSchemaHelper.normalizeId(job.getId()), job.getRootId(), job.getDagHash());

    Map<String, Object> inputs = new HashMap<>();
    List<VariableRecord> inputVariables = variableRecordService.find(job.getId(), LinkPortType.INPUT, job.getRootId());
    for (VariableRecord inputVariable : inputVariables) {
      Object value = CloneHelper.deepCopy(variableRecordService.getValue(inputVariable));
      inputs.put(inputVariable.getPortId(), value);
    }
    
    ContextRecord contextRecord = contextRecordService.find(job.getRootId());
    String encodedApp = URIHelper.createDataURI(appDB.get(node.getAppHash()).serialize());
    return new Job(job.getExternalId(), job.getParentId(), job.getRootId(), job.getId(), encodedApp, status, null, inputs, outputs, contextRecord.getConfig(), null, null);
  }
  
  private static Set<String> findVisiblePorts(JobRecord jobRecord, JobRecordService jobRecordService, LinkRecordService linkRecordService, VariableRecordService variableRecordService) {
    Set<String> visiblePorts = new HashSet<>();
    for (PortCounter outputPortCounter : jobRecord.getOutputCounters()) {
      boolean isVisible = isRoot(outputPortCounter.getPort(), jobRecord.getId(), jobRecord.getRootId(), linkRecordService);
      if (isVisible) {
        visiblePorts.add(outputPortCounter.getPort());
      }
    }
    return visiblePorts;
  }
  
  private static boolean isRoot(String portId, String jobId, UUID rootId, LinkRecordService linkRecordService) {
    List<LinkRecord> links = linkRecordService.findBySourceAndDestinationType(jobId, portId, LinkPortType.OUTPUT, rootId);

    for (LinkRecord link : links) {
      if (link.getDestinationJobId().equals(InternalSchemaHelper.ROOT_NAME)) {
        return true;
      } else {
        return isRoot(link.getDestinationJobPort(), link.getDestinationJobId(), rootId, linkRecordService);
      }
    }
    return false;
  }
  
  public static Job fillOutputs(Job job, JobRecordService jobRecordService, VariableRecordService variableRecordService) {
    JobRecord jobRecord = jobRecordService.findRoot(job.getRootId());
    List<VariableRecord> outputVariables = variableRecordService.find(jobRecord.getId(), LinkPortType.OUTPUT, job.getRootId());
    
    Map<String, Object> outputs = new HashMap<>();
    for (VariableRecord outputVariable : outputVariables) {
      outputs.put(outputVariable.getPortId(), variableRecordService.getValue(outputVariable));
    }
    return Job.cloneWithOutputs(job, outputs);
  }

}
