package org.rabix.engine.store.memory;

import org.rabix.engine.store.repository.TransactionHelper;
import org.rabix.engine.store.memory.impl.InMemoryAppRepository;
import org.rabix.engine.store.memory.impl.InMemoryBackendRepository;
import org.rabix.engine.store.memory.impl.InMemoryContextRecordRepository;
import org.rabix.engine.store.memory.impl.InMemoryDAGRepository;
import org.rabix.engine.store.memory.impl.InMemoryEventRepository;
import org.rabix.engine.store.memory.impl.InMemoryIntermediaryFilesRepository;
import org.rabix.engine.store.memory.impl.InMemoryJobRecordRepository;
import org.rabix.engine.store.memory.impl.InMemoryJobRepository;
import org.rabix.engine.store.memory.impl.InMemoryJobStatsRecordRepository;
import org.rabix.engine.store.memory.impl.InMemoryLinkRecordRepository;
import org.rabix.engine.store.memory.impl.InMemoryVariableRecordRepository;

import com.google.inject.Inject;

public class InMemoryRepositoryRegistry extends TransactionHelper {
  
  InMemoryAppRepository memoryAppRepository;
  InMemoryBackendRepository memoryBackendRepository;
  InMemoryContextRecordRepository memoryContextRecordRepository;
  InMemoryJobStatsRecordRepository memoryJobStatsRecordRepository;
  InMemoryDAGRepository memoryDAGRepository;
  InMemoryEventRepository memoryEventRepository;
  InMemoryJobRecordRepository memoryJobRecordRepository;
  InMemoryJobRepository memoryJobRepository;
  InMemoryLinkRecordRepository memoryLinkRecordRepository;
  InMemoryVariableRecordRepository memoryVariableRecordRepository;
  InMemoryIntermediaryFilesRepository memoryIntermediaryFilesRepository;
  
  @Inject
  public InMemoryRepositoryRegistry(InMemoryAppRepository memoryAppRepository, InMemoryBackendRepository memoryBackendRepository, InMemoryContextRecordRepository memoryContextRecordRepository, InMemoryDAGRepository memoryDAGRepository, InMemoryEventRepository memoryEventRepository, InMemoryJobRecordRepository memoryJobRecordRepository, InMemoryJobRepository memoryJobRepository, InMemoryLinkRecordRepository memoryLinkRecordRepository, InMemoryVariableRecordRepository memoryVariableRecordRepository, InMemoryJobStatsRecordRepository memoryJobStatsRecordRepository, InMemoryIntermediaryFilesRepository memoryIntermediaryFilesRepository) {
    this.memoryAppRepository = memoryAppRepository;
    this.memoryBackendRepository = memoryBackendRepository;
    this.memoryContextRecordRepository = memoryContextRecordRepository;
    this.memoryDAGRepository = memoryDAGRepository;
    this.memoryEventRepository = memoryEventRepository;
    this.memoryJobRecordRepository = memoryJobRecordRepository;
    this.memoryJobRepository = memoryJobRepository;
    this.memoryLinkRecordRepository = memoryLinkRecordRepository;
    this.memoryVariableRecordRepository = memoryVariableRecordRepository;
    this.memoryJobStatsRecordRepository = memoryJobStatsRecordRepository;
    this.memoryIntermediaryFilesRepository = memoryIntermediaryFilesRepository;
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

  public InMemoryJobStatsRecordRepository jobStatsRecordRepository() {
    return memoryJobStatsRecordRepository;
  }

  public InMemoryEventRepository eventRepository() {
    return memoryEventRepository;
  }
  
  public InMemoryIntermediaryFilesRepository intermediaryFilesRepository() {
    return memoryIntermediaryFilesRepository;
  }
  
  public <Result> Result doInTransaction(TransactionCallback<Result> callback) throws Exception {
    return callback.call();
  }

}
