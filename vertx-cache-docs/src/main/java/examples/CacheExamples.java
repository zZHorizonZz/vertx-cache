package examples;

import io.vertx.cache.common.Cache;
import io.vertx.cache.common.event.CacheEvent;
import io.vertx.cache.distributed.DistributedCache;
import io.vertx.cache.distributed.DistributedCacheOptions;
import io.vertx.cache.memory.MemoryCache;
import io.vertx.cache.memory.MemoryCacheOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.docgen.Source;
import io.vertx.redis.client.RedisOptions;

import java.util.concurrent.TimeUnit;

/**
 * Examples of using Vert.x Cache implementations
 */
@Source
public class CacheExamples {

    /**
     * Example of creating a memory cache
     */
    public void createMemoryCache(Vertx vertx) {
        // tag::createMemoryCache[]
        // Create a memory cache with default options
        MemoryCache cache = MemoryCache.create(vertx);
        // end::createMemoryCache[]
    }

    /**
     * Example of creating a memory cache with options
     */
    public void createMemoryCacheWithOptions(Vertx vertx) {
        // tag::createMemoryCacheWithOptions[]
        // Create memory cache options
        MemoryCacheOptions options = new MemoryCacheOptions()
                .setMaximumSize(10000)      // Maximum number of entries
                .setDefaultTtl(3600);       // Default TTL in seconds

        // Create a memory cache with options
        MemoryCache cache = MemoryCache.create(vertx, options);
        // end::createMemoryCacheWithOptions[]
    }

    /**
     * Example of creating a distributed cache
     */
    public void createDistributedCache(Vertx vertx) {
        // tag::createDistributedCache[]
        // Create a distributed cache with default options
        DistributedCache cache = DistributedCache.create(vertx);
        // end::createDistributedCache[]
    }

    /**
     * Example of creating a distributed cache with options
     */
    public void createDistributedCacheWithOptions(Vertx vertx) {
        // tag::createDistributedCacheWithOptions[]
        // Create distributed cache options
        DistributedCacheOptions options = new DistributedCacheOptions()
                .setRedisOptions(new RedisOptions()
                        .setConnectionString("redis://localhost:6379")
                        .setPassword("<PASSWORD>")
                );

        // Create a distributed cache with options
        DistributedCache cache = DistributedCache.create(vertx, options);
        // end::createDistributedCacheWithOptions[]
    }

    /**
     * Example of basic string operations that work with any Cache implementation
     */
    public void stringOperations(Cache cache) {
        // tag::stringOperations[]
        // Set a string value
        Future<Void> setFuture = cache.strings().set("greeting", "Hello, World!");
        setFuture.onSuccess(result -> {
            System.out.println("Value set successfully");
        });

        // Set a string value with TTL
        Future<Void> setWithTtlFuture = cache.strings().set("session", "session-data", 1800, TimeUnit.SECONDS); // 30 minutes
        setWithTtlFuture.onSuccess(result -> {
            System.out.println("Session set with TTL successfully");
        });

        // Get a string value
        Future<String> getFuture = cache.strings().get("greeting");
        getFuture.onSuccess(value -> {
            System.out.println("Retrieved value: " + value);
        });
        // end::stringOperations[]
    }

    /**
     * Example of working with JSON objects using any Cache implementation
     */
    public void jsonOperations(Cache cache) {
        // tag::jsonOperations[]
        // Create a JSON object
        JsonObject user = new JsonObject()
                .put("id", 123)
                .put("name", "John Doe")
                .put("email", "john@example.com");

        // Store the JSON object
        Future<Void> setFuture = cache.jsonOperation().set("user:123", user);
        setFuture.onSuccess(result -> {
            System.out.println("User stored successfully");
        });

        // Retrieve the JSON object
        Future<JsonObject> getFuture = cache.jsonOperation().get("user:123");
        getFuture.onSuccess(retrievedUser -> {
            System.out.println("Retrieved user: " + retrievedUser.getString("name"));
        });
        // end::jsonOperations[]
    }

    /**
     * Example of working with binary data using any Cache implementation
     */
    public void binaryOperations(Cache cache) {
        // tag::binaryOperations[]
        // Create some binary data
        byte[] data = new byte[] { 1, 2, 3, 4, 5 };

        // Store the binary data
        Future<Void> setFuture = cache.binaryOperation().set("binary:key", Buffer.buffer(data));
        setFuture.onSuccess(result -> {
            System.out.println("Binary data stored successfully");
        });

        // Retrieve the binary data
        Future<Buffer> getFuture = cache.binaryOperation().get("binary:key");
        getFuture.onSuccess(retrievedData -> {
            System.out.println("Retrieved binary data length: " + retrievedData.length());
        });
        // end::binaryOperations[]
    }

    /**
     * Example of setting a TTL (Time-To-Live) for cache entries
     */
    public void ttlExample(Cache cache) {
        // tag::ttlExample[]
        // Set a value with a TTL of 30 minutes
        cache.strings().set("key", "value", 30, TimeUnit.MINUTES);
        // end::ttlExample[]
    }

    /**
     * Example of using cache events
     */
    public void cacheEvents(Cache cache) {
        // tag::cacheEvents[]
        // Register a handler for all cache events
        Future<String> registrationFuture = cache.events().registerEventHandler(event -> {
            System.out.println("Event type: " + event.getType());
            System.out.println("Key: " + event.getKey());
        });

        // Register a handler for specific event type
        Future<String> keyCreatedFuture = cache.events().registerEventHandler(
            CacheEvent.EventType.KEY_CREATED, 
            event -> {
                System.out.println("Key created: " + event.getKey());
            }
        );

        // Register a handler for events on a specific key
        Future<String> keyEventFuture = cache.events().registerKeyEventHandler(
            "important-key", 
            event -> {
                System.out.println("Event on important key: " + event.getType());
            }
        );

        // Unregister a handler when no longer needed
        registrationFuture.onSuccess(registrationId -> {
            // Later, when you want to unregister:
            cache.events().unregisterEventHandler(registrationId);
        });
        // end::cacheEvents[]
    }
}
