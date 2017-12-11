package org.rabix.engine.store.lru.stats;

import com.google.inject.Inject;
import org.apache.commons.configuration.Configuration;
import org.rabix.engine.store.lru.LRUCache;
import org.rabix.engine.store.lru.dag.DAGCache;
import org.rabix.engine.store.model.JobStatsRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class JobStatsRecordCache extends LRUCache<UUID, JobStatsRecord> {

    private final Logger logger = LoggerFactory.getLogger(DAGCache.class);
    private final static String CACHE_NAME = "JobStatsCache";
    private static int DEFAULT_CACHE_SIZE = 2000;

    @Inject
    public JobStatsRecordCache(Configuration configuration) {
        super(CACHE_NAME, configuration.getInteger("job.stats.cache.size", DEFAULT_CACHE_SIZE));
        logger.debug("{} initialized with size={}", CACHE_NAME, getCacheSize());
    }
}
