package org.rabix.engine.memory;

import org.rabix.engine.memory.impl.InMemoryAppRepository;
import org.rabix.engine.memory.impl.InMemoryBackendRepository;
import org.rabix.engine.memory.impl.InMemoryCompletedJobRepository;
import org.rabix.engine.memory.impl.InMemoryContextRecordRepository;
import org.rabix.engine.memory.impl.InMemoryDAGRepository;
import org.rabix.engine.memory.impl.InMemoryEventRepository;
import org.rabix.engine.memory.impl.InMemoryJobRecordRepository;
import org.rabix.engine.memory.impl.InMemoryJobRepository;
import org.rabix.engine.memory.impl.InMemoryLinkRecordRepository;
import org.rabix.engine.memory.impl.InMemoryVariableRecordRepository;
import org.rabix.engine.repository.AppRepository;
import org.rabix.engine.repository.BackendRepository;
import org.rabix.engine.repository.CompletedJobRepository;
import org.rabix.engine.repository.ContextRecordRepository;
import org.rabix.engine.repository.DAGRepository;
import org.rabix.engine.repository.EventRepository;
import org.rabix.engine.repository.JobRecordRepository;
import org.rabix.engine.repository.JobRepository;
import org.rabix.engine.repository.LinkRecordRepository;
import org.rabix.engine.repository.VariableRecordRepository;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class InMemoryRepositoryModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(AppRepository.class).to(InMemoryAppRepository.class).in(Scopes.SINGLETON);
    bind(BackendRepository.class).to(InMemoryBackendRepository.class).in(Scopes.SINGLETON);
    bind(ContextRecordRepository.class).to(InMemoryContextRecordRepository.class).in(Scopes.SINGLETON);
    bind(JobStatsRecordRepository.class).to(InMemoryJobStatsRecordRepository.class).in(Scopes.SINGLETON);
    bind(DAGRepository.class).to(InMemoryDAGRepository.class).in(Scopes.SINGLETON);
    bind(EventRepository.class).to(InMemoryEventRepository.class).in(Scopes.SINGLETON);
    bind(JobRecordRepository.class).to(InMemoryJobRecordRepository.class).in(Scopes.SINGLETON);
    bind(JobRepository.class).to(InMemoryJobRepository.class).in(Scopes.SINGLETON);
    bind(CompletedJobRepository.class).to(InMemoryCompletedJobRepository.class).in(Scopes.SINGLETON);
    bind(LinkRecordRepository.class).to(InMemoryLinkRecordRepository.class).in(Scopes.SINGLETON);
    bind(VariableRecordRepository.class).to(InMemoryVariableRecordRepository.class).in(Scopes.SINGLETON);
  }
  
}
