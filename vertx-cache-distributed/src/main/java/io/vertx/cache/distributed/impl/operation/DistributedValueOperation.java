package io.vertx.cache.distributed.impl.operation;

import io.vertx.cache.common.event.CacheEvent;
import io.vertx.cache.common.operation.ValueOperation;
import io.vertx.cache.common.serialization.CacheDeserializer;
import io.vertx.cache.common.serialization.CacheSerializer;
import io.vertx.cache.distributed.impl.DistributedCacheImpl;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of the ValueOperation interface using Redis.
 *
 * @param <T> The type of values this operation works with
 */
public class DistributedValueOperation<T> implements ValueOperation<T> {

    protected final DistributedCacheImpl cache;

    private final Class<T> valueClass;
    private final CacheSerializer<T> serializer;
    private final CacheDeserializer<T> deserializer;

    public DistributedValueOperation(DistributedCacheImpl cache, Class<T> valueClass,
            CacheSerializer<T> serializer, CacheDeserializer<T> deserializer) {
        this.cache = cache;
        this.valueClass = valueClass;
        this.serializer = serializer != null ? serializer : createDefaultSerializer();
        this.deserializer = deserializer != null ? deserializer : createDefaultDeserializer();
    }

    private CacheSerializer<T> createDefaultSerializer() {
        return value -> {
            // For null values, return null
            if (value == null) {
                return null;
            }
            // For non-null values, convert to string and then to bytes
            return Buffer.buffer(value.toString().getBytes());
        };
    }

    private CacheDeserializer<T> createDefaultDeserializer() {
        return data -> {
            // For null values, return null
            if (data == null) {
                return null;
            }

            String str = new String(data.getBytes());

            // Try to convert string to the target type
            try {
                if (valueClass == String.class) {
                    return valueClass.cast(str);
                } else if (valueClass == Integer.class) {
                    return valueClass.cast(Integer.parseInt(str));
                } else if (valueClass == Long.class) {
                    return valueClass.cast(Long.parseLong(str));
                } else if (valueClass == Double.class) {
                    return valueClass.cast(Double.parseDouble(str));
                } else if (valueClass == Boolean.class) {
                    return valueClass.cast(Boolean.parseBoolean(str));
                } else {
                    // For other types, return null
                    return null;
                }
            } catch (Exception e) {
                return null;
            }
        };
    }

    @Override
    public Future<T> get(String key) {
        return get(key, deserializer);
    }

    @Override
    public Future<T> get(String key, CacheDeserializer<T> deserializer) {
        String prefixedKey = cache.prefixKey(key);

        return cache.getRedis().get(prefixedKey)
                .compose(response -> {
                    if (response == null) {
                        return Future.succeededFuture(null);
                    }

                    // Deserialize the value
                    byte[] bytes = response.toString().getBytes();
                    T value = deserializer.deserialize(Buffer.buffer(bytes));

                    if (value != null) {
                        cache.events().publishEvent(CacheEvent.EventType.KEY_READ, key);
                    }

                    return Future.succeededFuture(value);
                });
    }

    @Override
    public Future<T> getAndSet(String key, T value) {
        return get(key).compose(oldValue -> cache.put(key, serializer.serialize(value).toString()).compose(v -> Future.succeededFuture(oldValue)));
    }

    @Override
    public Future<T> getAndDelete(String key) {
        return get(key).compose(oldValue -> cache.remove(key).compose(v -> Future.succeededFuture(oldValue)));
    }

    @Override
    public Future<Void> set(String key, T value) {
        return cache.put(key, serializer.serialize(value).toString()).compose(v -> Future.succeededFuture());
    }

    @Override
    public Future<Void> set(String key, T value, CacheSerializer<T> serializer) {
        Buffer data = serializer.serialize(value);
        if (data == null) {
            throw new IllegalArgumentException("The serialized value cannot be null");
        }

        return cache.getRedis().set(List.of(cache.prefixKey(key), data.toString())).compose(response -> {
            cache.events().publishEvent(CacheEvent.EventType.KEY_CREATED, key);
            return Future.succeededFuture();
        });
    }

    @Override
    public Future<Void> set(String key, T value, long ttl, TimeUnit unit) {
        return cache.put(key, serializer.serialize(value).toString(), unit.toMillis(ttl)).compose(v -> Future.succeededFuture());
    }

    @Override
    public Future<Void> set(String key, T value, long ttl, TimeUnit unit, CacheSerializer<T> serializer) {
        String prefixedKey = cache.prefixKey(key);

        // Serialize the value
        Buffer data = serializer.serialize(value);
        String serializedValue = data != null ? data.toString() : null;

        // Set the value in Redis with expiration
        List<String> args = new ArrayList<>();
        args.add(prefixedKey);
        args.add(serializedValue);
        args.add("PX");
        args.add(String.valueOf(unit.toMillis(ttl)));

        return cache.getRedis().set(args).compose(response -> {
            cache.events().publishEvent(CacheEvent.EventType.KEY_CREATED, key);
            return Future.succeededFuture();
        });
    }

    @Override
    public Future<Void> setIfAbsent(String key, T value) {
        String prefixedKey = cache.prefixKey(key);

        // Use Redis SETNX command to set the value only if the key doesn't exist
        return cache.getRedis().setnx(prefixedKey, serializer.serialize(value).toString()).compose(response -> {
            if (response.toInteger() == 1) {
                // Key was set
                cache.events().publishEvent(CacheEvent.EventType.KEY_CREATED, key);
            } else {
                // Key already exists
                cache.events().publishEvent(CacheEvent.EventType.KEY_READ, key);
            }
            return Future.succeededFuture();
        });
    }

    @Override
    public Future<Void> setIfAbsent(String key, T value, long ttl, TimeUnit unit) {
        String prefixedKey = cache.prefixKey(key);

        // Use Redis SET command with NX and PX options
        List<String> args = new ArrayList<>();
        args.add(prefixedKey);
        args.add(serializer.serialize(value).toString());
        args.add("NX");
        args.add("PX");
        args.add(String.valueOf(unit.toMillis(ttl)));

        return cache.getRedis().set(args).compose(response -> {
            if (response != null && "OK".equalsIgnoreCase(response.toString())) {
                // Key was set
                cache.events().publishEvent(CacheEvent.EventType.KEY_CREATED, key);
            } else {
                // Key already exists
                cache.events().publishEvent(CacheEvent.EventType.KEY_READ, key);
            }
            return Future.succeededFuture();
        });
    }

    @Override
    public Future<Boolean> exists(String... key) {
        // Prefix all keys
        String[] prefixedKeys = new String[key.length];
        for (int i = 0; i < key.length; i++) {
            prefixedKeys[i] = cache.prefixKey(key[i]);
        }

        // Convert to List for Redis API
        List<String> keysList = List.of(prefixedKeys);
        return cache.getRedis().exists(keysList).map(response -> response.toInteger() == key.length);
    }

    @Override
    public Future<Void> delete(String... keys) {
        // Prefix all keys
        String[] prefixedKeys = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {
            prefixedKeys[i] = cache.prefixKey(keys[i]);
        }

        // Convert to List for Redis API
        List<String> keysList = List.of(prefixedKeys);

        // Delete the keys
        return cache.getRedis().del(keysList).compose(response -> {
            // Publish events for each deleted key
            for (String key : keys) {
                cache.events().publishEvent(CacheEvent.EventType.KEY_DELETED, key);
            }
            return Future.succeededFuture();
        });
    }

    @Override
    public CacheSerializer<T> getSerializer() {
        return serializer;
    }

    @Override
    public CacheDeserializer<T> getDeserializer() {
        return deserializer;
    }
}
