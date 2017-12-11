package org.rabix.engine.store.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.rabix.bindings.model.LinkMerge;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.engine.store.model.scatter.ScatterStrategy;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class JobRecord extends TimestampedModel {

  public static class JobIdRootIdPair {
    final public String id;
    final public UUID rootId;
    
    public JobIdRootIdPair(String id, UUID rootId) {
      this.id = id;
      this.rootId = rootId;
    }
  }

  public static enum JobState {
    PENDING,
    READY,
    RUNNING,
    COMPLETED,
    FAILED,
    ABORTED
  }
  
  public final static String CACHE_NAME = "JOB_RECORD";
  
  private String id;
  private UUID externalId;
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
  
  private String dagHash;
  
  private ScatterStrategy scatterStrategy;
  
  public JobRecord() {
    super(LocalDateTime.now(), LocalDateTime.now());
  }
  
  public JobRecord(UUID rootId, String id, UUID uniqueId, UUID parentId, JobState state, Boolean isContainer, Boolean isScattered, Boolean master, Boolean blocking, String dagCache) {
    this(rootId, id, uniqueId, parentId, state, isContainer, isScattered, master, blocking, dagCache, LocalDateTime.now(), LocalDateTime.now());
  }

  public JobRecord(UUID rootId, String id, UUID uniqueId, UUID parentId, JobState state, Boolean isContainer, Boolean isScattered, Boolean master, Boolean blocking, String dagCache, LocalDateTime createdAt, LocalDateTime modifiedAt) {
    super(createdAt, modifiedAt);
    this.id = id;
    this.externalId = uniqueId;
    this.rootId = rootId;
    this.parentId = parentId;
    this.state = state;
    this.master = master;
    this.blocking = blocking;
    this.isContainer = isContainer;
    this.isScattered = isScattered;
    this.dagHash = dagCache;
    this.inputCounters = new ArrayList<>();
    this.outputCounters = new ArrayList<>();
  }
  
  public Boolean isRoot() {
    return externalId.equals(rootId);
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

  public String getId() {
    return id;
  }

  public UUID getExternalId() {
    return externalId;
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

  public String getDagHash() {
    return dagHash;
  }

  public void setDagHash(String dagHash) {
    this.dagHash = dagHash;
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

    @Override
    public String toString() {
      return "PortCounter [port=" + port + ", counter=" + counter + ", scatter=" + scatter + ", incoming=" + incoming
          + ", updatedAsSourceCounter=" + updatedAsSourceCounter + ", globalCounter=" + globalCounter + "]";
    }
    
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((rootId == null) ? 0 : rootId.hashCode());
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
    JobRecord other = (JobRecord) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (rootId == null) {
      if (other.rootId != null)
        return false;
    } else if (!rootId.equals(other.rootId))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "JobRecord [id=" + id + ", externalId=" + externalId + ", rootId=" + rootId + ", master=" + master + ", state=" + state + ", inputCounters=" + inputCounters + ", outputCounters=" + outputCounters + ", isScattered=" + isScattered + ", isContainer=" + isContainer + ", isScatterWrapper=" + isScatterWrapper + ", numberOfGlobalInputs=" + numberOfGlobalInputs + ", numberOfGlobalOutputs=" + numberOfGlobalOutputs + ", scatterStrategy=" + scatterStrategy + ", dagCache=" + dagHash + ", createdAt=" + getCreatedAt() + ", modifiedAt="+ getModifiedAt() +"]";
  }

}
