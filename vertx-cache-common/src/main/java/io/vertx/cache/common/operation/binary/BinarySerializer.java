package io.vertx.cache.common.operation.binary;

import io.vertx.cache.common.serialization.CacheDeserializer;
import io.vertx.cache.common.serialization.CacheSerializer;
import io.vertx.core.buffer.Buffer;

public class BinarySerializer implements CacheSerializer<Buffer>, CacheDeserializer<Buffer> {
    @Override
    public Buffer serialize(Buffer object) {
        return object.copy();
    }

    @Override
    public Buffer deserialize(Buffer bytes) {
        return bytes.copy();
    }
}
