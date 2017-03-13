package org.rabix.executor.service.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.common.json.BeanSerializer;
import org.rabix.common.json.processor.BeanProcessorException;
import org.rabix.executor.engine.EngineStub;
import org.rabix.executor.engine.EngineStubActiveMQ;
import org.rabix.executor.engine.EngineStubLocal;
import org.rabix.executor.engine.EngineStubRabbitMQ;
import org.rabix.executor.service.ExecutorService;
import org.rabix.transport.backend.Backend;
import org.rabix.transport.backend.impl.BackendActiveMQ;
import org.rabix.transport.backend.impl.BackendLocal;
import org.rabix.transport.backend.impl.BackendRabbitMQ;
import org.rabix.transport.mechanism.TransportPluginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockExecutorServiceImpl implements ExecutorService {

  private final static Logger logger = LoggerFactory.getLogger(MockExecutorServiceImpl.class);
  
  private Configuration configuration;
  private EngineStub<?, ?, ?> engineStub;
  
  private Map<String, Job> cachedJobs = new HashMap<>(); 
  
  @Inject
  public MockExecutorServiceImpl(Configuration configuration) {
    this.configuration = configuration;
  }
  
  @Override
  public void initialize(Backend backend) {
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
      
      String cacheDir = configuration.getString("cache.directory");
      List<File> dirs = getSubdirs(new File(cacheDir));
      
      for (File file : dirs) {
        String filename = file.getName();
        if (file.isDirectory() && filename.startsWith(".") && filename.endsWith(".meta")) {
          File jobFile = new File(file, "job.json");
          Job job = BeanSerializer.deserialize(FileUtils.readFileToString(jobFile), Job.class);
          String name = getName(file);
          name = name.replace("..", ".");
          name = name.substring(0, name.lastIndexOf(".meta"));
          cachedJobs.put(name, job);
        }
      }
    } catch (TransportPluginException e) {
      logger.error("Failed to initialize Executor", e);
      throw new RuntimeException("Failed to initialize Executor", e);
    } catch (BeanProcessorException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  private List<File> getSubdirs(File file) {
    List<File> subdirs = Arrays.asList(file.listFiles(new FileFilter() {
      public boolean accept(File f) {
        return f.isDirectory();
      }
    }));
    subdirs = new ArrayList<File>(subdirs);

    List<File> deepSubdirs = new ArrayList<File>();
    for (File subdir : subdirs) {
      deepSubdirs.addAll(getSubdirs(subdir));
    }
    subdirs.addAll(deepSubdirs);
    return subdirs;
  }
  
  private String getName(File file) {
    if (file.getName().equals("root")) {
      return "root";
    }
    return getName(file.getParentFile()) + "." + file.getName();
  }

  @Override
  public void start(Job job, UUID rootId) {
    Job updatedJob = Job.cloneWithOutputs(job, cachedJobs.get(job.getName()).getOutputs());
    updatedJob = Job.cloneWithStatus(updatedJob, JobStatus.COMPLETED);
    engineStub.send(updatedJob);
    logger.info("Cached Job {} sent.", job.getName());
  }

  @Override
  public void stop(List<UUID> ids, UUID rootId) {
    // TODO Auto-generated method stub
  }

  @Override
  public void free(UUID rootId, Map<String, Object> config) {
    // TODO Auto-generated method stub
  }

  @Override
  public void shutdown(Boolean stopEverything) {
    // TODO Auto-generated method stub
  }

  @Override
  public boolean isRunning(UUID id, UUID rootId) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Map<String, Object> getResult(UUID id, UUID rootId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isStopped() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public JobStatus findStatus(UUID id, UUID rootId) {
    // TODO Auto-generated method stub
    return null;
  }

}
