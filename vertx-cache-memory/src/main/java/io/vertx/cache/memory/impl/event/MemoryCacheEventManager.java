package io.vertx.cache.memory.impl.event;

import io.vertx.cache.common.event.CacheEvent;
import io.vertx.cache.common.event.CacheEventManager;
import io.vertx.core.Completable;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MemoryCacheEventManager implements CacheEventManager {

    private final Vertx vertx;
    private final String eventAddress;
    private final Map<String, MessageConsumer<JsonObject>> consumers;

    public MemoryCacheEventManager(Vertx vertx) {
        this(vertx, DEFAULT_EVENT_ADDRESS);
    }

    public MemoryCacheEventManager(Vertx vertx, String eventAddress) {
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
    public Future<String> registerEventHandler(Handler<CacheEvent> handler) {
        String registrationId = UUID.randomUUID().toString();
        MessageConsumer<JsonObject> consumer = vertx.eventBus().consumer(eventAddress, message -> handler.handle(new CacheEvent(message.body())));
        consumers.put(registrationId, consumer);
        return Future.succeededFuture(registrationId);
    }

    @Override
    public Future<String> registerEventHandler(CacheEvent.EventType type, Handler<CacheEvent> handler) {
        String registrationId = UUID.randomUUID().toString();
        MessageConsumer<JsonObject> consumer = vertx.eventBus().consumer(eventAddress, message -> {
            CacheEvent event = new CacheEvent(message.body());
            if (event.getType() == type) {
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
            CacheEvent event = new CacheEvent(message.body());
            if (key.equals(event.getKey())) {
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

    @Override
    public void close(Completable<Void> completion) {
        for (MessageConsumer<JsonObject> consumer : consumers.values()) {
            consumer.unregister();
        }
        consumers.clear();
        completion.succeed();
    }
}