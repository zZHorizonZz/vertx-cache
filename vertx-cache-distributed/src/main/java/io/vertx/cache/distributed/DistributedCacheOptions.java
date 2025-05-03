package io.vertx.cache.distributed;

import io.vertx.cache.common.AbstractCacheOptions;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.json.annotations.JsonGen;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.RedisOptions;

import java.util.concurrent.TimeUnit;

@DataObject
@JsonGen(publicConverter = false)
public class DistributedCacheOptions extends AbstractCacheOptions {

    public static final String DEFAULT_KEY_PREFIX = "vertx:cache:";

    private String keyPrefix;
    private RedisOptions redisOptions;

    public DistributedCacheOptions() {
        super();
        this.keyPrefix = DEFAULT_KEY_PREFIX;
        this.redisOptions = new RedisOptions();
    }

    public DistributedCacheOptions(DistributedCacheOptions other) {
        super(other);
        this.keyPrefix = other.keyPrefix;
        this.redisOptions = other.redisOptions != null ? new RedisOptions(other.redisOptions) : new RedisOptions();
    }

    public DistributedCacheOptions(JsonObject json) {
        this();
        DistributedCacheOptionsConverter.fromJson(json, this);
    }

    @Override
    public DistributedCacheOptions setDefaultTtl(long defaultTtl) {
        super.setDefaultTtl(defaultTtl);
        return this;
    }

    @Override
    public DistributedCacheOptions setDefaultTtlTimeUnit(TimeUnit defaultTtlTimeUnit) {
        super.setDefaultTtlTimeUnit(defaultTtlTimeUnit);
        return this;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public DistributedCacheOptions setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
        return this;
    }

    public RedisOptions getRedisOptions() {
        return redisOptions;
    }

    public DistributedCacheOptions setRedisOptions(RedisOptions redisOptions) {
        this.redisOptions = redisOptions;
        return this;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        DistributedCacheOptionsConverter.toJson(this, json);
        return json;
    }

    @Override
    public String toString() {
        return toJson().encodePrettily();
    }
}
