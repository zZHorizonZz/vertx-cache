package io.vertx.cache.distributed.impl.operation.json;

import io.vertx.cache.common.operation.json.JsonObjectSerializer;
import io.vertx.cache.common.operation.json.JsonOperation;
import io.vertx.cache.distributed.impl.DistributedCacheImpl;
import io.vertx.cache.distributed.impl.operation.DistributedValueOperation;
import io.vertx.core.json.JsonObject;

public class DistributedJsonOperation extends DistributedValueOperation<JsonObject> implements JsonOperation {

    private static final JsonObjectSerializer SERIALIZER = new JsonObjectSerializer();

    public DistributedJsonOperation(DistributedCacheImpl cache) {
        super(cache, JsonObject.class, SERIALIZER, SERIALIZER);
    }
}
