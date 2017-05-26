package org.rabix.storage.memory;

import org.rabix.storage.memory.impl.*;
import org.rabix.storage.repository.TransactionHelper;

import com.google.inject.Inject;
import org.rabix.storage.memory.impl.*;

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
