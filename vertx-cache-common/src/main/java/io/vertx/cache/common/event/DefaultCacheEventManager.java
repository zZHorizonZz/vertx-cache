package io.vertx.cache.common.event;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Default implementation of the CacheEventManager interface. Uses the Vert.x EventBus to publish events and register handlers.
 */
public class DefaultCacheEventManager implements CacheEventManager {

    private final Vertx vertx;
    private final String eventAddress;
    private final Map<String, MessageConsumer<JsonObject>> consumers;

    /**
     * Creates a new DefaultCacheEventManager with the default event address.
     *
     * @param vertx The Vertx instance to use
     */
    public DefaultCacheEventManager(Vertx vertx) {
        this(vertx, DEFAULT_EVENT_ADDRESS);
    }

    /**
     * Creates a new DefaultCacheEventManager with a custom event address.
     *
     * @param vertx The Vertx instance to use
     * @param eventAddress The address to use for publishing events on the EventBus
     */
    public DefaultCacheEventManager(Vertx vertx, String eventAddress) {
        this.vertx = vertx;
        this.eventAddress = eventAddress;
        this.consumers = new HashMap<>();
    }

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public String getEventAddress() {
        return eventAddress;
    }

    @Override
    public Future<Void> publishEvent(CacheEvent event) {
        EventBus eventBus = vertx.eventBus();
        eventBus.publish(eventAddress, event.toJson());
        return Future.succeededFuture();
    }

    @Override
    public Future<Void> publishEvent(CacheEvent.EventType type, String key) {
        return publishEvent(new CacheEvent(type, key));
    }

    @Override
    public Future<String> registerEventHandler(Handler<CacheEvent> handler) {
        String registrationId = UUID.randomUUID().toString();
        MessageConsumer<JsonObject> consumer = vertx.eventBus().consumer(eventAddress, message -> {
            System.out.println("Received event: " + message.body().toString());
            handler.handle(new CacheEvent(message.body()));
        });

        consumers.put(registrationId, consumer);
        return Future.succeededFuture(registrationId);
    }

    @Override
    public Future<String> registerEventHandler(CacheEvent.EventType type, Handler<CacheEvent> handler) {
        String registrationId = UUID.randomUUID().toString();
        MessageConsumer<JsonObject> consumer = vertx.eventBus().consumer(eventAddress, message -> {
            JsonObject json = message.body();
            CacheEvent.EventType eventType = CacheEvent.EventType.valueOf(json.getString("type"));

            if (eventType == type) {
                String key = json.getString("key");
                long timestamp = json.getLong("timestamp");

                CacheEvent event = new CacheEvent(eventType, key);
                handler.handle(event);
            }
        });

        consumers.put(registrationId, consumer);
        return Future.succeededFuture(registrationId);
    }

    @Override
    public Future<String> registerKeyEventHandler(String key, Handler<CacheEvent> handler) {
        String registrationId = UUID.randomUUID().toString();
        MessageConsumer<JsonObject> consumer = vertx.eventBus().consumer(eventAddress, message -> {
            JsonObject json = message.body();
            String eventKey = json.getString("key");

            if (key.equals(eventKey)) {
                CacheEvent.EventType type = CacheEvent.EventType.valueOf(json.getString("type"));
                long timestamp = json.getLong("timestamp");

                CacheEvent event = new CacheEvent(type, key);
                handler.handle(event);
            }
        });

        consumers.put(registrationId, consumer);
        return Future.succeededFuture(registrationId);
    }

    @Override
    public Future<Void> unregisterEventHandler(String registrationId) {
        MessageConsumer<JsonObject> consumer = consumers.remove(registrationId);
        if (consumer != null) {
            return consumer.unregister();
        }
        return Future.succeededFuture();
    }
}