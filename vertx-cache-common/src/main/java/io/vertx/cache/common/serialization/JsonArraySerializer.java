package io.vertx.cache.common.serialization;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;

public class JsonArraySerializer implements CacheSerializer<JsonArray>, CacheDeserializer<JsonArray> {

    @Override
    public byte[] serialize(JsonArray array) {
        if (array == null) {
            return null;
        }
        return array.toBuffer().getBytes();
    }

    @Override
    public JsonArray deserialize(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        return new JsonArray(Buffer.buffer(bytes));
    }
}