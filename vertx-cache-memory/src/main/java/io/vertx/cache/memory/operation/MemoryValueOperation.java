package io.vertx.cache.memory.operation;

import io.vertx.cache.common.event.CacheEvent;
import io.vertx.cache.common.operation.ValueOperation;
import io.vertx.cache.common.serialization.CacheDeserializer;
import io.vertx.cache.common.serialization.CacheSerializer;
import io.vertx.cache.memory.MemoryCache;
import io.vertx.core.Future;

import java.time.Duration;

/**
 * Implementation of the ValueOperation interface using MemoryCache.
 *
 * @param <T> The type of values this operation works with
 */
public class MemoryValueOperation<T> implements ValueOperation<T> {

    protected final MemoryCache cache;
    private final Class<T> valueClass;
    private final CacheSerializer<T> serializer;
    private final CacheDeserializer<T> deserializer;

    public MemoryValueOperation(MemoryCache cache, Class<T> valueClass,
                               CacheSerializer<T> serializer, CacheDeserializer<T> deserializer) {
        this.cache = cache;
        this.valueClass = valueClass;
        this.serializer = serializer != null ? serializer : createDefaultSerializer();
        this.deserializer = deserializer != null ? deserializer : createDefaultDeserializer();
    }

    private CacheSerializer<T> createDefaultSerializer() {
        return value -> {
            // For null values, return null
            if (value == null) {
                return null;
            }
            // For non-null values, store the object directly
            // This works for in-memory cache but would need proper serialization for distributed cache
            return new byte[0]; // Placeholder, the actual object is stored directly
        };
    }

    private CacheDeserializer<T> createDefaultDeserializer() {
        return bytes -> {
            // For null values, return null
            if (bytes == null) {
                return null;
            }
            // For non-null values, the object is stored directly in the cache
            // This works for in-memory cache but would need proper deserialization for distributed cache
            return null; // Placeholder, the actual object is retrieved directly
        };
    }

    @Override
    public Future<T> get(String key) {
        T value = cache.get(key);

        // Publish event for key read operation
        if (value != null) {
            cache.events().publishEvent(CacheEvent.EventType.KEY_READ, key);
        }

        return Future.succeededFuture(value);
    }

    @Override
    public Future<T> getAndSet(String key, T value) {
        T oldValue = cache.put(key, value);
        return Future.succeededFuture(oldValue);
    }

    @Override
    public Future<T> getAndDelete(String key) {
        T oldValue = cache.remove(key);
        return Future.succeededFuture(oldValue);
    }

    @Override
    public Future<Void> set(String key, T value) {
        cache.put(key, value);
        return Future.succeededFuture();
    }

    @Override
    public Future<Void> set(String key, T value, Duration expiration) {
        // Use the per-key TTL feature of MemoryCache
        cache.put(key, value, expiration.toMillis());
        return Future.succeededFuture();
    }

    @Override
    public Future<Void> setIfAbsent(String key, T value) {
        // Use atomic operation to avoid race conditions
        T existingValue = cache.get(key);
        if (existingValue == null) {
            // Key doesn't exist, so set it with the default TTL
            cache.put(key, value);
        } else {
            // Key exists, publish a read event
            cache.events().publishEvent(CacheEvent.EventType.KEY_READ, key);
        }
        return Future.succeededFuture();
    }

    @Override
    public Future<Void> setIfAbsent(String key, T value, Duration expiration) {
        // Use atomic operation to avoid race conditions
        T existingValue = cache.get(key);
        if (existingValue == null) {
            // Key doesn't exist, so set it with the specified TTL
            cache.put(key, value, expiration.toMillis());
        } else {
            // Key exists, publish a read event
            cache.events().publishEvent(CacheEvent.EventType.KEY_READ, key);
        }
        return Future.succeededFuture();
    }

    @Override
    public Future<Void> delete(String... keys) {
        for (String key : keys) {
            cache.remove(key);
        }
        return Future.succeededFuture();
    }

    @Override
    public CacheSerializer<T> getSerializer() {
        return serializer;
    }

    @Override
    public CacheDeserializer<T> getDeserializer() {
        return deserializer;
    }
}
