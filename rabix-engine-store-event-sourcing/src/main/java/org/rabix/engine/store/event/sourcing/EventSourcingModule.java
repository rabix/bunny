package org.rabix.engine.store.event.sourcing;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.apache.commons.configuration.Configuration;
import org.rabix.engine.store.memory.InMemoryRepositoryModule;
import org.rabix.engine.store.postgres.jdbi.JDBIRepositoryModule;
import org.rabix.engine.store.postgres.jdbi.JDBIRepositoryRegistry;
import org.rabix.engine.store.repository.*;
import org.skife.jdbi.v2.DBI;

public class EventSourcingModule extends AbstractModule {

    private final InMemoryRepositoryModule inMemoryRepositoryModule;
    private final JDBIRepositoryModule jdbiRepositoryModule;

    public EventSourcingModule (InMemoryRepositoryModule inMemoryRepositoryModule,
                                JDBIRepositoryModule jdbiRepositoryModule) {

        this.inMemoryRepositoryModule = inMemoryRepositoryModule;
        this.jdbiRepositoryModule = jdbiRepositoryModule;
    }

    @Singleton
    @Provides
    public JDBIRepositoryRegistry provideJDBIRepositoryRegistry(Configuration configuration) {
        DBI dbi = jdbiRepositoryModule.provideDBI(configuration);
        return dbi.onDemand(JDBIRepositoryRegistry.class);
    }

    @Provides
    @Singleton
    public EventRepository provideEventRepository(JDBIRepositoryRegistry jdbiRepositoryRegistry) {
        return jdbiRepositoryRegistry.eventRepository();
    }

    @Provides
    @Singleton
    public DAGRepository provideDAGRepository(JDBIRepositoryRegistry jdbiRepositoryRegistry) {
        return jdbiRepositoryRegistry.dagRepository();
    }

    @Provides
    @Singleton
    public AppRepository provideAppRepository(JDBIRepositoryRegistry jdbiRepositoryRegistry) {
        return jdbiRepositoryRegistry.applicationRepository();
    }

    @Provides
    @Singleton
    public BackendRepository provideBackendRepository() {
        return inMemoryRepositoryModule.provideBackendRepository();
    }

    @Provides
    @Singleton
    public JobRepository provideJobRepository() {
        return inMemoryRepositoryModule.provideJobRepository();
    }

    @Provides
    @Singleton
    public JobRecordRepository provideJobRecordRepository() {
        return inMemoryRepositoryModule.provideJobRecordRepository();
    }

    @Provides
    @Singleton
    public LinkRecordRepository provideLinkRecordRepository() {
        return inMemoryRepositoryModule.provideLinkRecordRepository();
    }

    @Provides
    @Singleton
    public VariableRecordRepository provideVariableRecordRepository() {
        return inMemoryRepositoryModule.provideVariableRecordRepository();
    }

    @Provides
    @Singleton
    public ContextRecordRepository provideContextRecordRepository() {
        return inMemoryRepositoryModule.provideContextRecordRepository();
    }

    @Provides
    @Singleton
    public JobStatsRecordRepository provideJobStatsRecordRepository() {
        return inMemoryRepositoryModule.provideJobStatsRecordRepository();
    }

    @Provides
    @Singleton
    public IntermediaryFilesRepository provideIntermediaryFilesRepository() {
        return inMemoryRepositoryModule.provideIntermediaryFilesRepository();
    }

    @Override
    protected void configure() {}
}
