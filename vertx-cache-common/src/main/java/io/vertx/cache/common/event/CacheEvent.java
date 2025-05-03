package io.vertx.cache.common.event;

import io.vertx.core.json.JsonObject;

/**
 * Represents an event that occurred in the cache.
 */
public class CacheEvent {

    /**
     * The type of the cache event.
     */
    public enum EventType {
        /**
         * A key was created in the cache.
         */
        KEY_CREATED,

        /**
         * A key was updated in the cache.
         */
        KEY_UPDATED,

        /**
         * A key was deleted from the cache.
         */
        KEY_DELETED,

        /**
         * A key expired in the cache.
         */
        KEY_EXPIRED,

        /**
         * The cache was cleared.
         */
        CACHE_CLEARED,

        /**
         * A key was read from the cache.
         */
        KEY_READ,

        /**
         * Keys were listed from the cache.
         */
        KEYS_LISTED
    }

    private final EventType type;
    private final String key;
    private final long timestamp;

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
     * Gets the type of the event.
     *
     * @return The event type
     */
    public EventType getType() {
        return type;
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
        JsonObject json = new JsonObject()
                .put("type", type.name())
                .put("timestamp", timestamp);

        if (key != null) {
            json.put("key", key);
        }

        return json;
    }
}
