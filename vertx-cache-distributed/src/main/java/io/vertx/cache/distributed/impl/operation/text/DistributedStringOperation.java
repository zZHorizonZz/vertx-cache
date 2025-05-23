package io.vertx.cache.distributed.impl.operation.text;

import io.vertx.cache.common.event.CacheEvent;
import io.vertx.cache.common.operation.text.StringOperation;
import io.vertx.cache.distributed.impl.DistributedCacheImpl;
import io.vertx.cache.distributed.impl.operation.DistributedValueOperation;
import io.vertx.core.Future;

public class DistributedStringOperation extends DistributedValueOperation<String> implements StringOperation {

    public DistributedStringOperation(DistributedCacheImpl cache) {
        super(cache, String.class, null, null);
    }

    @Override
    public Future<Long> length(String key) {
        String prefixedKey = cache.prefixKey(key);

        return cache.getRedis().strlen(prefixedKey).compose(response -> {
            if (response != null) {
                return Future.succeededFuture(response.toLong());
            }
            return Future.succeededFuture(0L);
        });
    }

    @Override
    public Future<Integer> append(String key, String value) {
        String prefixedKey = cache.prefixKey(key);

        return cache.getRedis().append(prefixedKey, value).compose(response -> Future.succeededFuture(response.toInteger()));
    }

    @Override
    public Future<String> getRange(String key, int start, int end) {
        String prefixedKey = cache.prefixKey(key);

        return cache.getRedis().getrange(prefixedKey, String.valueOf(start), String.valueOf(end)).compose(response -> {
            if (response != null) {
                return Future.succeededFuture(response.toString());
            }
            return Future.succeededFuture("");
        });
    }

    @Override
    public Future<Long> setRange(String key, long offset, String value) {
        String prefixedKey = cache.prefixKey(key);

        return cache.getRedis().setrange(prefixedKey, String.valueOf(offset), value).compose(response -> Future.succeededFuture(response.toLong()));
    }
}