package io.vertx.cache.common.operation.binary;

import io.vertx.cache.common.operation.CacheOperation;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.buffer.Buffer;

/**
 * Interface for binary data operations in the cache.
 */
@VertxGen
public interface BinaryOperation extends CacheOperation<Buffer> {
    // Inherits all methods from CacheOperation with Buffer as the value type
}
