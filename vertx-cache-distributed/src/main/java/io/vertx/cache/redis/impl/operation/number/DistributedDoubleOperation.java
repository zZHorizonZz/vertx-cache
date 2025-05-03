package io.vertx.cache.redis.impl.operation.number;

import io.vertx.cache.common.event.CacheEvent;
import io.vertx.cache.common.operation.number.NumberOperation;
import io.vertx.cache.common.serialization.CacheDeserializer;
import io.vertx.cache.common.serialization.CacheSerializer;
import io.vertx.cache.redis.impl.DistributedCacheImpl;
import io.vertx.cache.redis.impl.operation.DistributedValueOperation;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;

public class DistributedDoubleOperation extends DistributedValueOperation<Double> implements NumberOperation<Double> {

    private static final RedisDoubleSerializer SERIALIZER = new RedisDoubleSerializer();

    public DistributedDoubleOperation(DistributedCacheImpl cache) {
        super(cache, Double.class, SERIALIZER, SERIALIZER);
    }

    @Override
    public Future<Double> increment(String key) {
        return increment(key, 1.0);
    }

    @Override
    public Future<Double> increment(String key, Double amount) {
        String prefixedKey = cache.prefixKey(key);

        // Use Redis INCRBYFLOAT command
        return cache.getRedis().incrbyfloat(prefixedKey, amount.toString())
                .compose(response -> {
                    cache.events().publishEvent(CacheEvent.EventType.KEY_UPDATED, key);
                    return Future.succeededFuture(Double.parseDouble(response.toString()));
                });
    }

    @Override
    public Future<Double> decrement(String key) {
        return decrement(key, 1.0);
    }

    @Override
    public Future<Double> decrement(String key, Double amount) {
        // Redis doesn't have a DECRBYFLOAT command, so we use INCRBYFLOAT with a negative amount
        return increment(key, -amount);
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