package org.rabix.engine.memory;

import org.rabix.engine.memory.impl.MemoryAppRepository;
import org.rabix.engine.memory.impl.MemoryBackendRepository;
import org.rabix.engine.memory.impl.MemoryContextRecordRepository;
import org.rabix.engine.memory.impl.MemoryDAGRepository;
import org.rabix.engine.memory.impl.MemoryEventRepository;
import org.rabix.engine.memory.impl.MemoryJobRecordRepository;
import org.rabix.engine.memory.impl.MemoryJobRepository;
import org.rabix.engine.memory.impl.MemoryLinkRecordRepository;
import org.rabix.engine.memory.impl.MemoryVariableRecordRepository;
import org.rabix.engine.repository.TransactionHelper;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import org.skife.jdbi.v2.sqlobject.Transaction;

import com.google.inject.Inject;

public class MemoryRepositoryRegistry extends TransactionHelper {
  
  MemoryAppRepository memoryAppRepository;
  MemoryBackendRepository memoryBackendRepository;
  MemoryContextRecordRepository memoryContextRecordRepository;
  MemoryDAGRepository memoryDAGRepository;
  MemoryEventRepository memoryEventRepository;
  MemoryJobRecordRepository memoryJobRecordRepository;
  MemoryJobRepository memoryJobRepository;
  MemoryLinkRecordRepository memoryLinkRecordRepository;
  MemoryVariableRecordRepository memoryVariableRecordRepository;
  
  @Inject
  public MemoryRepositoryRegistry(MemoryAppRepository memoryAppRepository, MemoryBackendRepository memoryBackendRepository, MemoryContextRecordRepository memoryContextRecordRepository, MemoryDAGRepository memoryDAGRepository, MemoryEventRepository memoryEventRepository, MemoryJobRecordRepository memoryJobRecordRepository, MemoryJobRepository memoryJobRepository, MemoryLinkRecordRepository memoryLinkRecordRepository, MemoryVariableRecordRepository memoryVariableRecordRepository) {
    this.memoryAppRepository = memoryAppRepository;
    this.memoryBackendRepository = memoryBackendRepository;
    this.memoryContextRecordRepository = memoryContextRecordRepository;
    this.memoryDAGRepository = memoryDAGRepository;
    this.memoryEventRepository = memoryEventRepository;
    this.memoryJobRecordRepository = memoryJobRecordRepository;
    this.memoryJobRepository = memoryJobRepository;
    this.memoryLinkRecordRepository = memoryLinkRecordRepository;
    this.memoryVariableRecordRepository = memoryVariableRecordRepository;
  }
  
  public MemoryAppRepository applicationRepository() {
    return memoryAppRepository;
  }
  
  public MemoryBackendRepository backendRepository() {
    return memoryBackendRepository;
  }
  
  public MemoryDAGRepository dagRepository() {
    return memoryDAGRepository;
  }
  
  public MemoryJobRepository jobRepository() {
    return memoryJobRepository;
  }
  
  public MemoryJobRecordRepository jobRecordRepository() {
    return memoryJobRecordRepository;
  }
  
  public MemoryLinkRecordRepository linkRecordRepository() {
    return memoryLinkRecordRepository;
  }
  
  public MemoryVariableRecordRepository variableRecordRepository() {
    return memoryVariableRecordRepository;
  }
  
  public MemoryContextRecordRepository contextRecordRepository() {
    return memoryContextRecordRepository;
  }
  
  public MemoryEventRepository eventRepository() {
    return memoryEventRepository;
  }
  
  public <Result> Result doInTransaction(TransactionCallback<Result> callback) throws Exception {
    return callback.call();
  }

}
