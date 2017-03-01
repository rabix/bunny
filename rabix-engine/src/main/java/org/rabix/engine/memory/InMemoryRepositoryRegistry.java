package org.rabix.engine.memory;

import org.rabix.engine.memory.impl.InMemoryAppRepository;
import org.rabix.engine.memory.impl.InMemoryBackendRepository;
import org.rabix.engine.memory.impl.InMemoryContextRecordRepository;
import org.rabix.engine.memory.impl.InMemoryDAGRepository;
import org.rabix.engine.memory.impl.InMemoryEventRepository;
import org.rabix.engine.memory.impl.InMemoryJobRecordRepository;
import org.rabix.engine.memory.impl.InMemoryJobRepository;
import org.rabix.engine.memory.impl.InMemoryLinkRecordRepository;
import org.rabix.engine.memory.impl.InMemoryVariableRecordRepository;
import org.rabix.engine.repository.TransactionHelper;

import com.google.inject.Inject;

public class InMemoryRepositoryRegistry extends TransactionHelper {
  
  InMemoryAppRepository memoryAppRepository;
  InMemoryBackendRepository memoryBackendRepository;
  InMemoryContextRecordRepository memoryContextRecordRepository;
  InMemoryDAGRepository memoryDAGRepository;
  InMemoryEventRepository memoryEventRepository;
  InMemoryJobRecordRepository memoryJobRecordRepository;
  InMemoryJobRepository memoryJobRepository;
  InMemoryLinkRecordRepository memoryLinkRecordRepository;
  InMemoryVariableRecordRepository memoryVariableRecordRepository;
  
  @Inject
  public InMemoryRepositoryRegistry(InMemoryAppRepository memoryAppRepository, InMemoryBackendRepository memoryBackendRepository, InMemoryContextRecordRepository memoryContextRecordRepository, InMemoryDAGRepository memoryDAGRepository, InMemoryEventRepository memoryEventRepository, InMemoryJobRecordRepository memoryJobRecordRepository, InMemoryJobRepository memoryJobRepository, InMemoryLinkRecordRepository memoryLinkRecordRepository, InMemoryVariableRecordRepository memoryVariableRecordRepository) {
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
  
  public InMemoryAppRepository applicationRepository() {
    return memoryAppRepository;
  }
  
  public InMemoryBackendRepository backendRepository() {
    return memoryBackendRepository;
  }
  
  public InMemoryDAGRepository dagRepository() {
    return memoryDAGRepository;
  }
  
  public InMemoryJobRepository jobRepository() {
    return memoryJobRepository;
  }
  
  public InMemoryJobRecordRepository jobRecordRepository() {
    return memoryJobRecordRepository;
  }
  
  public InMemoryLinkRecordRepository linkRecordRepository() {
    return memoryLinkRecordRepository;
  }
  
  public InMemoryVariableRecordRepository variableRecordRepository() {
    return memoryVariableRecordRepository;
  }
  
  public InMemoryContextRecordRepository contextRecordRepository() {
    return memoryContextRecordRepository;
  }
  
  public InMemoryEventRepository eventRepository() {
    return memoryEventRepository;
  }
  
  public <Result> Result doInTransaction(TransactionCallback<Result> callback) throws Exception {
    return callback.call();
  }

}
