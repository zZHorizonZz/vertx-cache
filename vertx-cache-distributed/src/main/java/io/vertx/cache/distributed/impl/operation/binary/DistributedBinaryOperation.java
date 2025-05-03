package io.vertx.cache.distributed.impl.operation.binary;

import io.vertx.cache.common.operation.binary.BinaryOperation;
import io.vertx.cache.common.operation.binary.BinarySerializer;
import io.vertx.cache.distributed.impl.DistributedCacheImpl;
import io.vertx.cache.distributed.impl.operation.DistributedValueOperation;
import io.vertx.core.buffer.Buffer;

public class DistributedBinaryOperation extends DistributedValueOperation<Buffer> implements BinaryOperation {

    private static final BinarySerializer SERIALIZER = new BinarySerializer();

    public DistributedBinaryOperation(DistributedCacheImpl cache) {
        super(cache, Buffer.class, SERIALIZER, SERIALIZER);
    }
}
