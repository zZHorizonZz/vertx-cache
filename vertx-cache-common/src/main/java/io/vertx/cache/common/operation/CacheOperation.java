package io.vertx.cache.common.operation;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;

import java.util.concurrent.TimeUnit;

/**
 * Base interface for value-related operations in the cache.
 *
 * @param <V> The type of values this operation works with
 */
@VertxGen
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
     * @param key The key to get and set the value for
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
     * @param key The key to set the value for
     * @param value The value to set
     * @return A Future that will be completed when the operation is done
     */
    Future<Void> set(String key, V value);

    /**
     * Sets the value for a key with an expiration time.
     *
     * @param key The key to set the value for
     * @param value The value to set
     * @param ttl The time-to-live for the value in the cache, or 0 for no expiration
     * @param unit The time unit for the ttl parameter
     * @return A Future that will be completed when the operation is done
     */
    Future<Void> set(String key, V value, long ttl, TimeUnit unit);

    /**
     * Sets the value for a key only if the key does not already exist.
     *
     * @param key The key to set the value for
     * @param value The value to set
     * @return A Future that will be completed when the operation is done
     */
    Future<Void> setIfAbsent(String key, V value);

    /**
     * Sets the value for a key with an expiration time only if the key does not already exist.
     *
     * @param key The key to set the value for
     * @param value The value to set
     * @param ttl The time-to-live for the value in the cache, or 0 for no expiration
     * @param unit The time unit for the ttl parameter
     * @return A Future that will be completed when the operation is done
     */
    Future<Void> setIfAbsent(String key, V value, long ttl, TimeUnit unit);

    /**
     * Checks if a key exists in the cache.
     *
     * @param key The key to check for existence in the cache
     * @return A Future that will be completed with true if the key exists, or false if it doesn't
     */
    @GenIgnore
    Future<Boolean> exists(String... key);

    /**
     * Deletes one or more keys from the cache.
     *
     * @param keys The keys to delete
     * @return A Future that will be completed when the operation is done
     */
    @GenIgnore
    Future<Void> delete(String... keys);
}
