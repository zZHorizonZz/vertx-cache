package io.vertx.cache.common.operation;

import io.vertx.cache.common.serialization.CacheDeserializer;
import io.vertx.cache.common.serialization.CacheSerializer;
import io.vertx.core.Future;

import java.time.Duration;

/**
 * Interface for operations on custom objects in the cache.
 * 
 * @param <T> The type of objects this operation works with
 */
public interface ValueOperation<T> extends CacheOperation<T> {
    /**
     * Gets a custom object for a key.
     *
     * @param key The key to get the object for
     * @return A Future that will be completed with the object, or null if the key doesn't exist
     */
    @Override
    Future<T> get(String key);

    /**
     * Sets a custom object for a key.
     *
     * @param key    The key to set the object for
     * @param value  The object to set
     * @return A Future that will be completed when the operation is done
     */
    @Override
    Future<Void> set(String key, T value);

    /**
     * Sets a custom object for a key with an expiration time.
     *
     * @param key         The key to set the object for
     * @param value       The object to set
     * @param expiration  The expiration time for the object
     * @return A Future that will be completed when the operation is done
     */
    @Override
    Future<Void> set(String key, T value, Duration expiration);

    /**
     * Gets the serializer used by this operation.
     *
     * @return The serializer
     */
    CacheSerializer<T> getSerializer();

    /**
     * Gets the deserializer used by this operation.
     *
     * @return The deserializer
     */
    CacheDeserializer<T> getDeserializer();
}
