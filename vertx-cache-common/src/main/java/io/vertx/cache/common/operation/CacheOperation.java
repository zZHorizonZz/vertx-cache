package io.vertx.cache.common.operation;

import io.vertx.core.Future;

import java.time.Duration;

/**
 * Base interface for value-related operations in the cache.
 * 
 * @param <V> The type of values this operation works with
 */
public interface CacheOperation<V> {
    /**
     * Gets the value for a key.
     *
     * @param key The key to get the value for
     * @return A Future that will be completed with the value, or null if the key doesn't exist
     */
    Future<V> get(String key);

    /**
     * Gets the current value for a key and sets it to a new value.
     *
     * @param key   The key to get and set the value for
     * @param value The new value to set
     * @return A Future that will be completed with the old value, or null if the key didn't exist
     */
    Future<V> getAndSet(String key, V value);

    /**
     * Gets the current value for a key and deletes the key.
     *
     * @param key The key to get and delete
     * @return A Future that will be completed with the value that was deleted, or null if the key didn't exist
     */
    Future<V> getAndDelete(String key);

    /**
     * Sets the value for a key.
     *
     * @param key   The key to set the value for
     * @param value The value to set
     * @return A Future that will be completed when the operation is done
     */
    Future<Void> set(String key, V value);

    /**
     * Sets the value for a key with an expiration time.
     *
     * @param key        The key to set the value for
     * @param value      The value to set
     * @param expiration The expiration time for the value
     * @return A Future that will be completed when the operation is done
     */
    Future<Void> set(String key, V value, Duration expiration);

    /**
     * Sets the value for a key only if the key does not already exist.
     *
     * @param key   The key to set the value for
     * @param value The value to set
     * @return A Future that will be completed when the operation is done
     */
    Future<Void> setIfAbsent(String key, V value);

    /**
     * Sets the value for a key with an expiration time only if the key does not already exist.
     *
     * @param key        The key to set the value for
     * @param value      The value to set
     * @param expiration The expiration time for the value
     * @return A Future that will be completed when the operation is done
     */
    Future<Void> setIfAbsent(String key, V value, Duration expiration);

    /**
     * Deletes one or more keys from the cache.
     *
     * @param keys The keys to delete
     * @return A Future that will be completed when the operation is done
     */
    Future<Void> delete(String... keys);
}
