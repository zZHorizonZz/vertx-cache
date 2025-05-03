package io.vertx.cache.common.operation;

import io.vertx.core.Future;

import java.util.Set;

/**
 * Interface for key-related operations in the cache.
 */
public interface KeyOperation {
    /**
     * Gets all keys matching a pattern.
     *
     * @param pattern The pattern to match (supports glob-style patterns)
     * @return A Future that will be completed with the set of matching keys
     */
    Future<Set<String>> keys(String pattern);

    /**
     * Gets all keys in the cache.
     *
     * @return A Future that will be completed with the set of all keys
     */
    Future<Set<String>> keys();
}
