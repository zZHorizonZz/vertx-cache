package io.vertx.cache.it;

import io.vertx.cache.common.Cache;
import io.vertx.cache.memory.MemoryCacheFactory;
import io.vertx.core.Vertx;

public class MemoryCacheTest extends AbstractCacheTest {
    @Override
    protected Cache createCache(Vertx vertx) {
        return MemoryCacheFactory.create(vertx);
    }
}
