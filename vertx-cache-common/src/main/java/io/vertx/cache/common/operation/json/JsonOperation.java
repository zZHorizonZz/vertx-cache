package io.vertx.cache.common.operation.json;

import io.vertx.cache.common.operation.CacheOperation;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.json.JsonObject;

/**
 * Interface for JSON object operations in the cache.
 */
@VertxGen
public interface JsonOperation extends CacheOperation<JsonObject> {
    
}