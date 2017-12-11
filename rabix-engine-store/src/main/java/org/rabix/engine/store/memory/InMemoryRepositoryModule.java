package org.rabix.engine.store.memory;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.rabix.engine.store.memory.impl.*;
import org.rabix.engine.store.repository.*;

public class InMemoryRepositoryModule extends AbstractModule {

  @Override
  protected void configure() {}

  @Provides
  @Singleton
  public AppRepository provideAppRepository() {
    return new InMemoryAppRepository();
  }

  @Provides
  @Singleton
  public BackendRepository provideBackendRepository() {
    return new InMemoryBackendRepository();
  }

  @Provides
  @Singleton
  public DAGRepository provideDAGRepository() {
    return new InMemoryDAGRepository();
  }

  @Provides
  @Singleton
  public JobRepository provideJobRepository() {
    return new InMemoryJobRepository();
  }

  @Provides
  @Singleton
  public EventRepository provideEventRepository() {
    return new InMemoryEventRepository();
  }

  @Provides
  @Singleton
  public JobRecordRepository provideJobRecordRepository() {
    return new InMemoryJobRecordRepository();
  }

  @Provides
  @Singleton
  public LinkRecordRepository provideLinkRecordRepository() {
    return new InMemoryLinkRecordRepository();
  }

  @Provides
  @Singleton
  public VariableRecordRepository provideVariableRecordRepository() {
    return new InMemoryVariableRecordRepository();
  }

  @Provides
  @Singleton
  public ContextRecordRepository provideContextRecordRepository() {
    return new InMemoryContextRecordRepository();
  }

  @Provides
  @Singleton
  public JobStatsRecordRepository provideJobStatsRecordRepository() {
    return new InMemoryJobStatsRecordRepository();
  }

  @Provides
  @Singleton
  public IntermediaryFilesRepository provideIntermediaryFilesRepository() {
    return new InMemoryIntermediaryFilesRepository();
  }

}
