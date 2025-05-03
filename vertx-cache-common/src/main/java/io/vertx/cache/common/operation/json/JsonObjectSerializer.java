package io.vertx.cache.common.operation.json;

import io.vertx.cache.common.serialization.CacheDeserializer;
import io.vertx.cache.common.serialization.CacheSerializer;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

public class JsonObjectSerializer implements CacheSerializer<JsonObject>, CacheDeserializer<JsonObject> {
    @Override
    public Buffer serialize(JsonObject object) {
        if (object == null) {
            return null;
        }

        return object.toBuffer();
    }

    @Override
    public JsonObject deserialize(Buffer bytes) {
        if (bytes == null) {
            return null;
        }

        return new JsonObject(bytes);
    }
}