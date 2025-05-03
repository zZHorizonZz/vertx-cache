package io.vertx.cache.memory.impl;

import io.vertx.cache.common.event.CacheEvent;
import io.vertx.cache.common.event.CacheEventManager;
import io.vertx.cache.common.event.DefaultCacheEventManager;
import io.vertx.cache.common.operation.KeyOperation;
import io.vertx.cache.common.operation.ValueOperation;
import io.vertx.cache.common.operation.binary.BinaryOperation;
import io.vertx.cache.common.operation.json.JsonArraySerializer;
import io.vertx.cache.common.operation.json.JsonObjectSerializer;
import io.vertx.cache.common.operation.json.JsonOperation;
import io.vertx.cache.common.operation.number.NumberOperation;
import io.vertx.cache.common.operation.text.StringOperation;
import io.vertx.cache.common.serialization.CacheDeserializer;
import io.vertx.cache.common.serialization.CacheSerializer;
import io.vertx.cache.memory.MemoryCache;
import io.vertx.cache.memory.MemoryCacheOptions;
import io.vertx.cache.memory.impl.operation.MemoryKeyOperation;
import io.vertx.cache.memory.impl.operation.MemoryValueOperation;
import io.vertx.cache.memory.impl.operation.binary.MemoryBinaryOperation;
import io.vertx.cache.memory.impl.operation.json.MemoryJsonOperation;
import io.vertx.cache.memory.impl.operation.number.MemoryDoubleOperation;
import io.vertx.cache.memory.impl.operation.number.MemoryLongOperation;
import io.vertx.cache.memory.impl.operation.text.MemoryStringOperation;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Thread-safe implementation of the Vert.x Cache interface using Vert.x features. This implementation provides an in-memory cache with support for per-key expiration.
 *
 * Thread safety is ensured through the use of ConcurrentHashMap and atomic operations for all cache modifications. This allows the cache to be safely used in multi-threaded
 * environments without external synchronization.
 */
public class MemoryCacheImpl implements MemoryCache {

    private final Vertx vertx;
    private final CacheEventManager eventManager;
    private final ConcurrentMap<Class<?>, MemoryValueOperation<?>> valueOperations;
    private final MemoryKeyOperation keyOperation;
    private final MemoryStringOperation stringOperation;
    private final MemoryLongOperation longOperation;
    private final MemoryDoubleOperation doubleOperation;
    private final MemoryJsonOperation jsonOperation;
    private final MemoryBinaryOperation binaryOperation;

    private final ConcurrentMap<String, CacheEntry<?>> cache;
    private final long defaultTtl;
    private final long cleanupIntervalMillis;
    private Long cleanupTimerId;

    public MemoryCacheImpl(Vertx vertx) {
        this(vertx, new MemoryCacheOptions());
    }

    public MemoryCacheImpl(Vertx vertx, MemoryCacheOptions options) {
        this.vertx = vertx;
        this.defaultTtl = options.getDefaultTtlMillis() > 0 ? options.getDefaultTtlMillis() : 3600000;
        this.cleanupIntervalMillis = options.getCleanupIntervalMillis() > 0 ? options.getCleanupIntervalMillis() : 1000;
        this.cache = new ConcurrentHashMap<>();
        this.eventManager = new DefaultCacheEventManager(vertx);

        this.keyOperation = new MemoryKeyOperation(this);
        this.stringOperation = new MemoryStringOperation(this);
        this.longOperation = new MemoryLongOperation(this);
        this.doubleOperation = new MemoryDoubleOperation(this);
        this.jsonOperation = new MemoryJsonOperation(this);
        this.binaryOperation = new MemoryBinaryOperation(this);

        this.valueOperations = new ConcurrentHashMap<>(Map.of(
                JsonObject.class, new MemoryValueOperation<>(this, JsonObject.class, new JsonObjectSerializer(), new JsonObjectSerializer()),
                JsonArray.class, new MemoryValueOperation<>(this, JsonArray.class, new JsonArraySerializer(), new JsonArraySerializer()))
        );

        this.cleanupTimerId = vertx.setPeriodic(cleanupIntervalMillis, id -> checkExpiredEntries());
    }

