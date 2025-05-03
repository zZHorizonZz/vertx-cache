package io.vertx.cache.it.operation;

import io.vertx.cache.common.Cache;
import io.vertx.cache.memory.MemoryCache;
import io.vertx.core.Vertx;

public class MemoryOperationCacheTest extends OperationCacheTest {
    @Override
    protected Cache cache(Vertx vertx) {
        return MemoryCache.create(vertx);
    }
}
