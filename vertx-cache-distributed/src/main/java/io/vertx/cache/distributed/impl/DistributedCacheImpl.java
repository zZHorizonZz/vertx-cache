package io.vertx.cache.distributed.impl;

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
import io.vertx.cache.distributed.DistributedCache;
import io.vertx.cache.distributed.DistributedCacheOptions;
import io.vertx.cache.distributed.impl.operation.DistributedKeyOperation;
import io.vertx.cache.distributed.impl.operation.DistributedValueOperation;
import io.vertx.cache.distributed.impl.operation.binary.DistributedBinaryOperation;
import io.vertx.cache.distributed.impl.operation.json.DistributedJsonOperation;
import io.vertx.cache.distributed.impl.operation.number.DistributedDoubleOperation;
import io.vertx.cache.distributed.impl.operation.number.DistributedLongOperation;
import io.vertx.cache.distributed.impl.operation.text.DistributedStringOperation;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Implementation of the Vert.x Cache interface using Redis. This implementation provides a distributed cache with Redis as the backend.
 */
public class DistributedCacheImpl implements DistributedCache {

    private final Vertx vertx;
    private final Redis redisClient;
    private final RedisAPI redis;
    private final CacheEventManager eventManager;
    private final ConcurrentMap<Class<?>, DistributedValueOperation<?>> valueOperations;
    private final DistributedKeyOperation keyOperation;
    private final DistributedStringOperation stringOperation;
    private final DistributedLongOperation longOperation;
    private final DistributedDoubleOperation doubleOperation;
    private final DistributedJsonOperation jsonOperation;
    private final DistributedBinaryOperation binaryOperation;

    private final long defaultTtl;
    private final String keyPrefix;

    public DistributedCacheImpl(Vertx vertx) {
        this(vertx, new DistributedCacheOptions());
    }

    public DistributedCacheImpl(Vertx vertx, DistributedCacheOptions options) {
        this(vertx, Redis.createClient(vertx, options.getRedisOptions()), options);
    }

    public DistributedCacheImpl(Vertx vertx, Redis redisClient, DistributedCacheOptions options) {
        this.vertx = vertx;
        this.redisClient = redisClient;
        this.redis = RedisAPI.api(redisClient);
        this.defaultTtl = options.getDefaultTtlMillis();
        this.keyPrefix = options.getKeyPrefix();
        this.eventManager = new DefaultCacheEventManager(vertx);

        this.keyOperation = new DistributedKeyOperation(this);
        this.stringOperation = new DistributedStringOperation(this);
        this.longOperation = new DistributedLongOperation(this);
        this.doubleOperation = new DistributedDoubleOperation(this);
        this.jsonOperation = new DistributedJsonOperation(this);
        this.binaryOperation = new DistributedBinaryOperation(this);

        this.valueOperations = new ConcurrentHashMap<>(Map.of(
                JsonObject.class, new DistributedValueOperation<>(this, JsonObject.class, new JsonObjectSerializer(), new JsonObjectSerializer()),
                JsonArray.class, new DistributedValueOperation<>(this, JsonArray.class, new JsonArraySerializer(), new JsonArraySerializer()))
        );
    }

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public Redis getRedisClient() {
        return redisClient;
    }

    /**
     * Gets the Redis API for this cache.
     *
     * @return The Redis API
     */
    public RedisAPI getRedis() {
        return redis;
    }

    /**
     * Gets the key prefix used by this cache.
     *
     * @return The key prefix
     */
    public String getKeyPrefix() {
        return keyPrefix;
    }

    /**
     * Prefixes a key with the cache's key prefix.
     *
     * @param key The key to prefix
     * @return The prefixed key
     */
    public String prefixKey(String key) {
        return keyPrefix + key;
    }

    @Override
    public Future<String> put(String key, String value) {
        return put(key, value, defaultTtl);
    }

    @Override
    public Future<String> put(String key, String value, long ttlMillis) {
        String prefixedKey = prefixKey(key);

        // Get the previous value before setting the new one
        return get(key).compose(previousValue -> {
            // Set the value in Redis
            if (ttlMillis > 0) {
                return redis.psetex(prefixedKey, String.valueOf(ttlMillis), value).compose(response -> {
                    // Publish event based on whether there was a previous value
                    if (previousValue != null) {
                        eventManager.publishEvent(CacheEvent.EventType.KEY_UPDATED, key);
                    } else {
                        eventManager.publishEvent(CacheEvent.EventType.KEY_CREATED, key);
                    }

                    return Future.succeededFuture(previousValue == null ? null : previousValue.toString());
                });
            } else {
                return redis.set(List.of(prefixedKey, value)).compose(response -> {
                    // Publish event based on whether there was a previous value
                    if (previousValue != null) {
                        eventManager.publishEvent(CacheEvent.EventType.KEY_UPDATED, key);
                    } else {
                        eventManager.publishEvent(CacheEvent.EventType.KEY_CREATED, key);
                    }
                    return Future.succeededFuture(previousValue == null ? null : previousValue.toString());
                });
            }
        });
    }

    @Override
    public Future<Response> get(String key) {
        return redis.get(prefixKey(key));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Future<T> remove(String key) {
        String prefixedKey = prefixKey(key);

        // Get the value before deleting it
        return get(key)
                .compose(value -> {
                    if (value == null) {
                        return Future.succeededFuture(null);
                    }

                    // Delete the key
                    return redis.del(List.of(prefixedKey))
                            .compose(response -> {
                                eventManager.publishEvent(CacheEvent.EventType.KEY_DELETED, key);
                                return Future.succeededFuture((T) value);
                            });
                });
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
        return (ValueOperation<T>) valueOperations.computeIfAbsent(clazz,
                c -> new DistributedValueOperation<>(this, clazz, serializer, deserializer));
    }

    @Override
    public Future<Void> clear() {
        // Use the KEYS command to find all keys with our prefix
        return redis.keys(keyPrefix + "*")
                .compose(response -> {
                    if (response == null || response.size() == 0) {
                        return Future.succeededFuture();
                    }

                    // Convert the response to a list of strings for the DEL command
                    List<String> keysList = new ArrayList<>();
                    for (int i = 0; i < response.size(); i++) {
                        keysList.add(response.get(i).toString());
                    }

                    // Delete all the keys
                    return redis.del(keysList)
                            .compose(delResponse -> {
                                eventManager.publishEvent(CacheEvent.EventType.CACHE_CLEARED, null);
                                return Future.succeededFuture();
                            });
                });
    }

    @Override
    public CacheEventManager events() {
        return eventManager;
    }

    @Override
    public Future<Void> close() {
        return redisClient.close();
    }
}
