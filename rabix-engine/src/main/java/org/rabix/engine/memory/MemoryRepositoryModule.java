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
import org.rabix.engine.repository.AppRepository;
import org.rabix.engine.repository.BackendRepository;
import org.rabix.engine.repository.ContextRecordRepository;
import org.rabix.engine.repository.DAGRepository;
import org.rabix.engine.repository.EventRepository;
import org.rabix.engine.repository.JobRecordRepository;
import org.rabix.engine.repository.JobRepository;
import org.rabix.engine.repository.LinkRecordRepository;
import org.rabix.engine.repository.VariableRecordRepository;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class MemoryRepositoryModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(AppRepository.class).to(MemoryAppRepository.class).in(Scopes.SINGLETON);
    bind(BackendRepository.class).to(MemoryBackendRepository.class).in(Scopes.SINGLETON);
    bind(ContextRecordRepository.class).to(MemoryContextRecordRepository.class).in(Scopes.SINGLETON);
    bind(DAGRepository.class).to(MemoryDAGRepository.class).in(Scopes.SINGLETON);
    bind(EventRepository.class).to(MemoryEventRepository.class).in(Scopes.SINGLETON);
    bind(JobRecordRepository.class).to(MemoryJobRecordRepository.class).in(Scopes.SINGLETON);
    bind(JobRepository.class).to(MemoryJobRepository.class).in(Scopes.SINGLETON);
    bind(LinkRecordRepository.class).to(MemoryLinkRecordRepository.class).in(Scopes.SINGLETON);
    bind(VariableRecordRepository.class).to(MemoryVariableRecordRepository.class).in(Scopes.SINGLETON);
  }
  
}
