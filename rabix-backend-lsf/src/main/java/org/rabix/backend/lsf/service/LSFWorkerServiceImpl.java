package org.rabix.backend.lsf.service;

import com.genestack.cluster.lsf.LSBOpenJobInfo;
import com.genestack.cluster.lsf.LSFBatch;
import com.genestack.cluster.lsf.LSFBatchException;
import com.genestack.cluster.lsf.model.*;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.NotImplementedException;
import org.rabix.backend.api.WorkerService;
import org.rabix.backend.api.callback.WorkerStatusCallback;
import org.rabix.backend.api.callback.WorkerStatusCallbackException;
import org.rabix.backend.api.engine.EngineStub;
import org.rabix.backend.api.engine.EngineStubActiveMQ;
import org.rabix.backend.api.engine.EngineStubLocal;
import org.rabix.backend.api.engine.EngineStubRabbitMQ;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.mapper.FileMappingException;
import org.rabix.bindings.mapper.FilePathMapper;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.bindings.model.requirement.DockerContainerRequirement;
import org.rabix.bindings.model.requirement.EnvironmentVariableRequirement;
import org.rabix.bindings.model.requirement.Requirement;
import org.rabix.bindings.model.requirement.ResourceRequirement;
import org.rabix.common.helper.ChecksumHelper;
import org.rabix.common.helper.EncodingHelper;
import org.rabix.common.json.processor.BeanProcessorException;
import org.rabix.executor.config.StorageConfiguration;
import org.rabix.transport.backend.Backend;
import org.rabix.transport.backend.impl.BackendActiveMQ;
import org.rabix.transport.backend.impl.BackendLocal;
import org.rabix.transport.backend.impl.BackendRabbitMQ;
import org.rabix.transport.mechanism.TransportPluginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LSFWorkerServiceImpl implements WorkerService {

  private final static Logger logger = LoggerFactory.getLogger(LSFWorkerServiceImpl.class);

  private final static String TYPE = "LSF";

  @Inject
  private Configuration configuration;
  private EngineStub<?, ?, ?> engineStub;

  private ScheduledExecutorService scheduledTaskChecker = Executors.newScheduledThreadPool(1);
  private Map<Job, Long> pendingResults = new ConcurrentHashMap<>();

  @com.google.inject.Inject
  private WorkerStatusCallback statusCallback;
  @com.google.inject.Inject
  private StorageConfiguration storageConfig;

  private JobInfoEntry jobInfo;
  public LSFWorkerServiceImpl() {
  }
  
  @Override
  public void start(Backend backend) {
    try {
      switch (backend.getType()) {
      case LOCAL:
        engineStub = new EngineStubLocal((BackendLocal) backend, this, configuration);
        break;
      case RABBIT_MQ:
        engineStub = new EngineStubRabbitMQ((BackendRabbitMQ) backend, this, configuration);
        break;
      case ACTIVE_MQ:
        engineStub = new EngineStubActiveMQ((BackendActiveMQ) backend, this, configuration);
      default:
        break;
      }
      engineStub.start();
      
    } catch (TransportPluginException e) {
      logger.error("Failed to initialize Executor", e);
      throw new RuntimeException("Failed to initialize Executor", e);
    } catch (BeanProcessorException e) {
      logger.error("Failed to initialize Executor", e);
    }

    this.scheduledTaskChecker.scheduleAtFixedRate(new Runnable() {
      @Override public void run() {
        for (Iterator<Map.Entry<Job, Long>> iterator = pendingResults.entrySet().iterator(); iterator.hasNext(); ) {
          Map.Entry<Job, Long> entry = iterator.next();
          Long lsfJobId = entry.getValue();

          LSFBatch batch = LSFBatch.getInstance();

          batch.readJobInfo(new LSFBatch.JobReader(){
            @Override public boolean readJob(JobInfoEntry jobInfoEntry) {
              jobInfo = jobInfoEntry;
              return false;
            }
          }, lsfJobId, null, null, null, null, LSBOpenJobInfo.ALL_JOB);
          if ((jobInfo.status & LSBJobStates.JOB_STAT_DONE) == LSBJobStates.JOB_STAT_DONE) {
            success(entry.getKey());
            iterator.remove();
          } else if ((jobInfo.status & LSBJobStates.JOB_STAT_EXIT) == LSBJobStates.JOB_STAT_EXIT) {
            fail(entry.getKey());
            iterator.remove();
          }

        }
      }
    }, 0, 1, TimeUnit.SECONDS);
  }
  private void success(Job job) {
    job = Job.cloneWithStatus(job, JobStatus.COMPLETED);
    Bindings bindings = null;
    try {
      bindings = BindingsFactory.create(job);
      job = bindings.postprocess(job, storageConfig.getWorkingDir(job), ChecksumHelper.HashAlgorithm.SHA1, null);
    } catch (BindingException e) {
      e.printStackTrace();
    }
    job = Job.cloneWithMessage(job, "Success");
    try {
      job = statusCallback.onJobCompleted(job);
    } catch (WorkerStatusCallbackException e1) {
      logger.warn("Failed to execute statusCallback: {}", e1);
    }
    engineStub.send(job);
  }

  private void fail(Job job) {
    job = Job.cloneWithStatus(job, JobStatus.FAILED);
    try {
      job = statusCallback.onJobFailed(job);
    } catch (WorkerStatusCallbackException e) {
      logger.warn("Failed to execute statusCallback: {}", e);
    }
    engineStub.send(job);
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
  @Override
  public void submit(Job job, UUID rootId) {

    logger.debug("Received Job {}", job.getName());
    LSFBatch batch = LSFBatch.getInstance();
    SubmitRequest submitRequest = new SubmitRequest();
    Bindings bindings = null;
    try {
      bindings = BindingsFactory.create(job);
      List<Requirement> combinedRequirements = new ArrayList<>();
      combinedRequirements.addAll(bindings.getHints(job));
      combinedRequirements.addAll(bindings.getRequirements(job));
      if (getRequirement(combinedRequirements, DockerContainerRequirement.class) != null) {
        throw new BindingException("Can't run docker tasks");
      }
      job = bindings.preprocess(job, storageConfig.getWorkingDir(job), new FilePathMapper() {
        @Override public String map(String path, Map<String, Object> config) throws FileMappingException {
          return path;
        }
      });

      // Environment variables
      StringBuilder envs = new StringBuilder();
      EnvironmentVariableRequirement env = getRequirement(combinedRequirements, EnvironmentVariableRequirement.class);
      if (env != null) {
        for (String varName: env.getVariables().keySet()) {
          envs.append("export ").append(varName).append("=")
              .append(EncodingHelper.shellQuote(env.getVariables().get(varName))).append(";");
        }
      }

      submitRequest.command = envs + bindings.buildCommandLineObject(job, storageConfig.getWorkingDir(job), new FilePathMapper() {
        @Override public String map(String path, Map<String, Object> config) throws FileMappingException {
          return path;
        }
      }).build();

      submitRequest.cwd = storageConfig.getWorkingDir(job).getAbsolutePath();
      submitRequest.options3 = LSBSubmitOptions3.SUB3_CWD;

      submitRequest.errFile = "job.stderr.log";
      submitRequest.options = LSBSubmitOptions.SUB_ERR_FILE;


      // Resource requirements
      // TODO: Check how to insert getMemRecommendedMB() and getDiskSpaceRecommendedMB()
      ResourceRequirement jobResourceRequirement = bindings.getResourceRequirement(job);
      if (jobResourceRequirement != null) {
        logger.debug("Found resource req");
        String resReq = null;

        if (jobResourceRequirement.getMemMinMB() != null)
          resReq = "mem=" + jobResourceRequirement.getMemMinMB();
        if (jobResourceRequirement.getDiskSpaceMinMB() != null)
          resReq = (resReq != null ? ", " : "") + "swp=" + jobResourceRequirement.getDiskSpaceMinMB();

        if (resReq != null)
          resReq = "rusage[" + resReq + "]";

        if (jobResourceRequirement.getCpuMin() != null)
          resReq = "affinity[core(" + jobResourceRequirement.getCpuMin() + ")] " +
              (resReq != null ? resReq : "");

        if (resReq != null) {
          logger.debug("Generated resReq value: {}", resReq);
          submitRequest.resReq = resReq;
          submitRequest.options3 &= ~LSBSubmitOptions3.SUB3_JOB_REQUEUE;
        }

      }
    } catch (BindingException e) {
      e.printStackTrace();
    }


    SubmitReply reply = null;
    try {
      reply = batch.submit(submitRequest);
      pendingResults.put(job, reply.jobID);
      logger.debug("Submitted job: " + reply.jobID);
    } catch (LSFBatchException e) {
      e.printStackTrace();
    }

  }


  @Override
  public void cancel(List<UUID> ids, UUID contextId) {
    throw new NotImplementedException("This method is not implemented");
  }

  @Override
  public void freeResources(UUID rootId, Map<String, Object> config) {
    throw new NotImplementedException("This method is not implemented");
  }

  @Override
  public void shutdown(Boolean stopEverything) {
    throw new NotImplementedException("This method is not implemented");
  }

  @Override
  public boolean isRunning(UUID id, UUID contextId) {
    throw new NotImplementedException("This method is not implemented");
  }

  @Override
  public Map<String, Object> getResult(UUID id, UUID contextId) {
    throw new NotImplementedException("This method is not implemented");
  }

  @Override
  public boolean isStopped() {
    throw new NotImplementedException("This method is not implemented");
  }

  @Override
  public JobStatus findStatus(UUID id, UUID contextId) {
    throw new NotImplementedException("This method is not implemented");
  }

  @Override
  public String getType() {
    return TYPE;
  }

}
