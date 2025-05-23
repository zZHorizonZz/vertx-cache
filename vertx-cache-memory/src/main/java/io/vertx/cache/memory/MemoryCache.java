package io.vertx.cache.memory;

import io.vertx.cache.common.Cache;
import io.vertx.cache.memory.impl.MemoryCacheImpl;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;

import java.util.Set;

/**
 * MemoryCache is an interface that extends {@link Cache} and provides in-memory caching capabilities. It includes static factory methods to create instances of a memory-based
 * cache implementation.
 */
@VertxGen
public interface MemoryCache extends Cache {

    /**
     * Creates a new instance of a memory-based cache using the provided Vert.x instance.
     *
     * @param vertx The Vert.x instance used for the cache
     * @return A new memory-based cache instance
     */
    static MemoryCache create(Vertx vertx) {
        return new MemoryCacheImpl(vertx);
    }

    /**
     * Creates a new instance of a memory-based cache implementation with the specified Vert.x instance and memory cache options.
     *
     * @param vertx The Vert.x instance to associate with the cache, used for internal operations.
     * @param options The memory cache options specifying configuration such as TTL and maximum size.
     * @return A Cache instance configured with the provided Vert.x instance and memory cache options.
     */
    static MemoryCache create(Vertx vertx, MemoryCacheOptions options) {
        return new MemoryCacheImpl(vertx, options);
    }

    /**
     * Gets the Vertx instance used by this cache.
     *
     * @return The Vertx instance
     */
    Vertx getVertx();

    /**
     * Retrieves the set of keys currently stored in the cache.
     *
     * @return A set containing all keys present in the cache
     */
    Set<String> keySet();

    /**
     * Puts a value in the cache with the default TTL.
     *
     * @param key The key
     * @param value The value
     * @param <T> The type of the value
     * @return The previous value, or null if there was no previous value
     */
    <T> T put(String key, T value);

    /**
     * Puts a value in the cache with a custom TTL.
     *
     * @param key The key
     * @param value The value
     * @param ttlMillis The TTL in milliseconds
     * @param <T> The type of the value
     * @return The previous value, or null if there was no previous value
     */
    <T> T put(String key, T value, long ttlMillis);

    /**
     * Gets a value from the cache.
     *
     * @param key The key
     * @param <T> The expected type of the value
     * @return The value, or null if the key doesn't exist or has expired
     */
    <T> T get(String key);

    /**
     * Removes a value from the cache.
     *
     * @param key The key
     * @return The removed value, or null if the key didn't exist
     */
    <T> T remove(String key);
}
