package io.vertx.cache.distributed.impl.operation.number;

import io.vertx.cache.common.event.CacheEvent;
import io.vertx.cache.common.operation.number.NumberOperation;
import io.vertx.cache.common.serialization.CacheDeserializer;
import io.vertx.cache.common.serialization.CacheSerializer;
import io.vertx.cache.distributed.impl.DistributedCacheImpl;
import io.vertx.cache.distributed.impl.operation.DistributedValueOperation;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;

public class DistributedDoubleOperation extends DistributedValueOperation<Double> implements NumberOperation<Double> {

    private static final RedisDoubleSerializer SERIALIZER = new RedisDoubleSerializer();

    public DistributedDoubleOperation(DistributedCacheImpl cache) {
        super(cache, Double.class, SERIALIZER, SERIALIZER);
    }

    @Override
    public Future<Double> increment(String key) {
        return cache.getRedis().incr(cache.prefixKey(key)).compose(response -> {
            cache.events().publishEvent(CacheEvent.EventType.KEY_UPDATED, key);
            return Future.succeededFuture(response.toDouble());
        });
    }

    @Override
    public Future<Double> increment(String key, Double amount) {
        return cache.getRedis().incrbyfloat(cache.prefixKey(key), amount.toString()).compose(response -> {
            cache.events().publishEvent(CacheEvent.EventType.KEY_UPDATED, key);
            return Future.succeededFuture(response.toDouble());
        });
    }

    @Override
    public Future<Double> decrement(String key) {
        return cache.getRedis().decr(cache.prefixKey(key)).compose(response -> {
            cache.events().publishEvent(CacheEvent.EventType.KEY_UPDATED, key);
            return Future.succeededFuture(response.toDouble());
        });
    }

    @Override
    public Future<Double> decrement(String key, Double amount) {
        return cache.getRedis().decrby(cache.prefixKey(key), amount.toString()).compose(response -> {
            cache.events().publishEvent(CacheEvent.EventType.KEY_UPDATED, key);
            return Future.succeededFuture(response.toDouble());
        });
    }

    private static class RedisDoubleSerializer implements CacheSerializer<Double>, CacheDeserializer<Double> {
        @Override
        public Double deserialize(Buffer data) {
            if (data == null) {
                return null;
            }
            return Double.parseDouble(data.toString());
        }

        @Override
        public Buffer serialize(Double value) {
            if (value == null) {
                return null;
            }
            return Buffer.buffer(value.toString());
        }
    }
}