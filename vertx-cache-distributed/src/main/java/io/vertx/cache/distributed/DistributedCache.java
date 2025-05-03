package io.vertx.cache.distributed;

import io.vertx.cache.common.Cache;
import io.vertx.cache.distributed.impl.DistributedCacheImpl;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.Response;

/**
 * DistributedCache is an interface that extends {@link Cache} and provides Redis-based distributed caching capabilities. It includes static factory methods to create instances of
 * a Redis-based cache implementation.
 */
@VertxGen
public interface DistributedCache extends Cache {

    /**
     * Creates a new instance of a Redis-based cache using the provided Vert.x instance with default Redis options.
     *
     * @param vertx The Vert.x instance used for the cache
     * @return A new Redis-based cache instance
     */
    static DistributedCache create(Vertx vertx) {
        return new DistributedCacheImpl(vertx);
    }

    /**
     * Creates a new instance of a Redis-based cache implementation with the specified Vert.x instance and Redis cache options.
     *
     * @param vertx The Vert.x instance to associate with the cache, used for internal operations.
     * @param options The Redis cache options specifying configuration such as TTL and Redis connection details.
     * @return A Cache instance configured with the provided Vert.x instance and Redis cache options.
     */
    static DistributedCache create(Vertx vertx, DistributedCacheOptions options) {
        return new DistributedCacheImpl(vertx, options);
    }

    /**
     * Creates a new instance of a Redis-based cache implementation with the specified Vert.x instance, Redis client, and Redis cache options.
     *
     * @param vertx The Vert.x instance to associate with the cache, used for internal operations.
     * @param redis The Redis client to use for Redis operations.
     * @param options The Redis cache options specifying configuration such as TTL.
     * @return A Cache instance configured with the provided parameters.
     */
    static DistributedCache create(Vertx vertx, Redis redis, DistributedCacheOptions options) {
        return new DistributedCacheImpl(vertx, redis, options);
    }

    /**
     * Gets the Vertx instance used by this cache.
     *
     * @return The Vertx instance
     */
    Vertx getVertx();

    /**
     * Gets the Redis client used by this cache.
     *
     * @return The Redis client
     */
    Redis getRedisClient();

    /**
     * Puts a value in the cache with the default TTL.
     *
     * @param key The key
     * @param value The value
     * @return A Future that will be completed with the previous value, or null if there was no previous value
     */
    Future<String> put(String key, String value);

    /**
     * Puts a value in the cache with a custom TTL.
     *
     * @param key The key
     * @param value The value
     * @param ttlMillis The TTL in milliseconds
     * @return A Future that will be completed with the previous value, or null if there was no previous value
     */
    Future<String> put(String key, String value, long ttlMillis);

    /**
     * Gets a value from the cache.
     *
     * @param key The key
     * @return A Future that will be completed with the value, or null if the key doesn't exist or has expired
     */
    Future<Response> get(String key);

    /**
     * Removes a value from the cache.
     *
     * @param key The key
     * @return A Future that will be completed with the removed value, or null if the key didn't exist
     */
    <T> Future<T> remove(String key);
}