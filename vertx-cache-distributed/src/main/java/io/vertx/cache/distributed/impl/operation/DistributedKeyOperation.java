package io.vertx.cache.distributed.impl.operation;

import io.vertx.cache.common.event.CacheEvent;
import io.vertx.cache.common.operation.KeyOperation;
import io.vertx.cache.distributed.impl.DistributedCacheImpl;
import io.vertx.core.Future;
import io.vertx.redis.client.Response;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class DistributedKeyOperation implements KeyOperation {

    private final DistributedCacheImpl cache;

    public DistributedKeyOperation(DistributedCacheImpl cache) {
        this.cache = cache;
    }

    @Override
    public Future<Set<String>> keys(String pattern) {
        return cache.getRedis().keys(cache.getKeyPrefix() + pattern).compose(response -> {
            Set<String> keys = extractKeys(response);
            cache.events().publishEvent(CacheEvent.EventType.KEYS_LISTED, pattern);
            return Future.succeededFuture(keys);
        });
    }

    @Override
    public Future<Set<String>> keys() {
        return cache.getRedis().keys(cache.getKeyPrefix() + "*").compose(response -> {
            Set<String> keys = extractKeys(response);
            cache.events().publishEvent(CacheEvent.EventType.KEYS_LISTED, "*");
            return Future.succeededFuture(keys);
        });
    }

    /**
     * Extracts keys from a Redis response and removes the prefix.
     *
     * @param response The Redis response
     * @return A set of keys without the prefix
     */
    private Set<String> extractKeys(Response response) {
        if (response == null) {
            return new HashSet<>();
        }

        return response.stream()
                .map(Response::toString)
                .map(key -> key.substring(cache.getKeyPrefix().length()))
                .collect(Collectors.toSet());
    }
}