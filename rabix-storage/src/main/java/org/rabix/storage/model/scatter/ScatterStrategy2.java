package org.rabix.storage.model.scatter;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public interface ScatterStrategy2 {

  void enable(String port, Object value, Integer position, Integer sizePerPort) throws ScatterStrategyException;

  void commit(List<RowMapping> mappings);
  
  int enabledCount();
  
  boolean isBlocking();
  
  List<RowMapping> enabled() throws ScatterStrategyException;
  
  LinkedList<Object> values(VariableFinder variableRecordService, String jobId, String portId, UUID rootId);

  List<Object> shape();
  
  void setEmptyListDetected();
  
  boolean isEmptyListDetected();
  
  boolean isHanging();
  
  Object generateOutputsForEmptyList();
  
  void skipScatter(boolean skip);
  
  boolean skipScatter();
  
  class ScatterStrategyPortValue {
    private String jobId;
    private String portId;

    public ScatterStrategyPortValue(String jobId, String portId) {
      this.jobId = jobId;
      this.portId = portId;
    }
    
    public String getJobId() {
      return jobId;
    }
    
    public String getPortId() {
      return portId;
    }

    @Override
    public String toString() {
      return "ScatterStrategyPortValue [jobId=" + jobId + ", portId=" + portId + "]";
    }
    
  }
  
}
