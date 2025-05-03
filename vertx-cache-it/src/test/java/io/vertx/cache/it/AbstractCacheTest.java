package io.vertx.cache.it;

import io.vertx.cache.common.Cache;
import io.vertx.cache.common.event.CacheEvent;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(VertxExtension.class)
public abstract class AbstractCacheTest {

    protected Vertx vertx;
    protected Cache cache;

    protected abstract Cache createCache(Vertx vertx);

    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        this.vertx = vertx;
        this.cache = createCache(vertx);

        testContext.completeNow();
    }

    @AfterEach
    void tearDown(VertxTestContext testContext) {
        if (cache != null) {
            try {
                cache.close();
                testContext.completeNow();
            } catch (Exception e) {
                testContext.failNow(e);
            }
        } else {
            testContext.completeNow();
        }
    }

    @Test
    void testBasicOperations(VertxTestContext testContext) {
        // Test basic put and get operations
        cache.value(String.class).set("key1", "value1")
                .compose(v -> cache.value(String.class).get("key1"))
                .onComplete(testContext.succeeding(value -> {
                    testContext.verify(() -> {
                        assertThat(value).isEqualTo("value1");
                        testContext.completeNow();
                    });
                }));
    }

    @Test
    void testKeyOperations(VertxTestContext testContext) {
        // Test key operations
        cache.value(String.class).set("key1", "value1")
                .compose(v -> cache.value(String.class).set("key2", "value2"))
                .compose(v -> cache.keys().keys())
                .onComplete(testContext.succeeding(keys -> {
                    testContext.verify(() -> {
                        assertThat(keys).containsExactlyInAnyOrder("key1", "key2");
                        testContext.completeNow();
                    });
                }));
    }

    @Test
    void testDeleteOperation(VertxTestContext testContext) {
        // Test delete operation
        cache.value(String.class).set("key1", "value1")
                .compose(v -> cache.value(String.class).set("key2", "value2"))
                .compose(v -> cache.value(String.class).delete("key1"))
                .compose(v -> cache.keys().keys())
                .onComplete(testContext.succeeding(keys -> {
                    testContext.verify(() -> {
                        assertThat(keys).containsExactly("key2");
                        testContext.completeNow();
                    });
                }));
    }

    @Test
    void testExpiration(VertxTestContext testContext) throws InterruptedException {
        // Test expiration
        cache.value(String.class).set("key1", "value1", Duration.ofMillis(100))
                .compose(v -> {
                    // Wait for expiration
                    try {
                        TimeUnit.MILLISECONDS.sleep(200);
                    } catch (InterruptedException e) {
                        return Future.failedFuture(e);
                    }
                    return Future.succeededFuture();
                })
                .compose(v -> cache.value(String.class).get("key1"))
                .onComplete(testContext.succeeding(value -> {
                    testContext.verify(() -> {
                        assertThat(value).isNull();
                        testContext.completeNow();
                    });
                }));
    }

    @Test
    void testClearOperation(VertxTestContext testContext) {
        // Test clear operation
        cache.value(String.class).set("key1", "value1")
                .compose(v -> cache.value(String.class).set("key2", "value2"))
                .compose(v -> cache.clear())
                .compose(v -> cache.keys().keys())
                .onComplete(testContext.succeeding(keys -> {
                    testContext.verify(() -> {
                        assertThat(keys).isEmpty();
                        testContext.completeNow();
                    });
                }));
    }

    @Test
    void testStringOperations(VertxTestContext testContext) {
        // Test string operations
        cache.strings().set("key1", "Hello")
                .compose(v -> cache.strings().append("key1", " World"))
                .compose(v -> cache.strings().get("key1"))
                .onComplete(testContext.succeeding(value -> {
                    testContext.verify(() -> {
                        assertThat(value).isEqualTo("Hello World");
                        testContext.completeNow();
                    });
                }));
    }

    @Test
    void testNumberOperations(VertxTestContext testContext) {
        // Test number operations
        cache.integers().set("counter", 0L)
                .compose(v -> cache.integers().increment("counter"))
                .compose(v -> cache.integers().increment("counter"))
                .compose(v -> cache.integers().get("counter"))
                .onComplete(testContext.succeeding(value -> {
                    testContext.verify(() -> {
                        assertThat(value).isEqualTo(2L);
                        testContext.completeNow();
                    });
                }));
    }

    @Test
    void testKeyCreatedEvent(VertxTestContext testContext) {
        // Create a list to store received events
        List<CacheEvent> receivedEvents = new ArrayList<>();

        // Register a handler for KEY_CREATED events
        cache.events().registerEventHandler(CacheEvent.EventType.KEY_CREATED, event -> {
            receivedEvents.add(event);
            if (event.getKey().equals("testKey")) {
                testContext.verify(() -> {
                    assertThat(event.getType()).isEqualTo(CacheEvent.EventType.KEY_CREATED);
                    assertThat(event.getKey()).isEqualTo("testKey");
                    testContext.completeNow();
                });
            }
        }).onComplete(testContext.succeeding(id -> {
            // Set a value to trigger the KEY_CREATED event
            cache.value(String.class).set("testKey", "testValue");
        }));
    }

    @Test
    void testKeyUpdatedEvent(VertxTestContext testContext) {
        // Create a list to store received events
        List<CacheEvent> receivedEvents = new ArrayList<>();

        // First set a value
        cache.value(String.class).set("testKey", "initialValue")
            .compose(v -> {
                // Register a handler for KEY_UPDATED events
                return cache.events().registerEventHandler(CacheEvent.EventType.KEY_UPDATED, event -> {
                    receivedEvents.add(event);
                    if (event.getKey().equals("testKey")) {
                        testContext.verify(() -> {
                            assertThat(event.getType()).isEqualTo(CacheEvent.EventType.KEY_UPDATED);
                            assertThat(event.getKey()).isEqualTo("testKey");
                            testContext.completeNow();
                        });
                    }
                });
            })
            .onComplete(testContext.succeeding(id -> {
                // Update the value to trigger the KEY_UPDATED event
                cache.value(String.class).set("testKey", "updatedValue");
            }));
    }

    @Test
    void testKeyDeletedEvent(VertxTestContext testContext) {
        // Create a list to store received events
        List<CacheEvent> receivedEvents = new ArrayList<>();

        // First set a value
        cache.value(String.class).set("testKey", "testValue")
            .compose(v -> {
                // Register a handler for KEY_DELETED events
                return cache.events().registerEventHandler(CacheEvent.EventType.KEY_DELETED, event -> {
                    receivedEvents.add(event);
                    if (event.getKey().equals("testKey")) {
                        testContext.verify(() -> {
                            assertThat(event.getType()).isEqualTo(CacheEvent.EventType.KEY_DELETED);
                            assertThat(event.getKey()).isEqualTo("testKey");
                            testContext.completeNow();
                        });
                    }
                });
            })
            .onComplete(testContext.succeeding(id -> {
                // Delete the value to trigger the KEY_DELETED event
                cache.value(String.class).delete("testKey");
            }));
    }

    @Test
    void testKeyExpiredEvent(VertxTestContext testContext) throws InterruptedException {
        // Create a list to store received events
        List<CacheEvent> receivedEvents = new ArrayList<>();

        // Register a handler for KEY_EXPIRED events
        cache.events().registerEventHandler(CacheEvent.EventType.KEY_EXPIRED, event -> {
            receivedEvents.add(event);
            if (event.getKey().equals("testKey")) {
                testContext.verify(() -> {
                    assertThat(event.getType()).isEqualTo(CacheEvent.EventType.KEY_EXPIRED);
                    assertThat(event.getKey()).isEqualTo("testKey");
                    testContext.completeNow();
                });
            }
        }).onComplete(testContext.succeeding(id -> {
            // Set a value with a short TTL to trigger the KEY_EXPIRED event
            cache.value(String.class).set("testKey", "testValue", Duration.ofMillis(100));
        }));

        // Wait for the expiration to occur
        testContext.awaitCompletion(5, TimeUnit.SECONDS);
    }

    @Test
    void testCacheClearedEvent(VertxTestContext testContext) {
        // Create a list to store received events
        List<CacheEvent> receivedEvents = new ArrayList<>();

        // First set some values
        cache.value(String.class).set("key1", "value1")
            .compose(v -> cache.value(String.class).set("key2", "value2"))
            .compose(v -> {
                // Register a handler for CACHE_CLEARED events
                return cache.events().registerEventHandler(CacheEvent.EventType.CACHE_CLEARED, event -> {
                    receivedEvents.add(event);
                    testContext.verify(() -> {
                        assertThat(event.getType()).isEqualTo(CacheEvent.EventType.CACHE_CLEARED);
                        assertThat(event.getKey()).isNull();
                        testContext.completeNow();
                    });
                });
            })
            .onComplete(testContext.succeeding(id -> {
                // Clear the cache to trigger the CACHE_CLEARED event
                cache.clear();
            }));
    }

    @Test
    void testMultipleEvents(VertxTestContext testContext) {
        // Create a list to store received events
        List<CacheEvent> receivedEvents = new ArrayList<>();
        AtomicReference<String> registrationId = new AtomicReference<>();

        // Register a handler for all events
        cache.events().registerEventHandler(event -> {
            receivedEvents.add(event);

            // If we've received 3 events (CREATED, UPDATED, DELETED), complete the test
            if (receivedEvents.size() == 3) {
                testContext.verify(() -> {
                    assertThat(receivedEvents).hasSize(3);

                    // Verify the first event is KEY_CREATED
                    assertThat(receivedEvents.get(0).getType()).isEqualTo(CacheEvent.EventType.KEY_CREATED);
                    assertThat(receivedEvents.get(0).getKey()).isEqualTo("testKey");

                    // Verify the second event is KEY_UPDATED
                    assertThat(receivedEvents.get(1).getType()).isEqualTo(CacheEvent.EventType.KEY_UPDATED);
                    assertThat(receivedEvents.get(1).getKey()).isEqualTo("testKey");

                    // Verify the third event is KEY_DELETED
                    assertThat(receivedEvents.get(2).getType()).isEqualTo(CacheEvent.EventType.KEY_DELETED);
                    assertThat(receivedEvents.get(2).getKey()).isEqualTo("testKey");

                    // Unregister the handler
                    cache.events().unregisterEventHandler(registrationId.get())
                        .onComplete(testContext.succeeding(v -> testContext.completeNow()));
                });
            }
        }).onComplete(testContext.succeeding(id -> {
            registrationId.set(id);

            // Perform operations to trigger events
            cache.value(String.class).set("testKey", "initialValue")
                .compose(v -> cache.value(String.class).set("testKey", "updatedValue"))
                .compose(v -> cache.value(String.class).delete("testKey"));
        }));
    }
}
