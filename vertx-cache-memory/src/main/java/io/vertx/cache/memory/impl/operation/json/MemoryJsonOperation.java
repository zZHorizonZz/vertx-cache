package io.vertx.cache.memory.impl.operation.json;

import io.vertx.cache.common.operation.json.JsonOperation;
import io.vertx.cache.memory.MemoryCache;
import io.vertx.cache.memory.impl.operation.MemoryValueOperation;
import io.vertx.core.json.JsonObject;

public class MemoryJsonOperation extends MemoryValueOperation<JsonObject> implements JsonOperation {

    public MemoryJsonOperation(MemoryCache cache) {
        super(cache, JsonObject.class, null, null);
    }
}