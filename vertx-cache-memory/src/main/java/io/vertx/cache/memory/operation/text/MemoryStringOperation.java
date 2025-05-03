package io.vertx.cache.memory.operation.text;

import io.vertx.cache.common.event.CacheEvent;
import io.vertx.cache.common.operation.text.StringOperation;
import io.vertx.cache.memory.MemoryCache;
import io.vertx.cache.memory.operation.MemoryValueOperation;
import io.vertx.core.Future;

/**
 * Implementation of the StringOperation interface using MemoryCache.
 */
public class MemoryStringOperation extends MemoryValueOperation<String> implements StringOperation {

    /**
     * Creates a new MemoryStringOperation.
     *
     * @param cache The MemoryCache to use
     */
    public MemoryStringOperation(MemoryCache cache) {
        super(cache, String.class, null, null);
    }

    @Override
    public Future<Long> length(String key) {
        return get(key).map(value -> value != null ? (long) value.length() : 0L);
    }

    @Override
    public Future<Integer> append(String key, String value) {
        String currentValue = cache.get(key);

        // Publish event for key read operation
        if (currentValue != null) {
            cache.events().publishEvent(CacheEvent.EventType.KEY_READ, key);
        }

        String newValue;

        if (currentValue == null) {
            newValue = value;
        } else {
            newValue = currentValue + value;
        }

        cache.put(key, newValue);
        return Future.succeededFuture(newValue.length());
    }

    @Override
    public Future<String> getRange(String key, int start, int end) {
        return get(key).map(value -> {
            if (value == null) {
                return "";
            }

            // Adjust indices to be within bounds
            int length = value.length();
            int adjustedStart = Math.max(0, start);
            int adjustedEnd = Math.min(length, end);

            if (adjustedStart >= length || adjustedEnd < 0 || adjustedStart > adjustedEnd) {
                return "";
            }

            return value.substring(adjustedStart, adjustedEnd);
        });
    }

    @Override
    public Future<Long> setRange(String key, long offset, String value) {
        String currentValue = cache.get(key);

        // Publish event for key read operation
        if (currentValue != null) {
            cache.events().publishEvent(CacheEvent.EventType.KEY_READ, key);
        }

        if (currentValue == null) {
            // If the key doesn't exist, create a new string with enough space
            if (offset > 0) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < offset; i++) {
                    sb.append('\0');
                }
                sb.append(value);
                String newValue = sb.toString();
                cache.put(key, newValue);
                return Future.succeededFuture((long) newValue.length());
            } else {
                cache.put(key, value);
                return Future.succeededFuture((long) value.length());
            }
        } else {
            // If the key exists, replace the substring
            int currentLength = currentValue.length();
            int offsetInt = (int) offset;

            if (offsetInt >= currentLength) {
                // If the offset is beyond the current length, pad with null bytes
                StringBuilder sb = new StringBuilder(currentValue);
                for (int i = currentLength; i < offsetInt; i++) {
                    sb.append('\0');
                }
                sb.append(value);
                String newValue = sb.toString();
                cache.put(key, newValue);
                return Future.succeededFuture((long) newValue.length());
            } else {
                // If the offset is within the current length, replace the substring
                StringBuilder sb = new StringBuilder(currentValue);
                for (int i = 0; i < value.length(); i++) {
                    int pos = offsetInt + i;
                    if (pos < currentLength) {
                        sb.setCharAt(pos, value.charAt(i));
                    } else {
                        sb.append(value.charAt(i));
                    }
                }
                String newValue = sb.toString();
                cache.put(key, newValue);
                return Future.succeededFuture((long) newValue.length());
            }
        }
    }
}
