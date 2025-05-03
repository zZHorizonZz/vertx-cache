package io.vertx.cache.common.operation.number;

import io.vertx.cache.common.operation.CacheOperation;
import io.vertx.core.Future;

/**
 * Interface for number-related operations in the cache.
 */
public interface NumberOperation<T extends Number> extends CacheOperation<T> {
    /**
     * Increments the number stored at a key by 1.
     * If the key doesn't exist, it is set to 0 before performing the operation.
     *
     * @param key The key to increment
     * @return A Future that will be completed with the value after the increment
     */
    Future<T> increment(String key);

    /**
     * Increments the number stored at a key by the given amount.
     * If the key doesn't exist, it is set to 0 before performing the operation.
     *
     * @param key    The key to increment
     * @param amount The amount to increment by
     * @return A Future that will be completed with the value after the increment
     */
    Future<T> increment(String key, T amount);

    /**
     * Decrements the number stored at a key by 1.
     * If the key doesn't exist, it is set to 0 before performing the operation.
     *
     * @param key The key to decrement
     * @return A Future that will be completed with the value after the decrement
     */
    Future<T> decrement(String key);

    /**
     * Decrements the number stored at a key by the given amount.
     * If the key doesn't exist, it is set to 0 before performing the operation.
     *
     * @param key    The key to decrement
     * @param amount The amount to decrement by
     * @return A Future that will be completed with the value after the decrement
     */
    Future<T> decrement(String key, T amount);
}
