package io.vertx.cache.common.serialization;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.buffer.Buffer;

/**
 * Interface for serializing objects to byte arrays.
 *
 * @param <T> The type of objects this serializer can handle
 */
@VertxGen
public interface CacheSerializer<T> {
    /**
     * Serializes an object to a byte array.
     *
     * @param object The object to serialize
     * @return The serialized byte array
     */
    Buffer serialize(T object);
}