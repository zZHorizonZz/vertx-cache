package io.vertx.cache.memory.operation.number;

import io.vertx.cache.common.event.CacheEvent;
import io.vertx.cache.common.operation.number.NumberOperation;
import io.vertx.cache.memory.MemoryCache;
import io.vertx.cache.memory.operation.MemoryValueOperation;
import io.vertx.core.Future;

/**
 * Implementation of the NumberOperation interface for Long values using MemoryCache.
 */
public class MemoryLongOperation extends MemoryValueOperation<Long> implements NumberOperation<Long> {

    /**
     * Creates a new MemoryLongOperation.
     *
     * @param cache The MemoryCache to use
     */
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

        // Publish event for key read operation
        if (currentValue != null) {
            cache.events().publishEvent(CacheEvent.EventType.KEY_READ, key);
        }

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

        // Publish event for key read operation
        if (currentValue != null) {
            cache.events().publishEvent(CacheEvent.EventType.KEY_READ, key);
        }

        Long newValue;
        if (currentValue == null) {
            newValue = -amount;
        } else {
            newValue = currentValue - amount;
        }

        cache.put(key, newValue);
        return Future.succeededFuture(newValue);
    }
}
