package io.vertx.cache.it.event;

import io.vertx.cache.common.event.CacheEvent;
import io.vertx.cache.it.AbstractCacheTest;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public abstract class EventCacheTest extends AbstractCacheTest {

    @Test
    public void testKeyUpdatedEvent(TestContext should) {
        Async async = should.async();

        cache.strings().set("testKey", "initialValue")
                .compose(v -> cache.events().registerEventHandler(CacheEvent.EventType.KEY_UPDATED, event -> {
                    if (event.getKey().equals("testKey")) {
                        should.assertEquals(CacheEvent.EventType.KEY_UPDATED, event.getType());
                        should.assertEquals("testKey", event.getKey());
                        async.complete();
                    }
                }))
                .onFailure(should::fail)
                .onComplete(should.asyncAssertSuccess(id -> cache.strings().set("testKey", "updatedValue")));

        async.awaitSuccess(5000);
    }

    @Test
    public void testKeyDeletedEvent(TestContext should) {
        Async async = should.async();

        cache.strings().set("testKey", "testValue")
                .compose(v -> cache.events().registerEventHandler(CacheEvent.EventType.KEY_DELETED, event -> {
                    if (event.getKey().equals("testKey")) {
                        should.assertEquals(CacheEvent.EventType.KEY_DELETED, event.getType());
                        should.assertEquals("testKey", event.getKey());
                        async.complete();
                    }
                }))
                .onFailure(should::fail)
                .onComplete(should.asyncAssertSuccess(id -> cache.strings().delete("testKey")));

        async.awaitSuccess(5000);
    }

    @Test
    public void testCacheClearedEvent(TestContext should) {
        Async async = should.async();

        cache.strings().set("key1", "value1")
                .compose(v -> cache.strings().set("key2", "value2"))
                .compose(v -> cache.events().registerEventHandler(CacheEvent.EventType.CACHE_CLEARED, event -> {
                    should.assertEquals(CacheEvent.EventType.CACHE_CLEARED, event.getType());
                    should.assertNull(event.getKey());
                    if (!async.isCompleted()) {
                        async.complete();
                    }
                }))
                .onFailure(should::fail)
                .onComplete(should.asyncAssertSuccess(id -> cache.clear()));

        async.awaitSuccess(5000);
    }
}
