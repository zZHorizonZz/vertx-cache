package io.vertx.cache.memory;

import io.vertx.cache.common.AbstractCacheOptions;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.json.annotations.JsonGen;
import io.vertx.core.json.JsonObject;

import java.util.concurrent.TimeUnit;

@DataObject
@JsonGen(publicConverter = false)
public class MemoryCacheOptions extends AbstractCacheOptions {

    public static final long DEFAULT_MAXIMUM_SIZE = 10000;
    public static final long DEFAULT_CLEANUP_INTERVAL = 1;
    public static final TimeUnit DEFAULT_CLEANUP_INTERVAL_TIMEUNIT = TimeUnit.SECONDS;

    private long maximumSize;
    private long cleanupInterval;
    private TimeUnit cleanupIntervalTimeUnit;

    public MemoryCacheOptions() {
        super();
        this.maximumSize = DEFAULT_MAXIMUM_SIZE;
        this.cleanupInterval = DEFAULT_CLEANUP_INTERVAL;
        this.cleanupIntervalTimeUnit = DEFAULT_CLEANUP_INTERVAL_TIMEUNIT;
    }

    public MemoryCacheOptions(MemoryCacheOptions other) {
        super(other);
        this.maximumSize = other.maximumSize;
        this.cleanupInterval = other.cleanupInterval;
        this.cleanupIntervalTimeUnit = other.cleanupIntervalTimeUnit;
    }

    public MemoryCacheOptions(JsonObject json) {
        this();
        MemoryCacheOptionsConverter.fromJson(json, this);
    }

    @Override
    public MemoryCacheOptions setDefaultTtl(long defaultTtl) {
        super.setDefaultTtl(defaultTtl);
        return this;
    }

    @Override
    public MemoryCacheOptions setDefaultTtlTimeUnit(TimeUnit defaultTtlTimeUnit) {
        super.setDefaultTtlTimeUnit(defaultTtlTimeUnit);
        return this;
    }

    public long getMaximumSize() {
        return maximumSize;
    }

    public MemoryCacheOptions setMaximumSize(long maximumSize) {
        this.maximumSize = maximumSize;
        return this;
    }

    public long getCleanupInterval() {
        return cleanupInterval;
    }

    public MemoryCacheOptions setCleanupInterval(long cleanupInterval) {
        this.cleanupInterval = cleanupInterval;
        return this;
    }

    public long getCleanupIntervalMillis() {
        return cleanupIntervalTimeUnit.toMillis(cleanupInterval);
    }

    public TimeUnit getCleanupIntervalTimeUnit() {
        return cleanupIntervalTimeUnit;
    }

    public MemoryCacheOptions setCleanupIntervalTimeUnit(TimeUnit cleanupIntervalTimeUnit) {
        this.cleanupIntervalTimeUnit = cleanupIntervalTimeUnit;
        return this;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        MemoryCacheOptionsConverter.toJson(this, json);
        return json;
    }

    @Override
    public String toString() {
        return toJson().encodePrettily();
    }
}
