package org.rabix.engine.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.rabix.bindings.model.dag.DAGLinkPort;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.engine.model.JobRecord;
import org.rabix.engine.model.JobRecord.PortCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobRecordService {

  private final static Logger logger = LoggerFactory.getLogger(JobRecordService.class);
  
  public static enum JobState {
    PENDING,
    READY,
    RUNNING,
    COMPLETED,
    FAILED
  }

  private ConcurrentMap<String, List<JobRecord>> jobRecordsPerContext = new ConcurrentHashMap<String, List<JobRecord>>();

  public static String generateUniqueId() {
    return UUID.randomUUID().toString();
  }
  
  public void create(JobRecord jobRecord) {
    getJobRecords(jobRecord.getRootId()).add(jobRecord);
  }

  public void delete(String rootId) {
    jobRecordsPerContext.remove(rootId);
  }
  
  public void update(JobRecord jobRecord) {
    for (JobRecord jr : getJobRecords(jobRecord.getRootId())) {
      if (jr.getId().equals(jobRecord.getId())) {
        jr.setState(jobRecord.getState());
        jr.setContainer(jobRecord.isContainer());
        jr.setScattered(jobRecord.isScattered());
        jr.setInputCounters(jobRecord.getInputCounters());
        jr.setOutputCounters(jobRecord.getOutputCounters());
        jr.setScatterWrapper(jobRecord.isScatterWrapper());
        jr.setScatterStrategy(jobRecord.getScatterStrategy());
        return;
      }
    }
  }
  
  public List<JobRecord> find(String contextId) {
    return getJobRecords(contextId);
  }
  
  public List<JobRecord> findReady(String contextId) {
    List<JobRecord> result = new ArrayList<>();
    
    for (JobRecord jr : getJobRecords(contextId)) {
      if (jr.getState().equals(JobState.READY) && jr.getRootId().equals(contextId)) {
        result.add(jr);
      }
    }
    return result;
  }
  
  public List<JobRecord> findByParent(String parentId, String contextId) {
    List<JobRecord> result = new ArrayList<>();

    for (JobRecord jr : getJobRecords(contextId)) {
      if (jr.getParentId() != null && jr.getParentId().equals(parentId)) {
        result.add(jr);
      }
    }
    return result;
  }
  
  public JobRecord find(String id, String contextId) {
    for (JobRecord jr : getJobRecords(contextId)) {
      if (jr.getId().equals(id) && jr.getRootId().equals(contextId)) {
        return jr;
      }
    }
    return null;
  }
  
  public JobRecord findRoot(String contextId) {
    for (JobRecord jr : getJobRecords(contextId)) {
      if (jr.isMaster() && jr.getRootId().equals(contextId)) {
        return jr;
      }
    }
    return null;
  }
  
  private List<JobRecord> getJobRecords(String contextId) {
    List<JobRecord> jobRecordList = jobRecordsPerContext.get(contextId);
    if (jobRecordList == null) {
      jobRecordList = new ArrayList<>();
      jobRecordsPerContext.put(contextId, jobRecordList);
    }
    return jobRecordList;
  }
  
  public void increaseInputPortIncoming(JobRecord jobRecord, String port) {
    for (PortCounter portCounter : jobRecord.getInputCounters()) {
      if (portCounter.port.equals(port)) {
        portCounter.incoming++;
        return;
      }
    }
  }
  
  public void increaseOutputPortIncoming(JobRecord jobRecord, String port) {
    for (PortCounter portCounter : jobRecord.getOutputCounters()) {
      if (portCounter.port.equals(port)) {
        portCounter.incoming++;
        return;
      }
    }
  }
  
  public void incrementPortCounter(JobRecord jobRecord, DAGLinkPort port, LinkPortType type) {
    List<PortCounter> counters = type.equals(LinkPortType.INPUT) ? jobRecord.getInputCounters() : jobRecord.getOutputCounters();

    for (PortCounter pc : counters) {
      if (pc.port.equals(port.getId())) {
        if (type.equals(LinkPortType.INPUT)) {
          pc.counter = pc.counter + 1;
        } else {
          if (pc.updatedAsSourceCounter > 0) {
            pc.updatedAsSourceCounter = pc.updatedAsSourceCounter--;
            return;
          } else { 
            if (type.equals(LinkPortType.OUTPUT)) {
              if (jobRecord.isScatterWrapper()) {
                pc.counter = pc.counter + 1;
              } else if (jobRecord.isContainer() && pc.incoming > 1) {
                pc.counter = pc.counter + 1;
              }
            }
          }
        }
        return;
      }
    }
    PortCounter portCounter = new PortCounter(port.getId(), 1, port.isScatter());
    counters.add(portCounter);
  }
  
  public void decrementPortCounter(JobRecord jobRecord, String portId, LinkPortType type) {
    logger.info("JobRecord {}. Decrementing port {}.", jobRecord.getId(), portId);
    List<PortCounter> counters = type.equals(LinkPortType.INPUT) ? jobRecord.getInputCounters() : jobRecord.getOutputCounters();
    for (PortCounter portCounter : counters) {
      if (portCounter.port.equals(portId)) {
        portCounter.counter = portCounter.counter - 1;
      }
    }
    printInputPortCounters(jobRecord);
    printOutputPortCounters(jobRecord);
  }
  
  private void printInputPortCounters(JobRecord jobRecord) {
    StringBuilder builder = new StringBuilder("\nJob ").append(jobRecord.getId()).append(" input counters:\n");
    for (PortCounter inputPortCounter : jobRecord.getInputCounters()) {
      builder.append(" -- Input port ").append(inputPortCounter.getPort()).append(", counter=").append(inputPortCounter.counter).append("\n");
    }
    logger.debug(builder.toString());
  }
  
  private void printOutputPortCounters(JobRecord jobRecord) {
    StringBuilder builder = new StringBuilder("\nJob ").append(jobRecord.getId()).append(" output counters:\n");
    for (PortCounter inputPortCounter : jobRecord.getOutputCounters()) {
      builder.append(" -- Output port ").append(inputPortCounter.getPort()).append(", counter=").append(inputPortCounter.counter).append("\n");
    }
    logger.debug(builder.toString());
  }
  
  public void resetInputPortCounters(JobRecord jobRecord, int value) {
    if (jobRecord.getNumberOfGlobalInputs() == value) {
      return;
    }
    int oldValue = jobRecord.getNumberOfGlobalInputs();
    if (jobRecord.getNumberOfGlobalInputs() < value) {
      jobRecord.setNumberOfGlobalInputs(value);

      for (PortCounter pc : jobRecord.getInputCounters()) {
        if (pc.counter != value) {
          if (pc.counter == 0) {
            continue;
          }
          if (oldValue != 0) {
            pc.counter = jobRecord.getNumberOfGlobalInputs() - (oldValue - pc.counter);
          } else {
            pc.counter = jobRecord.getNumberOfGlobalInputs();
          }
        }
      }
    }
  }
  
  public void resetOutputPortCounter(JobRecord jobRecord, int value, String port) {
    logger.info("Reset output port counter {} for {} to {}", port, jobRecord.getId(), value);
    for (PortCounter pc : jobRecord.getOutputCounters()) {
      if (pc.port.equals(port)) {
        int oldValue = pc.globalCounter;
        if (pc.globalCounter < value) {
          pc.globalCounter = value;

          if (pc.counter == 0) {
            continue;
          }
          if (pc.counter != value) {
            if (oldValue != 0) {
              pc.counter = pc.globalCounter - (oldValue - pc.counter);
            } else {
              pc.counter = pc.globalCounter;
            }
          }
        }
      }
    }
  }
  
  public void resetOutputPortCounters(JobRecord jobRecord, int value) {
    logger.info("Reset output port counters for {} to {}", jobRecord.getId(), value);
    if (jobRecord.getNumberOfGlobalOutputs() == value) {
      return;
    }
    int oldValue = jobRecord.getNumberOfGlobalOutputs();
    if (jobRecord.getNumberOfGlobalOutputs() < value) {
      jobRecord.setNumberOfGlobalOutputs(value);

      for (PortCounter pc : jobRecord.getOutputCounters()) {
        if (pc.counter == 0) {
          continue;
        }
        if (pc.counter != value) {
          if (oldValue != 0) {
            pc.counter = jobRecord.getNumberOfGlobalOutputs() - (oldValue - pc.counter);
          } else {
            pc.counter = jobRecord.getNumberOfGlobalOutputs();
          }
        }
      }
    }
  }
  
}
