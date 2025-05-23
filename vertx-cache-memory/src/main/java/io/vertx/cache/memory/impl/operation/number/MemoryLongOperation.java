package io.vertx.cache.memory.impl.operation.number;

import io.vertx.cache.common.event.CacheEvent;
import io.vertx.cache.common.operation.number.NumberOperation;
import io.vertx.cache.memory.MemoryCache;
import io.vertx.cache.memory.impl.operation.MemoryValueOperation;
import io.vertx.core.Future;

public class MemoryLongOperation extends MemoryValueOperation<Long> implements NumberOperation<Long> {

    public MemoryLongOperation(MemoryCache cache) {
        super(cache, Long.class, null, null);
    }

    @Override
    public Future<Long> increment(String key) {
        return increment(key, 1L);
    }

    @Override
    public Future<Long> increment(String key, Long amount) {
        Long currentValue = cache.get(key);

        Long newValue;
        if (currentValue == null) {
            newValue = amount;
        } else {
            newValue = currentValue + amount;
        }

        cache.put(key, newValue);
        return Future.succeededFuture(newValue);
    }

    @Override
    public Future<Long> decrement(String key) {
        return decrement(key, 1L);
    }

    @Override
    public Future<Long> decrement(String key, Long amount) {
        Long currentValue = cache.get(key);

        long newValue;
        if (currentValue == null) {
            newValue = -amount;
        } else {
            newValue = currentValue - amount;
        }

        cache.put(key, newValue);
        return Future.succeededFuture(newValue);
    }
}
