package io.vertx.cache.memory;

import io.vertx.cache.common.event.CacheEvent;
import io.vertx.cache.common.event.CacheEventManager;
import io.vertx.cache.common.event.DefaultCacheEventManager;
import io.vertx.cache.common.operation.KeyOperation;
import io.vertx.cache.common.operation.ValueOperation;
import io.vertx.cache.common.operation.number.NumberOperation;
import io.vertx.cache.common.operation.text.StringOperation;
import io.vertx.cache.common.serialization.CacheDeserializer;
import io.vertx.cache.common.serialization.CacheSerializer;
import io.vertx.cache.common.serialization.JsonArraySerializer;
import io.vertx.cache.common.serialization.JsonObjectSerializer;
import io.vertx.cache.memory.operation.MemoryKeyOperation;
import io.vertx.cache.memory.operation.MemoryValueOperation;
import io.vertx.cache.memory.operation.number.MemoryDoubleOperation;
import io.vertx.cache.memory.operation.number.MemoryLongOperation;
import io.vertx.cache.memory.operation.text.MemoryStringOperation;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Implementation of the Vert.x Cache interface using Vert.x features. This implementation provides an in-memory cache with support for per-key expiration.
 */
public class MemoryCache implements io.vertx.cache.common.Cache {

    private final Vertx vertx;
    private final CacheEventManager eventManager;
    private final ConcurrentMap<Class<?>, MemoryValueOperation<?>> valueOperations;
    private final MemoryKeyOperation keyOperation;
    private final MemoryStringOperation stringOperation;
    private final MemoryLongOperation longOperation;
    private final MemoryDoubleOperation doubleOperation;

    // The main cache storage
    private final ConcurrentMap<String, CacheEntry<?>> cache;

    // Default TTL in milliseconds (1 hour)
    private final long defaultTtl;

    /**
     * Creates a new MemoryCache with default settings. The default settings are: - Default TTL: 1 hour
     *
     * @param vertx The Vertx instance to use
     */
    public MemoryCache(Vertx vertx) {
        this(vertx, 3600000); // 1 hour in milliseconds
    }

    /**
     * Creates a new MemoryCache with custom TTL.
     *
     * @param vertx The Vertx instance to use
     * @param defaultTtlMillis The default TTL in milliseconds
     */
    public MemoryCache(Vertx vertx, long defaultTtlMillis) {
        this.vertx = vertx;
        this.defaultTtl = defaultTtlMillis;
        this.cache = new ConcurrentHashMap<>();
        this.eventManager = new DefaultCacheEventManager(vertx);

        this.keyOperation = new MemoryKeyOperation(this);
        this.stringOperation = new MemoryStringOperation(this);
        this.longOperation = new MemoryLongOperation(this);
        this.doubleOperation = new MemoryDoubleOperation(this);

        this.valueOperations = new ConcurrentHashMap<>(Map.of(
                JsonObject.class, new MemoryValueOperation<>(this, JsonObject.class, new JsonObjectSerializer(), new JsonObjectSerializer()),
                JsonArray.class, new MemoryValueOperation<>(this, JsonArray.class, new JsonArraySerializer(), new JsonArraySerializer()))
        );
    }

    /**
     * Gets the underlying cache map.
     *
     * @return The cache map
     */
    public ConcurrentMap<String, CacheEntry<?>> getCache() {
        return cache;
    }

    /**
     * Gets the Vertx instance used by this cache.
     *
     * @return The Vertx instance
     */
    public Vertx getVertx() {
        return vertx;
    }

    /**
     * Gets the default TTL in milliseconds.
     *
     * @return The default TTL
     */
    public long getDefaultTtl() {
        return defaultTtl;
    }

    /**
     * Puts a value in the cache with the default TTL.
     *
     * @param key The key
     * @param value The value
     * @param <T> The type of the value
     * @return The previous value, or null if there was no previous value
     */
    public <T> T put(String key, T value) {
        return put(key, value, defaultTtl);
    }

