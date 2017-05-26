package org.rabix.storage.memory;

import org.rabix.storage.memory.impl.InMemoryAppRepository;
import org.rabix.storage.memory.impl.InMemoryBackendRepository;
import org.rabix.storage.memory.impl.InMemoryContextRecordRepository;
import org.rabix.storage.memory.impl.InMemoryDAGRepository;
import org.rabix.storage.memory.impl.InMemoryEventRepository;
import org.rabix.storage.memory.impl.InMemoryIntermediaryFilesRepository;
import org.rabix.storage.memory.impl.InMemoryJobRecordRepository;
import org.rabix.storage.memory.impl.InMemoryJobRepository;
import org.rabix.storage.memory.impl.InMemoryJobStatsRecordRepository;
import org.rabix.storage.memory.impl.InMemoryLinkRecordRepository;
import org.rabix.storage.memory.impl.InMemoryVariableRecordRepository;
import org.rabix.storage.repository.AppRepository;
import org.rabix.storage.repository.BackendRepository;
import org.rabix.storage.repository.ContextRecordRepository;
import org.rabix.storage.repository.DAGRepository;
import org.rabix.storage.repository.EventRepository;
import org.rabix.storage.repository.IntermediaryFilesRepository;
import org.rabix.storage.repository.JobRecordRepository;
import org.rabix.storage.repository.JobRepository;
import org.rabix.storage.repository.JobStatsRecordRepository;
import org.rabix.storage.repository.LinkRecordRepository;
import org.rabix.storage.repository.VariableRecordRepository;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class InMemoryRepositoryModule extends AbstractModule {
  
  public InMemoryRepositoryModule() {
  }
  
  @Override
  protected void configure() {
    bind(AppRepository.class).to(InMemoryAppRepository.class).in(Scopes.SINGLETON);
    bind(BackendRepository.class).to(InMemoryBackendRepository.class).in(Scopes.SINGLETON);
    bind(ContextRecordRepository.class).to(InMemoryContextRecordRepository.class).in(Scopes.SINGLETON);
    bind(IntermediaryFilesRepository.class).to(InMemoryIntermediaryFilesRepository.class).in(Scopes.SINGLETON);
    bind(JobStatsRecordRepository.class).to(InMemoryJobStatsRecordRepository.class).in(Scopes.SINGLETON);
    bind(DAGRepository.class).to(InMemoryDAGRepository.class).in(Scopes.SINGLETON);
    bind(EventRepository.class).to(InMemoryEventRepository.class).in(Scopes.SINGLETON);
    bind(JobRecordRepository.class).to(InMemoryJobRecordRepository.class).in(Scopes.SINGLETON);
    bind(JobRepository.class).to(InMemoryJobRepository.class).in(Scopes.SINGLETON);
    bind(LinkRecordRepository.class).to(InMemoryLinkRecordRepository.class).in(Scopes.SINGLETON);
    bind(VariableRecordRepository.class).to(InMemoryVariableRecordRepository.class).in(Scopes.SINGLETON);
  }
  
}
