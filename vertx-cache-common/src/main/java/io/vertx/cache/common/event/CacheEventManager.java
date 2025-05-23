package io.vertx.cache.common.event;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Closeable;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

/**
 * Interface for managing cache events. This interface provides methods for publishing events to the EventBus and registering handlers for events.
 */
@VertxGen
public interface CacheEventManager extends Closeable {

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