package io.vertx.cache.common.serialization;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

public class JsonObjectSerializer implements CacheSerializer<JsonObject>, CacheDeserializer<JsonObject> {

    @Override
    public byte[] serialize(JsonObject object) {
        if (object == null) {
            return null;
        }
        return object.toBuffer().getBytes();
    }

    @Override
    public JsonObject deserialize(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        return new JsonObject(Buffer.buffer(bytes));
    }
}