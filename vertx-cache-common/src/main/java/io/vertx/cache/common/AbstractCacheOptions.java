package io.vertx.cache.common;

import io.vertx.core.json.JsonObject;

import java.util.concurrent.TimeUnit;

/**
 * Abstract base class for cache options. Contains common configuration options shared between different cache implementations.
 */
public abstract class AbstractCacheOptions {

    public static final long DEFAULT_TTL = 5;
    public static final TimeUnit DEFAULT_TTL_TIMEUNIT = TimeUnit.SECONDS;

    private long defaultTtl;
    private TimeUnit defaultTtlTimeUnit;

    protected AbstractCacheOptions() {
        this.defaultTtl = DEFAULT_TTL;
        this.defaultTtlTimeUnit = DEFAULT_TTL_TIMEUNIT;
    }

    protected AbstractCacheOptions(AbstractCacheOptions other) {
        this.defaultTtl = other.defaultTtl;
        this.defaultTtlTimeUnit = other.defaultTtlTimeUnit;
    }

    /**
     * @return the default time-to-live value
     */
    public long getDefaultTtl() {
        return defaultTtl;
    }

    /**
     * Set the default time-to-live value
     *
     * @param defaultTtl the default time-to-live value
     * @return a reference to this, so the API can be used fluently
     */
    public AbstractCacheOptions setDefaultTtl(long defaultTtl) {
        this.defaultTtl = defaultTtl;
        return this;
    }

    /**
     * @return the default time-to-live value in milliseconds
     */
    public long getDefaultTtlMillis() {
        return defaultTtlTimeUnit.toMillis(defaultTtl);
    }

    /**
     * @return the default time-to-live time unit
     */
    public TimeUnit getDefaultTtlTimeUnit() {
        return defaultTtlTimeUnit;
    }

    /**
     * Set the default time-to-live time unit
     *
     * @param defaultTtlTimeUnit the default time-to-live time unit
     * @return a reference to this, so the API can be used fluently
     */
    public AbstractCacheOptions setDefaultTtlTimeUnit(TimeUnit defaultTtlTimeUnit) {
        this.defaultTtlTimeUnit = defaultTtlTimeUnit;
        return this;
    }

    /**
     * Convert to JSON
     *
     * @return the JSON representation of this object
     */
    public abstract JsonObject toJson();

    @Override
    public String toString() {
        return toJson().encodePrettily();
    }
}