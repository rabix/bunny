package org.rabix.engine.service.impl;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.rabix.bindings.model.dag.DAGLinkPort;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.engine.service.JobRecordService;
import org.rabix.engine.store.model.JobRecord;
import org.rabix.engine.store.model.JobRecord.PortCounter;
import org.rabix.engine.store.repository.JobRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class JobRecordServiceImpl implements JobRecordService {

  private final static Logger logger = LoggerFactory.getLogger(JobRecordServiceImpl.class);
  private JobRecordRepository repo;

  @Inject
  public JobRecordServiceImpl(JobRecordRepository jobRecordRepository) {
    this.repo = jobRecordRepository;
  }

  public void create(JobRecord jobRecord) {
    repo.insert(jobRecord);
  }

  public void delete(UUID rootId) {}

  public void update(JobRecord jobRecord) {
    repo.update(jobRecord);
  }

  public List<JobRecord> findReady(UUID rootId) {
    return repo.getReady(rootId);
  }

  public List<JobRecord> findByParent(UUID parentId, UUID rootId) {
    return repo.getByParent(parentId, rootId);
  }

  @Override
  public List<JobRecord> find(UUID rootId, Set<JobRecord.JobState> statuses) {
    return repo.get(rootId, statuses);
  }

  public JobRecord find(String id, UUID rootId) {
    return repo.get(id, rootId);
  }

  public JobRecord findRoot(UUID rootId) {
    return repo.getRoot(rootId);
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
              } else if (jobRecord.isContainer()) {
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
    List<PortCounter> counters = type.equals(LinkPortType.INPUT) ? jobRecord.getInputCounters() : jobRecord.getOutputCounters();
    for (PortCounter portCounter : counters) {
      if (portCounter.port.equals(portId)) {
        portCounter.counter = portCounter.counter - 1;
      }
    }
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

  public void resetInputPortCounter(JobRecord jobRecord, int value, String port) {
    for (PortCounter pc : jobRecord.getInputCounters()) {
      if (pc.port.equals(port)) {
        if (pc.globalCounter == value) {
          return;
        }
        int oldValue = pc.globalCounter;
        if (pc.globalCounter < value) {
          pc.globalCounter = value;

          if (pc.counter != value) {
            if (pc.counter == 0) {
              continue;
            }
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

  public void resetOutputPortCounter(JobRecord jobRecord, int value, String port) {
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
