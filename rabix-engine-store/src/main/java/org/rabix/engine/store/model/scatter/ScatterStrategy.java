package org.rabix.engine.store.model.scatter;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface ScatterStrategy {

  void enable(String port, Object value, Integer position, Integer sizePerPort) throws ScatterStrategyException;

  void commit(List<RowMapping> mappings);
  
  int enabledCount();
  
  boolean isBlocking();
  
  List<RowMapping> enabled() throws ScatterStrategyException;
  
  List<Object> valueStructure(String jobId, String portId, UUID rootId);
  
  void setEmptyListDetected();
  
  boolean isEmptyListDetected();
  
  boolean isHanging();
  
  Object generateOutputsForEmptyList();
  
  void skipScatter(boolean skip);
  
  boolean skipScatter();

  public class JobPortPair {
    private String jobId;
    private String portId;

    public JobPortPair(String jobId, String portId) {
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
      return "JobPortPair [jobId=" + jobId + ", portId=" + portId + "]";
    }
  }
  
}
