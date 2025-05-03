package io.vertx.cache.common.serialization;

/**
 * Interface for deserializing byte arrays to objects.
 *
 * @param <T> The type of objects this deserializer can produce
 */
public interface CacheDeserializer<T> {
    /**
     * Deserializes a byte array to an object.
     *
     * @param bytes The byte array to deserialize
     * @return The deserialized object
     */
    T deserialize(byte[] bytes);
}