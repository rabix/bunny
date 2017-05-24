package org.rabix.executor.service.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.rabix.backend.api.ExecutorService;
import org.rabix.backend.api.engine.EngineStub;
import org.rabix.backend.api.engine.EngineStubActiveMQ;
import org.rabix.backend.api.engine.EngineStubLocal;
import org.rabix.backend.api.engine.EngineStubRabbitMQ;
import org.rabix.bindings.json.JobValuesDeserializer;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.bindings.model.Resources;
import org.rabix.common.helper.CloneHelper;
import org.rabix.common.json.BeanSerializer;
import org.rabix.common.json.processor.BeanProcessorException;
import org.rabix.transport.backend.Backend;
import org.rabix.transport.backend.impl.BackendActiveMQ;
import org.rabix.transport.backend.impl.BackendLocal;
import org.rabix.transport.backend.impl.BackendRabbitMQ;
import org.rabix.transport.mechanism.TransportPluginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class MockExecutorServiceImpl implements ExecutorService {

  private final static Logger logger = LoggerFactory.getLogger(MockExecutorServiceImpl.class);
  
  private Configuration configuration;
  private EngineStub<?, ?, ?> engineStub;
  
  private Map<String, MutableJob> cachedJobs = new HashMap<>(); 
  
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
          MutableJob job = BeanSerializer.deserialize(FileUtils.readFileToString(jobFile), MutableJob.class);
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
    logger.info("Received Job {}", job.getName());
    MutableJob cachedJob = cachedJobs.get(job.getName());
    cachedJob.status = JobStatus.COMPLETED;
    cachedJob.id = job.getId();
    cachedJob.rootId = job.getRootId();
    cachedJob.name = job.getName();

    engineStub.send(cachedJob.createJob());
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
  
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  protected static class MutableJob implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -202012646416646107L;

    @JsonProperty("id")
    private UUID id;
    @JsonProperty("parentId")
    private UUID parentId;
    @JsonProperty("rootId")
    private UUID rootId;
    @JsonProperty("name")
    private String name;
    @JsonProperty("app")
    private String app;
    @JsonProperty("status")
    private JobStatus status;
    @JsonProperty("message")
    private String message;
    @JsonProperty("config")
    private Map<String, Object> config;
    @JsonProperty("inputs")
    @JsonDeserialize(using = JobValuesDeserializer.class)
    private Map<String, Object> inputs;
    @JsonProperty("outputs")
    @JsonDeserialize(using = JobValuesDeserializer.class)
    private Map<String, Object> outputs;
    @JsonProperty("resources")
    private Resources resources;
    
    @JsonProperty("visiblePorts")
    private Set<String> visiblePorts;
    
    @JsonCreator
    public MutableJob(@JsonProperty("id") UUID id,
        @JsonProperty("parentId") UUID parentId,
        @JsonProperty("rootId") UUID rootId,
        @JsonProperty("name") String name,
        @JsonProperty("app") String app, 
        @JsonProperty("status") JobStatus status,
        @JsonProperty("message") String message,
        @JsonProperty("inputs") Map<String, Object> inputs, 
        @JsonProperty("outputs") Map<String, Object> otputs,
        @JsonProperty("config") Map<String, Object> config,
        @JsonProperty("resources") Resources resources,
        @JsonProperty("visiblePorts") Set<String> visiblePorts) {
      this.id = id;
      this.parentId = parentId;
      this.rootId = rootId;
      this.name = name;
      this.app = app;
      this.status = status;
      this.message = message;
      this.inputs = inputs;
      this.outputs = otputs;
      this.resources = resources;
      this.config = config;
      this.visiblePorts = visiblePorts;
    }
    
    @JsonIgnore
    public boolean isRoot() {
      if (id == null) {
        return false;
      }
      return id.equals(rootId);
    }
    
    public UUID getId() {
      return id;
    }
    
    public String getMessage() {
      return message;
    }
    
    public UUID getParentId() {
      return parentId;
    }
    
    public UUID getRootId() {
      return rootId;
    }
    
    public String getName() {
      return name;
    }
    
    public String getApp() {
      return app;
    }
    
    public Resources getResources() {
      return resources;
    }
    
    public Set<String> getVisiblePorts() {
      return visiblePorts;
    }
    
    @SuppressWarnings("unchecked")
    public Map<String, Object> getInputs() {
      try {
        return (Map<String, Object>) CloneHelper.deepCopy(inputs);
      } catch (Exception e) {
        throw new RuntimeException("Failed to clone inputs", e);
      }
    }
    
    @SuppressWarnings("unchecked")
    public Map<String, Object> getOutputs() {
      try {
        return (Map<String, Object>) CloneHelper.deepCopy(outputs);
      } catch (Exception e) {
        throw new RuntimeException("Failed to clone outputs", e);
      }
    }
    
    public JobStatus getStatus() {
      return status;
    }
    
    public Map<String, Object> getConfig() {
      return config;
    }

    public Job createJob() {
      return new Job(id, parentId, rootId, name, app, status, message, inputs, outputs, config, resources, visiblePorts);
    }
    
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((app == null) ? 0 : app.hashCode());
      result = prime * result + ((config == null) ? 0 : config.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + ((inputs == null) ? 0 : inputs.hashCode());
      result = prime * result + ((message == null) ? 0 : message.hashCode());
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      result = prime * result + ((outputs == null) ? 0 : outputs.hashCode());
      result = prime * result + ((parentId == null) ? 0 : parentId.hashCode());
      result = prime * result + ((resources == null) ? 0 : resources.hashCode());
      result = prime * result + ((rootId == null) ? 0 : rootId.hashCode());
      result = prime * result + ((status == null) ? 0 : status.hashCode());
      result = prime * result + ((visiblePorts == null) ? 0 : visiblePorts.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      MutableJob other = (MutableJob) obj;
      if (app == null) {
        if (other.app != null)
          return false;
      } else if (!app.equals(other.app))
        return false;
      if (config == null) {
        if (other.config != null)
          return false;
      } else if (!config.equals(other.config))
        return false;
      if (id == null) {
        if (other.id != null)
          return false;
      } else if (!id.equals(other.id))
        return false;
      if (inputs == null) {
        if (other.inputs != null)
          return false;
      } else if (!inputs.equals(other.inputs))
        return false;
      if (message == null) {
        if (other.message != null)
          return false;
      } else if (!message.equals(other.message))
        return false;
      if (name == null) {
        if (other.name != null)
          return false;
      } else if (!name.equals(other.name))
        return false;
      if (outputs == null) {
        if (other.outputs != null)
          return false;
      } else if (!outputs.equals(other.outputs))
        return false;
      if (parentId == null) {
        if (other.parentId != null)
          return false;
      } else if (!parentId.equals(other.parentId))
        return false;
      if (resources == null) {
        if (other.resources != null)
          return false;
      } else if (!resources.equals(other.resources))
        return false;
      if (rootId == null) {
        if (other.rootId != null)
          return false;
      } else if (!rootId.equals(other.rootId))
        return false;
      if (status != other.status)
        return false;
      if (visiblePorts == null) {
        if (other.visiblePorts != null)
          return false;
      } else if (!visiblePorts.equals(other.visiblePorts))
        return false;
      return true;
    }

    @Override
    public String toString() {
      return "Job [id=" + id + ", parentId=" + parentId + ", rootId=" + rootId + ", name=" + name + ", status=" + status + ", message=" + message + ", config=" + config + ", inputs=" + inputs + ", outputs=" + outputs + "]";
    }
  }

}
