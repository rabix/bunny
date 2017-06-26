package org.rabix.engine.store.memory;

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
import org.rabix.engine.store.repository.AppRepository;
import org.rabix.engine.store.repository.BackendRepository;
import org.rabix.engine.store.repository.ContextRecordRepository;
import org.rabix.engine.store.repository.DAGRepository;
import org.rabix.engine.store.repository.EventRepository;
import org.rabix.engine.store.repository.IntermediaryFilesRepository;
import org.rabix.engine.store.repository.JobRecordRepository;
import org.rabix.engine.store.repository.JobRepository;
import org.rabix.engine.store.repository.JobStatsRecordRepository;
import org.rabix.engine.store.repository.LinkRecordRepository;
import org.rabix.engine.store.repository.VariableRecordRepository;

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