    private void checkExpiredEntries() {
        List<String> expiredKeys = cache.entrySet().stream()
                .filter(entry -> entry.getValue().isExpired())
                .map(Map.Entry::getKey)
                .toList();

        expiredKeys.forEach(key -> {
            if (cache.remove(key) != null) {
                eventManager.publishEvent(CacheEvent.EventType.KEY_DELETED, key);
            }
        });
    }

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public Set<String> keySet() {
        return cache.keySet();
    }

    @Override
    public <T> T put(String key, T value) {
        return put(key, value, defaultTtl);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T put(String key, T value, long ttlMillis) {
        CacheEntry<T> newEntry = new CacheEntry<>(key, value, ttlMillis);
        CacheEntry<?> previousEntry = cache.put(key, newEntry);

        if (previousEntry != null) {
            eventManager.publishEvent(CacheEvent.EventType.KEY_UPDATED, key);
            return (T) previousEntry.getValue();
        } else {
            eventManager.publishEvent(CacheEvent.EventType.KEY_CREATED, key);
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        CacheEntry<?> entry = cache.get(key);

        if (entry == null) {
            return null;
        }

        if (entry.isExpired()) {
            if (cache.remove(key, entry)) {
                eventManager.publishEvent(CacheEvent.EventType.KEY_DELETED, key);
            }

            return null;
        }

        return (T) entry.getValue();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T remove(String key) {
        CacheEntry<?> entry = cache.remove(key);
        if (entry != null) {
            eventManager.publishEvent(CacheEvent.EventType.KEY_DELETED, key);
            return (T) entry.getValue();
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T handleExpiration(String key) {
        CacheEntry<?> entry = cache.get(key);
        if (entry != null) {
            if (cache.remove(key, entry)) {
                eventManager.publishEvent(CacheEvent.EventType.KEY_DELETED, key);
                return (T) entry.getValue();
            }

            // If the entry was concurrently modified, try to get the current value
            entry = cache.get(key);
            if (entry != null) {
                return (T) entry.getValue();
            }
        }
        return null;
    }

    @Override
    public KeyOperation keys() {
        return keyOperation;
    }

    @Override
    public StringOperation strings() {
        return stringOperation;
    }

    @Override
    public NumberOperation<Long> integers() {
        return longOperation;
    }

    @Override
    public NumberOperation<Double> floats() {
        return doubleOperation;
    }

    @Override
    public JsonOperation jsonOperation() {
        return jsonOperation;
    }

    @Override
    public BinaryOperation binaryOperation() {
        return binaryOperation;
    }

    @Override
    public <T> ValueOperation<T> value(Class<T> clazz) {
        return value(clazz, null, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ValueOperation<T> value(Class<T> clazz, CacheSerializer<T> serializer, CacheDeserializer<T> deserializer) {
        return (ValueOperation<T>) valueOperations.computeIfAbsent(clazz, c -> new MemoryValueOperation<>(this, clazz, serializer, deserializer));
    }

    @Override
    public Future<Void> clear() {
        cache.clear();
        eventManager.publishEvent(CacheEvent.EventType.CACHE_CLEARED, null);
        return Future.succeededFuture();
    }

    @Override
    public CacheEventManager events() {
        return eventManager;
    }

    @Override
    public Future<Void> close() {
        // Cancel the cleanup timer and clear the cache
        if (cleanupTimerId != null) {
            vertx.cancelTimer(cleanupTimerId);
            cleanupTimerId = null;
        }
        return clear();
    }

    /**
     * Inner class representing a cache entry with TTL support.
     */
    public static class CacheEntry<T> {
        private final String key;
        private final T value;
        private final long expirationTime;

        /**
         * Creates a new cache entry.
         *
         * @param key The key
         * @param value The value
         * @param ttlMillis The TTL in milliseconds
         */
        public CacheEntry(String key, T value, long ttlMillis) {
            this.key = key;
            this.value = value;
            this.expirationTime = ttlMillis > 0 ? System.currentTimeMillis() + ttlMillis : 0;
        }

        /**
         * Gets the key of this entry.
         *
         * @return The key
         */
        public String getKey() {
            return key;
        }

        /**
         * Gets the value of this entry.
         *
         * @return The value
         */
        public T getValue() {
            return value;
        }

        /**
         * Checks if this entry has expired.
         *
         * @return true if the entry has expired, false otherwise
         */
        public boolean isExpired() {
            return expirationTime > 0 && System.currentTimeMillis() > expirationTime;
        }
    }
}
