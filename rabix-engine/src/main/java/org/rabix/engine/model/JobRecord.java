package org.rabix.engine.model;

import java.util.ArrayList;
import java.util.List;

import org.rabix.bindings.model.LinkMerge;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.engine.model.scatter.ScatterStrategy;
import org.rabix.engine.service.JobRecordService.JobState;

public class JobRecord {

  private final String id;
  private final String externalId;
  private final String rootId;
  private final String parentId;
  private final boolean master;
  private boolean blocking;
  
  private JobState state;
  
  private List<PortCounter> inputCounters;
  private List<PortCounter> outputCounters;

  private boolean isScattered;                  // it's created from scatter
  private boolean isContainer;                  // it's a container Job
  private boolean isScatterWrapper;             // it's a scatter wrapper

  private int numberOfGlobalInputs = 0;
  private int numberOfGlobalOutputs = 0;
  
  private ScatterStrategy scatterStrategy;
  
  public JobRecord(String rootId, String id, String uniqueId, String parentId, JobState state, boolean isContainer, boolean isScattered, boolean master, boolean blocking) {
    this.id = id;
    this.externalId = uniqueId;
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
  
  public boolean isRoot() {
    return externalId.equals(rootId);
  }
  
  public boolean isBlocking() {
    return blocking;
  }

  public void setBlocking(boolean blocking) {
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

  public boolean isScattered() {
    return isScattered;
  }

  public void setScattered(boolean isScattered) {
    this.isScattered = isScattered;
  }

  public boolean isContainer() {
    return isContainer;
  }

  public void setContainer(boolean isContainer) {
    this.isContainer = isContainer;
  }

  public boolean isScatterWrapper() {
    return isScatterWrapper;
  }

  public void setScatterWrapper(boolean isScatterWrapper) {
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

  public String getExternalId() {
    return externalId;
  }

  public String getRootId() {
    return rootId;
  }

  public String getParentId() {
    return parentId;
  }

  public boolean isMaster() {
    return master;
  }

  public boolean isInputPortReady(String port) {
    for (PortCounter pc : inputCounters) {
      if (pc.port.equals(port)) {
        if (pc.counter == 0) {
          return true;
        }
      }
    }
    return false;
  }
  
  public boolean isOutputPortReady(String port) {
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
  
  public boolean isReady() {
    for (PortCounter portCounter : inputCounters) {
      if (portCounter.counter > 0) {
        return false;
      }
    }
    return true;
  }

  public boolean isCompleted() {
    for (PortCounter portCounter : outputCounters) {
      if (portCounter.counter > 0) {
        return false;
      }
    }
    return true;
  }

  public boolean isScatterPort(String port) {
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
  
  public boolean isInputPortBlocking(DAGNode node, String port) {
    return getInputPortIncoming(port) > 1 && LinkMerge.isBlocking(node.getLinkMerge(port, LinkPortType.INPUT));
  }

  public static class PortCounter {
    public String port;
    public int counter;
    public boolean scatter;
    
    public int incoming;
    
    public int updatedAsSourceCounter = 0;
    public int globalCounter = 0;

    public PortCounter(String port, int counter, boolean scatter) {
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

    public boolean isScatter() {
      return scatter;
    }

    public void setScatter(boolean scatter) {
      this.scatter = scatter;
    }
  }

  @Override
  public String toString() {
    return "JobRecord [id=" + id + ", externalId=" + externalId + ", rootId=" + rootId + ", master=" + master + ", state=" + state + ", inputCounters=" + inputCounters + ", outputCounters=" + outputCounters + ", isScattered=" + isScattered + ", isContainer=" + isContainer + ", isScatterWrapper=" + isScatterWrapper + ", numberOfGlobalInputs=" + numberOfGlobalInputs + ", numberOfGlobalOutputs=" + numberOfGlobalOutputs + ", scatterStrategy=" + scatterStrategy + "]";
  }

}
