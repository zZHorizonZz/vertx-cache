package io.vertx.cache.common.serialization;

/**
 * Interface for serializing objects to byte arrays.
 *
 * @param <T> The type of objects this serializer can handle
 */
public interface CacheSerializer<T> {
    /**
     * Serializes an object to a byte array.
     *
     * @param object The object to serialize
     * @return The serialized byte array
     */
    byte[] serialize(T object);
}