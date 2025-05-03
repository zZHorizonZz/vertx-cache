package io.vertx.cache.distributed.impl.operation.number;

import io.vertx.cache.common.event.CacheEvent;
import io.vertx.cache.common.operation.number.NumberOperation;
import io.vertx.cache.common.serialization.CacheDeserializer;
import io.vertx.cache.common.serialization.CacheSerializer;
import io.vertx.cache.distributed.impl.DistributedCacheImpl;
import io.vertx.cache.distributed.impl.operation.DistributedValueOperation;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;

public class DistributedLongOperation extends DistributedValueOperation<Long> implements NumberOperation<Long> {

    private static final RedisLongSerializer SERIALIZER = new RedisLongSerializer();

    public DistributedLongOperation(DistributedCacheImpl cache) {
        super(cache, Long.class, SERIALIZER, SERIALIZER);
    }

    @Override
    public Future<Long> increment(String key) {
        return cache.getRedis().incr(cache.prefixKey(key)).compose(response -> {
            cache.events().publishEvent(CacheEvent.EventType.KEY_UPDATED, key);
            return Future.succeededFuture(response.toLong());
        });
    }

    @Override
    public Future<Long> increment(String key, Long amount) {
        return cache.getRedis().incrby(cache.prefixKey(key), amount.toString()).compose(response -> {
            cache.events().publishEvent(CacheEvent.EventType.KEY_UPDATED, key);
            return Future.succeededFuture(response.toLong());
        });
    }

    @Override
    public Future<Long> decrement(String key) {
        return cache.getRedis().decr(cache.prefixKey(key)).compose(response -> {
            cache.events().publishEvent(CacheEvent.EventType.KEY_UPDATED, key);
            return Future.succeededFuture(response.toLong());
        });
    }

    @Override
    public Future<Long> decrement(String key, Long amount) {
        return cache.getRedis().decrby(cache.prefixKey(key), amount.toString()).compose(response -> {
            cache.events().publishEvent(CacheEvent.EventType.KEY_UPDATED, key);
            return Future.succeededFuture(response.toLong());
        });
    }

    private static class RedisLongSerializer implements CacheSerializer<Long>, CacheDeserializer<Long> {
        @Override
        public Long deserialize(Buffer data) {
            if (data == null) {
                return null;
            }

            return Long.parseLong(data.toString());
        }

        @Override
        public Buffer serialize(Long object) {
            if (object == null) {
                return null;
            }

            return Buffer.buffer(object.toString().getBytes());
        }
    }
}
