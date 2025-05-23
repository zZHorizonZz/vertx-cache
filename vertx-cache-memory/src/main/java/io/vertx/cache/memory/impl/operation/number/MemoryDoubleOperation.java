package io.vertx.cache.memory.impl.operation.number;

import io.vertx.cache.common.event.CacheEvent;
import io.vertx.cache.common.operation.number.NumberOperation;
import io.vertx.cache.memory.MemoryCache;
import io.vertx.cache.memory.impl.operation.MemoryValueOperation;
import io.vertx.core.Future;

public class MemoryDoubleOperation extends MemoryValueOperation<Double> implements NumberOperation<Double> {

    public MemoryDoubleOperation(MemoryCache cache) {
        super(cache, Double.class, null, null);
    }

    @Override
    public Future<Double> increment(String key) {
        return increment(key, 1.0);
    }

    @Override
    public Future<Double> increment(String key, Double amount) {
        Double currentValue = cache.get(key);

        double newValue;
        if (currentValue == null) {
            newValue = amount;
        } else {
            newValue = currentValue + amount;
        }

        cache.put(key, newValue);
        return Future.succeededFuture(newValue);
    }

    @Override
    public Future<Double> decrement(String key) {
        return decrement(key, 1.0);
    }

    @Override
    public Future<Double> decrement(String key, Double amount) {
        Double currentValue = cache.get(key);

        double newValue;
        if (currentValue == null) {
            newValue = -amount;
        } else {
            newValue = currentValue - amount;
        }

        cache.put(key, newValue);
        return Future.succeededFuture(newValue);
    }
}
