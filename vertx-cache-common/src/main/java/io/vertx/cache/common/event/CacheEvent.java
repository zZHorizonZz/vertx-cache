package io.vertx.cache.common.event;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.json.annotations.JsonGen;
import io.vertx.core.json.JsonObject;

/**
 * Represents an event that occurred in the cache.
 */
@DataObject
@JsonGen(publicConverter = false)
public class CacheEvent {

    private EventType type;
    private String key;
    private long timestamp;

    /**
     * Creates a new cache event.
     *
     * @param json The JSON object to create the event from
     */
    public CacheEvent(JsonObject json) {
        CacheEventConverter.fromJson(json, this);
    }

    /**
     * Creates a new cache event.
     *
     * @param type The type of the event
     * @param key The key that the event is related to (can be null for events like CACHE_CLEARED)
     */
    public CacheEvent(EventType type, String key) {
        this.type = type;
        this.key = key;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Sets the type of the event.
     *
     * @param type The event type
     */
    public void setType(EventType type) {
        this.type = type;
    }

    /**
     * Gets the type of the event.
     *
     * @return The event type
     */
    public EventType getType() {
        return type;
    }

    /**
     * Sets the key that the event is related to.
     *
     * @param key The key, or null if the event is not related to a specific key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Gets the key that the event is related to.
     *
     * @return The key, or null if the event is not related to a specific key
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the timestamp when the event occurred.
     *
     * @param timestamp The timestamp in milliseconds since epoch
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Gets the timestamp when the event occurred.
     *
     * @return The timestamp in milliseconds since epoch
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Converts the event to a JSON object.
     *
     * @return A JSON object representing the event
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        CacheEventConverter.toJson(this, json);
        return json;
    }

    /**
     * The type of the cache event.
     */
    public enum EventType {
        /**
         * A key was updated in the cache.
         */
        KEY_UPDATED,

        /**
         * A key was deleted from the cache.
         */
        KEY_DELETED,

        /**
         * A key expired from the cache.
         */
        KEY_EXPIRED,

        /**
         * The cache was cleared.
         */
        CACHE_CLEARED
    }
}
