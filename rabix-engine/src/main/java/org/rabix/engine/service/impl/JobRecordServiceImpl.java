package org.rabix.engine.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.rabix.bindings.model.dag.DAGLinkPort;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.engine.cache.Cachable;
import org.rabix.engine.cache.Cache;
import org.rabix.engine.cache.CacheItem.Action;
import org.rabix.engine.model.JobRecord;
import org.rabix.engine.model.JobRecord.PortCounter;
import org.rabix.engine.repository.JobRecordRepository;
import org.rabix.engine.service.CacheService;
import org.rabix.engine.service.JobRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class JobRecordServiceImpl implements JobRecordService {

  private final static Logger logger = LoggerFactory.getLogger(JobRecordServiceImpl.class);
  
  public static enum JobState {
    PENDING,
    READY,
    RUNNING,
    COMPLETED,
    FAILED,
    ABORTED
  }

  private CacheService cacheService;
  private JobRecordRepository jobRecordRepository;
  
  @Inject
  public JobRecordServiceImpl(JobRecordRepository jobRecordRepository, CacheService cacheService) {
    this.cacheService = cacheService;
    this.jobRecordRepository = jobRecordRepository;
  }
  
  public void create(JobRecord jobRecord) {
    Cache cache = cacheService.getCache(jobRecord.getRootId(), jobRecord.getCacheEntityName());
    cache.put(jobRecord, Action.INSERT);
  }

  public void delete(UUID rootId) {
  }
  
  public void update(JobRecord jobRecord) {
    Cache cache = cacheService.getCache(jobRecord.getRootId(), jobRecord.getCacheEntityName());
    cache.put(jobRecord, Action.UPDATE);
  }
  
  public List<JobRecord> findReady(UUID rootId) {
    Cache cache = cacheService.getCache(rootId, JobRecord.CACHE_NAME);
    List<Cachable> jobRecords = cache.get(new JobRecord.JobCacheKey(null, rootId));
    List<JobRecord> readyJobRecords = new ArrayList<>();
    for (Cachable jobRecord : jobRecords) {
      if (((JobRecord) jobRecord).isReady()) {
        readyJobRecords.add((JobRecord) jobRecord);
      }
    }
    return readyJobRecords;
  }

  public List<JobRecord> findByParent(UUID parentId, UUID rootId) {
    List<JobRecord> recordsByParent = jobRecordRepository.getByParent(parentId, rootId);

    if (recordsByParent != null) {
      Cache cache = cacheService.getCache(rootId, JobRecord.CACHE_NAME);
      return cache.<JobRecord> merge(recordsByParent, JobRecord.class);
    }
    return recordsByParent;
  }
  
  @Override
  public List<JobRecord> find(UUID rootId, Set<JobState> statuses) {
    List<JobRecord> records = jobRecordRepository.get(rootId, statuses);

    Cache cache = cacheService.getCache(rootId, JobRecord.CACHE_NAME);
    return cache.<JobRecord> merge(records, JobRecord.class);
  }
  
  public JobRecord find(String id, UUID rootId) {
    Cache cache = cacheService.getCache(rootId, JobRecord.CACHE_NAME);
    List<Cachable> records = cache.get(new JobRecord.JobCacheKey(id, rootId));
    if (!records.isEmpty()) {
      return (JobRecord) records.get(0);
    }
    JobRecord record = jobRecordRepository.get(id, rootId);
    cache.put(record, Action.NOOP);
    return record;
  }
  
  public JobRecord findRoot(UUID rootId) {
    Cache cache = cacheService.getCache(rootId, JobRecord.CACHE_NAME);
    List<Cachable> records = cache.get(new JobRecord.JobCacheKey(InternalSchemaHelper.ROOT_NAME, rootId));
    if (!records.isEmpty()) {
      return (JobRecord) records.get(0);
    }
    JobRecord record = jobRecordRepository.getRoot(rootId);
    cache.put(record, Action.NOOP);
    return record;
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
