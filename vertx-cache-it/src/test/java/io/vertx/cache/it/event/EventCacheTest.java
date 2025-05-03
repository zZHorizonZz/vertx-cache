package io.vertx.cache.it.event;

import io.vertx.cache.common.event.CacheEvent;
import io.vertx.cache.it.AbstractCacheTest;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public abstract class EventCacheTest extends AbstractCacheTest {
    @Test
    public void testKeyCreatedEvent(TestContext should) {
        cache.events().registerEventHandler(CacheEvent.EventType.KEY_CREATED, event -> {
                    if (event.getKey().equals("testKey")) {
                        should.assertEquals(CacheEvent.EventType.KEY_CREATED, event.getType());
                        should.assertEquals("testKey", event.getKey());
                    }
                })
                .onFailure(should::fail)
                .onComplete(should.asyncAssertSuccess(id -> cache.strings().set("testKey", "testValue")));
    }

    @Test
    public void testKeyUpdatedEvent(TestContext should) {
        cache.strings().set("testKey", "initialValue")
                .compose(v -> cache.events().registerEventHandler(CacheEvent.EventType.KEY_UPDATED, event -> {
                    if (event.getKey().equals("testKey")) {
                        should.assertEquals(CacheEvent.EventType.KEY_UPDATED, event.getType());
                        should.assertEquals("testKey", event.getKey());
                    }
                }))
                .onFailure(should::fail)
                .onComplete(should.asyncAssertSuccess(id -> cache.strings().set("testKey", "updatedValue")));
    }

    @Test
    public void testKeyDeletedEvent(TestContext should) {
        cache.strings().set("testKey", "testValue")
                .compose(v -> cache.events().registerEventHandler(CacheEvent.EventType.KEY_DELETED, event -> {
                    if (event.getKey().equals("testKey")) {
                        should.assertEquals(CacheEvent.EventType.KEY_DELETED, event.getType());
                        should.assertEquals("testKey", event.getKey());
                    }
                }))
                .onFailure(should::fail)
                .onComplete(should.asyncAssertSuccess(id -> cache.strings().delete("testKey")));
    }

    @Test
    public void testCacheClearedEvent(TestContext should) {
        cache.strings().set("key1", "value1")
                .compose(v -> cache.strings().set("key2", "value2"))
                .compose(v -> cache.events().registerEventHandler(CacheEvent.EventType.CACHE_CLEARED, event -> {
                    should.assertEquals(CacheEvent.EventType.CACHE_CLEARED, event.getType());
                    should.assertNull(event.getKey());
                }))
                .onFailure(should::fail)
                .onComplete(should.asyncAssertSuccess(id -> cache.clear()));
    }

    @Test
    public void testMultipleEvents(TestContext should) {
        AtomicInteger receivedEvents = new AtomicInteger(0);

        AtomicBoolean receivedKeyCreatedEvent = new AtomicBoolean(false);
        AtomicBoolean receivedKeyUpdatedEvent = new AtomicBoolean(false);
        AtomicBoolean receivedKeyDeletedEvent = new AtomicBoolean(false);

        AtomicReference<String> registrationId = new AtomicReference<>();

        cache.events().registerEventHandler(event -> {
                    receivedEvents.incrementAndGet();

                    switch (event.getType()) {
                        case KEY_CREATED -> {
                            receivedKeyCreatedEvent.set(true);
                            should.assertEquals("testKey", event.getKey());
                        }
                        case KEY_UPDATED -> {
                            receivedKeyUpdatedEvent.set(true);
                            should.assertEquals("testKey", event.getKey());
                        }
                        case KEY_DELETED -> {
                            receivedKeyDeletedEvent.set(true);
                            should.assertEquals("testKey", event.getKey());
                        }
                    }

                    if (receivedEvents.get() == 3) {
                        should.assertTrue(receivedKeyCreatedEvent.get());
                        should.assertTrue(receivedKeyUpdatedEvent.get());
                        should.assertTrue(receivedKeyDeletedEvent.get());
                        cache.events().unregisterEventHandler(registrationId.get()).onComplete(should.asyncAssertSuccess());
                    }
                })
                .onFailure(should::fail)
                .onComplete(should.asyncAssertSuccess(id -> {
                    registrationId.set(id);

                    cache.strings().set("testKey", "initialValue")
                            .compose(v -> cache.strings().set("testKey", "updatedValue"))
                            .compose(v -> cache.strings().delete("testKey"));
                }));
    }
}
