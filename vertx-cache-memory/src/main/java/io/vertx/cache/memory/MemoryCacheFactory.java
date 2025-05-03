package io.vertx.cache.memory;

import io.vertx.cache.common.Cache;
import io.vertx.core.Vertx;

/**
 * Factory class for creating Vert.x-based cache instances.
 */
public class MemoryCacheFactory {

    /**
     * Creates a new MemoryCache with default settings. The default settings are: - Default TTL: 1 hour (3,600,000 milliseconds)
     *
     * @param vertx The Vertx instance to use
     * @return A new MemoryCache instance
     */
    public static io.vertx.cache.common.Cache create(Vertx vertx) {
        return new MemoryCache(vertx);
    }

    /**
     * Creates a new MemoryCache with custom TTL.
     *
     * @param vertx The Vertx instance to use
     * @param defaultTtlSeconds The default TTL in seconds
     * @return A new MemoryCache instance
     */
    public static io.vertx.cache.common.Cache create(Vertx vertx, long defaultTtlSeconds) {
        return new MemoryCache(vertx, defaultTtlSeconds * 1000);
    }

    /**
     * Creates a new MemoryCache with custom TTL and maximum size. Note: The maximum size is not enforced in the current implementation. It's included for API compatibility with
     * the previous implementation.
     *
     * @param vertx The Vertx instance to use
     * @param maximumSize The maximum number of entries in the cache (not enforced)
     * @param defaultTtlSeconds The default TTL in seconds
     * @return A new MemoryCache instance
     */
    public static Cache create(Vertx vertx, long maximumSize, long defaultTtlSeconds) {
        return new MemoryCache(vertx, defaultTtlSeconds * 1000);
    }
}