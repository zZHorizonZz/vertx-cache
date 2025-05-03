package io.vertx.cache.common.operation.json;

import io.vertx.cache.common.serialization.CacheDeserializer;
import io.vertx.cache.common.serialization.CacheSerializer;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;

public class JsonArraySerializer implements CacheSerializer<JsonArray>, CacheDeserializer<JsonArray> {

    @Override
    public Buffer serialize(JsonArray array) {
        if (array == null) {
            return null;
        }
        return array.toBuffer();
    }

    @Override
    public JsonArray deserialize(Buffer bytes) {
        if (bytes == null) {
            return null;
        }
        return new JsonArray(bytes);
    }
}