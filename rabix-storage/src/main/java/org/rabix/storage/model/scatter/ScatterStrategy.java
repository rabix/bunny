package org.rabix.storage.model.scatter;

import java.util.LinkedList;
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
  
  LinkedList<Object> values(VariableFinder variableRecordService, String jobId, String portId, UUID rootId);

//  List<Integer> shape();
  
  void setEmptyListDetected();
  
  boolean isEmptyListDetected();
  
  boolean isHanging();
  
  Object generateOutputsForEmptyList();
  
  void skipScatter(boolean skip);
  
  boolean skipScatter();
  
}
