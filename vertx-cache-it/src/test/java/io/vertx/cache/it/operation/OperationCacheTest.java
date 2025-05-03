package io.vertx.cache.it.operation;

import io.vertx.cache.it.AbstractCacheTest;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public abstract class OperationCacheTest extends AbstractCacheTest {

    @Test
    public void testBasicOperations(TestContext should) {
        cache.strings().set("key1", "value1")
                .compose(v -> cache.strings().get("key1"))
                .onComplete(should.asyncAssertSuccess(value -> should.assertEquals("value1", value)));
    }

    @Test
    public void testKeyOperations(TestContext should) {
        cache.value(String.class).set("key1", "value1")
                .compose(v -> cache.value(String.class).set("key2", "value2"))
                .compose(v -> cache.keys().keys())
                .onComplete(should.asyncAssertSuccess(keys -> {
                    should.assertTrue(keys.contains("key1"));
                    should.assertTrue(keys.contains("key2"));
                    should.assertEquals(2, keys.size());
                }));
    }

    @Test
    public void testDeleteOperation(TestContext should) {
        cache.value(String.class).set("key1", "value1")
                .compose(v -> cache.value(String.class).set("key2", "value2"))
                .compose(v -> cache.value(String.class).delete("key1"))
                .compose(v -> cache.keys().keys())
                .onComplete(should.asyncAssertSuccess(keys -> {
                    should.assertTrue(keys.contains("key2"));
                    should.assertFalse(keys.contains("key1"));
                    should.assertEquals(1, keys.size());
                }));
    }

    @Test
    public void testExpiration(TestContext should) {
        cache.value(String.class).set("key1", "value1", 100, TimeUnit.MILLISECONDS)
                .compose(v -> {
                    try {
                        TimeUnit.MILLISECONDS.sleep(200);
                    } catch (InterruptedException e) {
                        return Future.failedFuture(e);
                    }
                    return Future.succeededFuture();
                })
                .compose(v -> cache.value(String.class).get("key1"))
                .onComplete(should.asyncAssertSuccess(should::assertNull));
    }

    @Test
    public void testClearOperation(TestContext should) {
        cache.value(String.class).set("key1", "value1")
                .compose(v -> cache.value(String.class).set("key2", "value2"))
                .compose(v -> cache.clear())
                .compose(v -> cache.keys().keys())
                .onComplete(should.asyncAssertSuccess(keys -> should.assertTrue(keys.isEmpty())));
    }

    @Test
    public void testStringOperations(TestContext should) {
        cache.strings().set("key1", "Hello")
                .compose(v -> cache.strings().append("key1", " World"))
                .compose(v -> cache.strings().get("key1"))
                .onComplete(should.asyncAssertSuccess(value -> should.assertEquals("Hello World", value)));
    }

    @Test
    public void testNumberOperations(TestContext should) {
        cache.integers().set("counter", 0L)
                .compose(v -> cache.integers().increment("counter"))
                .compose(v -> cache.integers().increment("counter"))
                .compose(v -> cache.integers().get("counter"))
                .onComplete(should.asyncAssertSuccess(value -> should.assertEquals(2L, value)));
    }

    @Test
    public void testExistsOperation(TestContext should) {
        cache.strings().set("key1", "value1")
                .compose(v -> cache.strings().exists("key1"))
                .onComplete(should.asyncAssertSuccess(should::assertTrue));
    }

    @Test
    public void testNotExistsOperation(TestContext should) {
        cache.strings().set("key1", "value1")
                .compose(v -> cache.strings().exists("key2"))
                .onComplete(should.asyncAssertSuccess(should::assertFalse));
    }

    @Test
    public void testJsonOperations(TestContext should) {
        JsonObject user = new JsonObject()
                .put("id", 123)
                .put("name", "John Doe")
                .put("email", "john@example.com");

        cache.jsonOperation().set("user:123", user)
                .compose(v -> cache.jsonOperation().get("user:123"))
                .onComplete(should.asyncAssertSuccess(retrievedUser -> {
                    should.assertEquals(123, retrievedUser.getInteger("id"));
                    should.assertEquals("John Doe", retrievedUser.getString("name"));
                    should.assertEquals("john@example.com", retrievedUser.getString("email"));
                }));
    }

    @Test
    public void testBinaryOperations(TestContext should) {
        byte[] data = new byte[] { 1, 2, 3, 4, 5 };

        cache.binaryOperation().set("binary:key", Buffer.buffer(data))
                .compose(v -> cache.binaryOperation().get("binary:key"))
                .onComplete(should.asyncAssertSuccess(retrievedData -> {
                    should.assertEquals(data.length, retrievedData.length());
                    for (int i = 0; i < data.length; i++) {
                        should.assertEquals(data[i], retrievedData.getByte(i));
                    }
                }));
    }
}
