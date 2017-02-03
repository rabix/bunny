package org.rabix.engine.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.rabix.bindings.model.LinkMerge;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.engine.cache.Cachable;
import org.rabix.engine.cache.CacheKey;
import org.rabix.engine.model.scatter.ScatterStrategy;
import org.rabix.engine.service.JobRecordService.JobState;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class JobRecord implements Cachable {

  public final static String CACHE_NAME = "JOB_RECORD";
  
  private String name;
  private UUID id;
  private UUID rootId;
  private UUID parentId;
  private Boolean master;
  private Boolean blocking;
  
  private JobState state;
  
  private List<PortCounter> inputCounters;
  private List<PortCounter> outputCounters;

  private Boolean isScattered = false;                  // it's created from scatter
  private Boolean isContainer = false;                  // it's a container Job
  private Boolean isScatterWrapper = false;             // it's a scatter wrapper

  private int numberOfGlobalInputs = 0;
  private int numberOfGlobalOutputs = 0;
  
  private ScatterStrategy scatterStrategy;
  
  public JobRecord() {
  }
  
  public JobRecord(UUID rootId, String name, UUID uniqueId, UUID parentId, JobState state, Boolean isContainer, Boolean isScattered, Boolean master, Boolean blocking) {
    this.id = id;
    this.rootId = rootId;
    this.parentId = parentId;
    this.state = state;
    this.master = master;
    this.blocking = blocking;
    this.isContainer = isContainer;
    this.isScattered = isScattered;
    this.inputCounters = new ArrayList<>();
    this.outputCounters = new ArrayList<>();
  }
  
  public Boolean isRoot() {
    return id.equals(rootId);
  }
  
  public Boolean isBlocking() {
    return blocking;
  }

  public void setBlocking(Boolean blocking) {
    this.blocking = blocking;
  }

  public JobState getState() {
    return state;
  }

  public void setState(JobState state) {
    this.state = state;
  }

  public List<PortCounter> getInputCounters() {
    return inputCounters;
  }

  public void setInputCounters(List<PortCounter> inputCounters) {
    this.inputCounters = inputCounters;
  }

  public List<PortCounter> getOutputCounters() {
    return outputCounters;
  }

  public void setOutputCounters(List<PortCounter> outputCounters) {
    this.outputCounters = outputCounters;
  }

  public Boolean isScattered() {
    return isScattered;
  }

  public void setScattered(Boolean isScattered) {
    this.isScattered = isScattered;
  }

  public Boolean isContainer() {
    return isContainer;
  }

  public void setContainer(Boolean isContainer) {
    this.isContainer = isContainer;
  }

  public Boolean isScatterWrapper() {
    return isScatterWrapper;
  }

  public void setScatterWrapper(Boolean isScatterWrapper) {
    this.isScatterWrapper = isScatterWrapper;
  }

  public int getNumberOfGlobalInputs() {
    return numberOfGlobalInputs;
  }

  public void setNumberOfGlobalInputs(int numberOfGlobalInputs) {
    this.numberOfGlobalInputs = numberOfGlobalInputs;
  }

  public int getNumberOfGlobalOutputs() {
    return numberOfGlobalOutputs;
  }

  public void setNumberOfGlobalOutputs(int numberOfGlobalOutputs) {
    this.numberOfGlobalOutputs = numberOfGlobalOutputs;
  }

  public ScatterStrategy getScatterStrategy() {
    return scatterStrategy;
  }

  public void setScatterStrategy(ScatterStrategy scatterStrategy) {
    this.scatterStrategy = scatterStrategy;
  }

  public String getName() {
    return name;
  }

  public UUID getId() {
    return id;
  }

  public UUID getRootId() {
    return rootId;
  }

  public UUID getParentId() {
    return parentId;
  }

  public Boolean isMaster() {
    return master;
  }

  public Boolean isInputPortReady(String port) {
    for (PortCounter pc : inputCounters) {
      if (pc.port.equals(port)) {
        if (pc.counter == 0) {
          return true;
        }
      }
    }
    return false;
  }
  
  public Boolean isOutputPortReady(String port) {
    for (PortCounter pc : outputCounters) {
      if (pc.port.equals(port)) {
        if (pc.counter == 0) {
          return true;
        }
      }
    }
    return false;
  }
  
  public PortCounter getInputCounter(String port) {
    for (PortCounter portCounter : inputCounters) {
      if (portCounter.port.equals(port)) {
        return portCounter;
      }
    }
    return null;
  }
  
  public PortCounter getOutputCounter(String port) {
    for (PortCounter portCounter : outputCounters) {
      if (portCounter.port.equals(port)) {
        return portCounter;
      }
    }
    return null;
  }

  public int getInputPortIncoming(String port) {
    for (PortCounter pc : inputCounters) {
      if (pc.port.equals(port)) {
        return pc.incoming;
      }
    }
    return 0;
  }
  
  public int getOutputPortIncoming(String port) {
    for (PortCounter pc : outputCounters) {
      if (pc.port.equals(port)) {
        return pc.incoming;
      }
    }
    return 0;
  }
  
  public Boolean isReady() {
    for (PortCounter portCounter : inputCounters) {
      if (portCounter.counter > 0) {
        return false;
      }
    }
    return true;
  }

  public Boolean isCompleted() {
    for (PortCounter portCounter : outputCounters) {
      if (portCounter.counter > 0) {
        return false;
      }
    }
    return true;
  }

  public Boolean isScatterPort(String port) {
    for (PortCounter portCounter : inputCounters) {
      if (portCounter.port.equals(port)) {
        return portCounter.scatter;
      }
    }
    return false;
  }

  public List<String> getScatterPorts() {
    List<String> result = new ArrayList<>();

    for (PortCounter portCounter : inputCounters) {
      if (portCounter.scatter) {
        result.add(portCounter.port);
      }
    }
    return result;
  }
  
  public Boolean isInputPortBlocking(DAGNode node, String port) {
    return getInputPortIncoming(port) > 1 && LinkMerge.isBlocking(node.getLinkMerge(port, LinkPortType.INPUT));
  }

  @Override
  public String getCacheEntityName() {
    return CACHE_NAME;
  }
  
  @Override
  public CacheKey getCacheKey() {
    return new JobCacheKey(this);
  }

  public static class PortCounter {
    @JsonProperty("port")
    public String port;
    @JsonProperty("counter")
    public int counter;
    @JsonProperty("scatter")
    public Boolean scatter;
    @JsonProperty("incoming")
    public int incoming;
    
    @JsonProperty("updatedAsSourceCounter")
    public int updatedAsSourceCounter = 0;
    @JsonProperty("globalCounter")
    public int globalCounter = 0;

    @JsonCreator
    public PortCounter(@JsonProperty("port") String port, @JsonProperty("counter") int counter, @JsonProperty("scatter") Boolean scatter, @JsonProperty("incoming") int incoming, @JsonProperty("updatedAsSourceCounter") int updatedAsSourceCounter,
        @JsonProperty("globalCounter") int globalCounter) {
      super();
      this.port = port;
      this.counter = counter;
      this.scatter = scatter;
      this.incoming = incoming;
      this.updatedAsSourceCounter = updatedAsSourceCounter;
      this.globalCounter = globalCounter;
    }

    public PortCounter(String port, int counter, Boolean scatter) {
      this.port = port;
      this.counter = counter;
      this.scatter = scatter;
      this.incoming = 0;
    }

    public void increaseIncoming() {
      this.incoming++;
    }
    
    public void updatedAsSource(int value) {
      this.updatedAsSourceCounter = updatedAsSourceCounter + value;
    }
    
    public void setGlobalCounter(int globalCounter) {
      this.globalCounter = globalCounter;
    }
    
    public String getPort() {
      return port;
    }

    public void setPort(String port) {
      this.port = port;
    }

    public int getGlobalCounter() {
      return globalCounter;
    }
    
    public int getCounter() {
      return counter;
    }

    public void setCounter(int counter) {
      this.counter = counter;
    }

    public Boolean isScatter() {
      return scatter;
    }

    public void setScatter(Boolean scatter) {
      this.scatter = scatter;
    }
  }
  
  public static class JobCacheKey implements CacheKey {
    String name;
    UUID root;
    
    public JobCacheKey(JobRecord record) {
      this.name = record.name;
      this.root = record.rootId;
    }
    
    public JobCacheKey(String name, UUID rootId) {
      this.name = name;
      this.root = rootId;
    }
    
    @Override
    public boolean satisfies(CacheKey key) {
      if (key instanceof JobCacheKey) {
        return name.equals(((JobCacheKey) key).name) && root.equals(((JobCacheKey) key).root);
      }
      return false;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      result = prime * result + ((root == null) ? 0 : root.hashCode());
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
      JobCacheKey other = (JobCacheKey) obj;
      if (name == null) {
        if (other.name != null)
          return false;
      } else if (!name.equals(other.name))
        return false;
      if (root == null) {
        if (other.root != null)
          return false;
      } else if (!root.equals(other.root))
        return false;
      return true;
    }

  }

  @Override
  public String toString() {
    return "JobRecord [name=" + name + ", id=" + id + ", rootId=" + rootId + ", master=" + master + ", state=" + state + ", inputCounters=" + inputCounters + ", outputCounters=" + outputCounters + ", isScattered=" + isScattered + ", isContainer=" + isContainer + ", isScatterWrapper=" + isScatterWrapper + ", numberOfGlobalInputs=" + numberOfGlobalInputs + ", numberOfGlobalOutputs=" + numberOfGlobalOutputs + ", scatterStrategy=" + scatterStrategy + "]";
  }

}
