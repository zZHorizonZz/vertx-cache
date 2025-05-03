package io.vertx.cache.common.event;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

/**
 * Interface for managing cache events.
 * This interface provides methods for publishing events to the EventBus and registering handlers for events.
 */
public interface CacheEventManager {

    /**
     * The default address for cache events on the EventBus.
     */
    String DEFAULT_EVENT_ADDRESS = "vertx.cache.events";

    /**
     * Gets the Vertx instance used by this event manager.
     *
     * @return The Vertx instance
     */
    Vertx getVertx();

    /**
     * Gets the address used for publishing events on the EventBus.
     *
     * @return The event address
     */
    String getEventAddress();

    /**
     * Publishes a cache event to the EventBus.
     *
     * @param event The event to publish
     * @return A Future that will be completed when the event is published
     */
    Future<Void> publishEvent(CacheEvent event);

    /**
     * Publishes a cache event of the specified type for the given key to the EventBus.
     *
     * @param type The type of the event
     * @param key The key that the event is related to (can be null for events like CACHE_CLEARED)
     * @return A Future that will be completed when the event is published
     */
    Future<Void> publishEvent(CacheEvent.EventType type, String key);

    /**
     * Registers a handler for all cache events.
     *
     * @param handler The handler to register
     * @return A Future that will be completed with the registration ID when the handler is registered
     */
    Future<String> registerEventHandler(Handler<CacheEvent> handler);

    /**
     * Registers a handler for cache events of a specific type.
     *
     * @param type The type of events to handle
     * @param handler The handler to register
     * @return A Future that will be completed with the registration ID when the handler is registered
     */
    Future<String> registerEventHandler(CacheEvent.EventType type, Handler<CacheEvent> handler);

    /**
     * Registers a handler for cache events related to a specific key.
     *
     * @param key The key to handle events for
     * @param handler The handler to register
     * @return A Future that will be completed with the registration ID when the handler is registered
     */
    Future<String> registerKeyEventHandler(String key, Handler<CacheEvent> handler);

    /**
     * Unregisters a previously registered event handler.
     *
     * @param registrationId The registration ID returned by one of the registerEventHandler methods
     * @return A Future that will be completed when the handler is unregistered
     */
    Future<Void> unregisterEventHandler(String registrationId);
}