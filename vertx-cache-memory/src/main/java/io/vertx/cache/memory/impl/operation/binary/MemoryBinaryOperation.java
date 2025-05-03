package io.vertx.cache.memory.impl.operation.binary;

import io.vertx.cache.common.operation.binary.BinaryOperation;
import io.vertx.cache.memory.MemoryCache;
import io.vertx.cache.memory.impl.operation.MemoryValueOperation;
import io.vertx.core.buffer.Buffer;

public class MemoryBinaryOperation extends MemoryValueOperation<Buffer> implements BinaryOperation {

    public MemoryBinaryOperation(MemoryCache cache) {
        super(cache, Buffer.class, null, null);
    }
}