    /**
     * Puts a value in the cache with a custom TTL.
     *
     * @param key The key
     * @param value The value
     * @param ttlMillis The TTL in milliseconds
     * @param <T> The type of the value
     * @return The previous value, or null if there was no previous value
     */
    @SuppressWarnings("unchecked")
    public <T> T put(String key, T value, long ttlMillis) {
        // Get the previous entry
        CacheEntry<?> previousEntry = cache.get(key);
        T previousValue = null;

        // If there was a previous entry, cancel its expiration timer and get its value
        if (previousEntry != null) {
            previousEntry.cancelTimer();
            previousValue = (T) previousEntry.getValue();
        }

        // Create a new entry with the specified TTL
        CacheEntry<T> newEntry = new CacheEntry<>(key, value, ttlMillis, this);

        if (previousEntry != null) {
            cache.replace(key, previousEntry, newEntry);
            eventManager.publishEvent(CacheEvent.EventType.KEY_UPDATED, key);
        } else {
            cache.put(key, newEntry);
            eventManager.publishEvent(CacheEvent.EventType.KEY_CREATED, key);
        }

        return previousValue;
    }

    /**
     * Gets a value from the cache.
     *
     * @param key The key
     * @param <T> The expected type of the value
     * @return The value, or null if the key doesn't exist or has expired
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        CacheEntry<?> entry = cache.get(key);
        if (entry == null || entry.isExpired()) {
            if (entry != null) {
                // If the entry exists but is expired, remove it and publish an expiration event
                handleExpiration(key);
            }
            return null;
        }
        return (T) entry.getValue();
    }

    /**
     * Removes a value from the cache.
     *
     * @param key The key
     * @return The removed value, or null if the key didn't exist
     */
    @SuppressWarnings("unchecked")
    public <T> T remove(String key) {
        CacheEntry<?> entry = cache.remove(key);
        if (entry != null) {
            entry.cancelTimer();
            eventManager.publishEvent(CacheEvent.EventType.KEY_DELETED, key);
            return (T) entry.getValue();
        }
        return null;
    }

    /**
     * Handles a key expiration by removing it from the cache and publishing a KEY_EXPIRED event.
     *
     * @param key The key that expired
     * @return The expired value, or null if the key didn't exist
     */
    @SuppressWarnings("unchecked")
    public <T> T handleExpiration(String key) {
        CacheEntry<?> entry = cache.remove(key);
        if (entry != null) {
            entry.cancelTimer();
            eventManager.publishEvent(CacheEvent.EventType.KEY_EXPIRED, key);
            return (T) entry.getValue();
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
    public <T> ValueOperation<T> value(Class<T> clazz) {
        return value(clazz, null, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ValueOperation<T> value(Class<T> clazz, CacheSerializer<T> serializer, CacheDeserializer<T> deserializer) {
        return (ValueOperation<T>) valueOperations.computeIfAbsent(clazz,
                c -> new MemoryValueOperation<>(this, clazz, serializer, deserializer));
    }

    @Override
    public Future<Void> clear() {
        // Cancel all timers and clear the cache
        cache.values().forEach(CacheEntry::cancelTimer);
        cache.clear();
        eventManager.publishEvent(CacheEvent.EventType.CACHE_CLEARED, null);
        return Future.succeededFuture();
    }

    @Override
    public CacheEventManager events() {
        return eventManager;
    }

    @Override
    public void close() throws IOException {
        clear();
    }

    /**
     * Inner class representing a cache entry with TTL support.
     */
    public class CacheEntry<T> {
        private final String key;
        private final T value;
        private final long expirationTime;
        private Long timerId;
        private final MemoryCache cache;

        /**
         * Creates a new cache entry.
         *
         * @param key The key
         * @param value The value
         * @param ttlMillis The TTL in milliseconds
         * @param cache The cache this entry belongs to
         */
        public CacheEntry(String key, T value, long ttlMillis, MemoryCache cache) {
            this.key = key;
            this.value = value;
            this.cache = cache;
            this.expirationTime = System.currentTimeMillis() + ttlMillis;

            // Set up a timer to remove the entry when it expires
            if (ttlMillis > 0) {
                this.timerId = vertx.setTimer(ttlMillis, id -> {
                    cache.handleExpiration(key);
                });
            } else {
                this.timerId = null;
            }
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

        /**
         * Cancels the expiration timer for this entry.
         */
        public void cancelTimer() {
            if (timerId != null) {
                vertx.cancelTimer(timerId);
                timerId = null;
            }
        }
    }
}
