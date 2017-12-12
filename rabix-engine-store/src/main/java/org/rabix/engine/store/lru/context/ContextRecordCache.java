package org.rabix.engine.store.lru.context;

import org.apache.commons.configuration.Configuration;
import org.rabix.engine.store.lru.LRUCache;
import org.rabix.engine.store.model.ContextRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.UUID;

public class ContextRecordCache extends LRUCache<UUID, ContextRecord> {

    private static final Logger logger = LoggerFactory.getLogger(ContextRecordCache.class);

    public static final String CACHE_NAME = "ContextRecordCache";
    private static int DEFAULT_CACHE_SIZE = 1000;

    @Inject
    public ContextRecordCache(Configuration configuration) {
        super(CACHE_NAME, configuration.getInteger("cache.context.size", DEFAULT_CACHE_SIZE));
        logger.debug("Initialized {} with size {}", CACHE_NAME, getCacheSize());
    }
}