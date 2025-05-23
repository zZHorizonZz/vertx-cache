package io.vertx.cache.distributed.impl.event;

import io.vertx.cache.common.event.CacheEvent;
import io.vertx.cache.common.event.CacheEventManager;
import io.vertx.cache.distributed.impl.DistributedCacheImpl;
import io.vertx.core.Completable;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.internal.logging.Logger;
import io.vertx.core.internal.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.Command;
import io.vertx.redis.client.RedisConnection;
import io.vertx.redis.client.Request;
import io.vertx.redis.client.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DistributedCacheEventManager implements CacheEventManager {

    private static final Logger log = LoggerFactory.getLogger(DistributedCacheEventManager.class);

    private static final String MESSAGE = "pmessage";
    private static final String KEYSPACE = "__keyspace@";
    private static final String KEYEVENT = "__keyevent@";

    private final DistributedCacheImpl cache;

    private final EventBus eventBus;
    private final String eventAddress;
    private final Map<String, MessageConsumer<JsonObject>> consumers = new HashMap<>();

    private RedisConnection connection;

    public DistributedCacheEventManager(DistributedCacheImpl cache) {
        this(cache, CacheEventManager.DEFAULT_EVENT_ADDRESS);
    }

    public DistributedCacheEventManager(DistributedCacheImpl cache, String eventAddress) {
        this.cache = cache;
        this.eventBus = cache.getVertx().eventBus();
        this.eventAddress = eventAddress;

        setupRedisSubscriptions();
    }

    private void setupRedisSubscriptions() {
        Future.all(cache.getRedisClient().connect(), getCurrentDatabase()).onSuccess(result -> {
                    connection = result.resultAt(0);

                    connection.handler(this::handleRedisPubSubMessage);
                    connection.exceptionHandler(err -> {
                        log.trace("Error in Redis PubSub connection", err);
                        reconnectPubSub();
                    });

                    // Subscribe to keyspace events
                    String keyspacePattern = String.format("%s%s__:*", KEYSPACE, result.resultAt(1));
                    Request keyspaceRequest = Request.cmd(Command.PSUBSCRIBE).arg(keyspacePattern);
                    connection.send(keyspaceRequest)
                            .onSuccess(res -> log.info("Successfully subscribed to Redis keyspace notifications: " + keyspacePattern))
                            .onFailure(err -> log.trace("Failed to subscribe to Redis keyspace notifications: " + keyspacePattern, err));

                    // Subscribe to keyevent events
                    String keyeventPattern = String.format("%s%s__:*", KEYEVENT, result.resultAt(1));
                    Request keyeventRequest = Request.cmd(Command.PSUBSCRIBE).arg(keyeventPattern);
                    connection.send(keyeventRequest)
                            .onSuccess(res -> log.info("Successfully subscribed to Redis keyevent notifications: " + keyeventPattern))
                            .onFailure(err -> log.trace("Failed to subscribe to Redis keyevent notifications: " + keyeventPattern, err));
                })
                .onFailure(err -> {
                    log.trace("Failed to create Redis PubSub connection", err);
                    cache.getVertx().setTimer(5000, id -> reconnectPubSub());
                });
    }

    private Future<Integer> getCurrentDatabase() {
        return cache.getRedis().client(List.of("INFO")).compose(response -> {
            if (response == null) {
                return Future.failedFuture("Received null response from Redis INFO command");
            }

            Map<String, String> infoMap = new HashMap<>();
            for (String line : response.toString().split(" ")) {
                int equalsIndex = line.indexOf('=');
                if (equalsIndex > 0) {
                    infoMap.put(line.substring(0, equalsIndex), line.substring(equalsIndex + 1));
                }
            }

            try {
                return Future.succeededFuture(Integer.parseInt(infoMap.get("db")));
            } catch (Exception e) {
                return Future.failedFuture("Failed to parse Redis INFO response: " + response);
            }
        });
    }

    private void reconnectPubSub() {
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                // Ignore
            }
            connection = null;
        }
        cache.getVertx().setTimer(5000, id -> setupRedisSubscriptions());
    }

    private void handleRedisPubSubMessage(Response message) {
        if (message == null || message.type() == null) {
            log.warn("Received null or typeless message from Redis Pub/Sub");
            return;
        }

        if (MESSAGE.equalsIgnoreCase(message.get(0).toString())) {
            if (message.size() < 4) {
                throw new IllegalArgumentException("Received pmessage with unexpected size: " + message);
            }

            String pattern = message.get(1).toString();
            String channel = message.get(2).toString();
            String redisEvent = message.get(3).toString();

            log.debug("Received Redis pmessage: pattern=" + pattern + ", channel=" + channel + ", event=" + redisEvent);

            String key = parseKeyFromChannel(channel, redisEvent);
            CacheEvent.EventType eventType = translateRedisEvent(redisEvent, channel);

            if (eventType != null && key != null) {
                CacheEvent cacheEvent = new CacheEvent(eventType, key);
                log.debug("Publishing CacheEvent to Vert.x Event Bus: " + cacheEvent.toJson());
                eventBus.publish(eventAddress, cacheEvent.toJson());
            }
        }
    }

    private String parseKeyFromChannel(String channel, String redisEvent) {
        if (channel.startsWith(KEYSPACE)) {
            int lastColon = channel.lastIndexOf(':');
            if (lastColon > 0 && lastColon < channel.length() - 1) {
                return channel.substring(lastColon + 1);
            }
        } else if (channel.startsWith(KEYEVENT)) {
            return redisEvent;
        }
        return null;
    }

    private CacheEvent.EventType translateRedisEvent(String redisEvent, String channel) {
        if (channel.startsWith(KEYSPACE)) {
            return switch (redisEvent.toLowerCase()) {
                case "set", "hset", "hmset", "lset", "lpush", "rpush" -> CacheEvent.EventType.KEY_UPDATED;
                case "del", "hdel", "lrem" -> CacheEvent.EventType.KEY_DELETED;
                case "expire" -> CacheEvent.EventType.KEY_EXPIRED;
                default -> null;
            };
        } else if (channel.startsWith(KEYEVENT)) {
            if (channel.endsWith(":expired") || channel.endsWith(":evicted")) {
                return CacheEvent.EventType.KEY_EXPIRED;
            }
        }
        return null;
    }

    @Override
    public Vertx getVertx() {
        return cache.getVertx();
    }

    @Override
    public String getEventAddress() {
        return eventAddress;
    }

    @Override
    public Future<String> registerEventHandler(Handler<CacheEvent> handler) {
        String registrationId = UUID.randomUUID().toString();
        MessageConsumer<JsonObject> consumer = eventBus.consumer(eventAddress, message -> {
            try {
                handler.handle(new CacheEvent(message.body()));
            } catch (Exception e) {
                log.trace("Error in event handler", e);
            }
        });
        consumers.put(registrationId, consumer);
        return Future.succeededFuture(registrationId);
    }

    @Override
    public Future<String> registerEventHandler(CacheEvent.EventType type, Handler<CacheEvent> handler) {
        String registrationId = UUID.randomUUID().toString();
        MessageConsumer<JsonObject> consumer = eventBus.consumer(eventAddress, message -> {
            try {
                CacheEvent event = new CacheEvent(message.body());
                if (event.getType() == type) {
                    handler.handle(event);
                }
            } catch (Exception e) {
                log.trace("Error in event handler", e);
            }
        });
        consumers.put(registrationId, consumer);
        return Future.succeededFuture(registrationId);
    }

    @Override
    public Future<String> registerKeyEventHandler(String key, Handler<CacheEvent> handler) {
        String registrationId = UUID.randomUUID().toString();
        MessageConsumer<JsonObject> consumer = eventBus.consumer(eventAddress, message -> {
            try {
                CacheEvent event = new CacheEvent(message.body());
                if (key.equals(event.getKey())) {
                    handler.handle(event);
                }
            } catch (Exception e) {
                log.trace("Error in event handler", e);
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
        Future<Void> pubSubCloseFuture;
        if (connection != null) {
            connection.handler(null);
            connection.exceptionHandler(null);
            pubSubCloseFuture = connection.close();
        } else {
            pubSubCloseFuture = Future.succeededFuture();
        }

        pubSubCloseFuture.onSuccess(v -> {
            for (Map.Entry<String, MessageConsumer<JsonObject>> entry : consumers.entrySet()) {
                entry.getValue().unregister();
            }
            consumers.clear();
        }).onComplete(completion);
    }
}
