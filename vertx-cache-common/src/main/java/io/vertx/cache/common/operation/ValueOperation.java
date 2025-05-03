package io.vertx.cache.common.operation;

import io.vertx.cache.common.serialization.CacheDeserializer;
import io.vertx.cache.common.serialization.CacheSerializer;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;

import java.util.concurrent.TimeUnit;

/**
 * Interface for operations on custom objects in the cache.
 *
 * @param <T> The type of objects this operation works with
 */
@VertxGen
public interface ValueOperation<T> extends CacheOperation<T> {

    /**
     * Retrieves a value from the cache associated with the given key, using the provided deserializer to transform the cached byte array into the desired object type.
     *
     * @param key The key whose associated value is to be retrieved
     * @param deserializer The deserializer to convert the cached data to an object
     * @return A Future that will be completed with the value of type T, or null if no value is associated with the key
     */
    Future<T> get(String key, CacheDeserializer<T> deserializer);

    /**
     * Sets the value for a given key in the cache using a custom serializer.
     *
     * @param key The key for which the value is to be set
     * @param value The value to set for the given key
     * @param serializer The serializer to use for converting the value to its serialized form
     * @return A Future that will be completed when the operation is done
     */
    Future<Void> set(String key, T value, CacheSerializer<T> serializer);

    /**
     * Sets a value in the cache associated with the specified key, with an optional expiration time and using the provided serializer for object conversion.
     *
     * @param key The key to associate with the value in the cache
     * @param value The value to store in the cache
     * @param ttl The time-to-live for the value in the cache, or 0 for no expiration
     * @param unit The time unit for the ttl parameter
     * @param serializer The serializer to use for converting the value to a storable byte format
     * @return A Future that will be completed when the operation is done
     */
    Future<Void> set(String key, T value, long ttl, TimeUnit unit, CacheSerializer<T> serializer);

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
