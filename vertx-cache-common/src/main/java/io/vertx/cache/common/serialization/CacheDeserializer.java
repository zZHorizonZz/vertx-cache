package io.vertx.cache.common.serialization;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.buffer.Buffer;

/**
 * Interface for deserializing byte arrays to objects.
 *
 * @param <T> The type of objects this deserializer can produce
 */
@VertxGen
public interface CacheDeserializer<T> {
    /**
     * Deserializes a byte array to an object.
     *
     * @param data The {@link Buffer} to deserialize from. This buffer will be automatically released after the deserialization is complete.
     * @return The deserialized object
     */
    T deserialize(Buffer data);
}