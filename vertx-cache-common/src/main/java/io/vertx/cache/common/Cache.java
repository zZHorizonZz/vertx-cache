package io.vertx.cache.common;

import io.vertx.cache.common.event.CacheEventManager;
import io.vertx.cache.common.operation.KeyOperation;
import io.vertx.cache.common.operation.ValueOperation;
import io.vertx.cache.common.operation.number.NumberOperation;
import io.vertx.cache.common.operation.text.StringOperation;
import io.vertx.cache.common.serialization.CacheDeserializer;
import io.vertx.cache.common.serialization.CacheSerializer;
import io.vertx.core.Future;

import java.io.Closeable;

/**
 * Main interface for the Vert.x Cache. Provides methods for basic cache operations and access to specialized operation interfaces.
 * Also provides access to the cache event manager for publishing and subscribing to cache events.
 */
public interface Cache extends Closeable {

    /**
     * Gets the key operation interface.
     *
     * @return The key operation interface
     */
    KeyOperation keys();

    /**
     * Gets the string operation interface.
     *
     * @return The string operation interface
     */
    StringOperation strings();

    /**
     * Gets the number operation interface.
     *
     * @return The number operation interface
     */
    NumberOperation<Long> integers();

    /**
     * Gets the float operation interface.
     *
     * @return The float operation interface
     */
    NumberOperation<Double> floats();


    /**
     * Gets the value operation interface for a specific class type.
     *
     * @param <T> The type of objects this operation works with
     * @param clazz The class of objects this operation works with
     * @return The value operation interface
     */
    <T> ValueOperation<T> value(Class<T> clazz);

    /**
     * Gets the value operation interface for a specific class type with custom serializer and deserializer.
     *
     * @param <T> The type of objects this operation works with
     * @param clazz The class of objects this operation works with
     * @param serializer The serializer to use
     * @param deserializer The deserializer to use
     * @return The value operation interface
     */
    <T> ValueOperation<T> value(Class<T> clazz, CacheSerializer<T> serializer, CacheDeserializer<T> deserializer);

    /**
     * Clears all keys from the cache.
     *
     * @return A Future that will be completed when the operation is done
     */
    Future<Void> clear();

    /**
     * Gets the event manager for this cache.
     * The event manager can be used to publish events and register handlers for cache events.
     *
     * @return The cache event manager
     */
    CacheEventManager events();
}
