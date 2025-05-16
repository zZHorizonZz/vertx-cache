package io.vertx.cache.it.event;

import io.vertx.cache.common.Cache;
import io.vertx.cache.memory.MemoryCache;
import io.vertx.core.Vertx;

public class MemoryEventCacheTest extends EventCacheTest {
    @Override
    protected Cache cache(Vertx vertx) {
        return MemoryCache.create(vertx);
    }
}
