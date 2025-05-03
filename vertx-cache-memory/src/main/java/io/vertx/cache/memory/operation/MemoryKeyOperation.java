package io.vertx.cache.memory.operation;

import io.vertx.cache.common.event.CacheEvent;
import io.vertx.cache.common.operation.KeyOperation;
import io.vertx.cache.memory.MemoryCache;
import io.vertx.core.Future;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Implementation of the KeyOperation interface using MemoryCache.
 */
public class MemoryKeyOperation implements KeyOperation {

    private final MemoryCache cache;

    /**
     * Creates a new MemoryKeyOperation.
     *
     * @param cache The MemoryCache to use
     */
    public MemoryKeyOperation(MemoryCache cache) {
        this.cache = cache;
    }

    @Override
    public Future<Set<String>> keys(String pattern) {
        // Convert glob pattern to regex pattern
        String regex = globToRegex(pattern);
        Pattern compiledPattern = Pattern.compile(regex);

        // Get all keys from the cache
        Set<String> allKeys = cache.getCache().keySet();

        // Filter keys that match the pattern
        Set<String> matchingKeys = allKeys.stream()
                .filter(key -> compiledPattern.matcher(key).matches())
                .collect(Collectors.toSet());

        // Publish event for keys listed operation
        cache.events().publishEvent(CacheEvent.EventType.KEYS_LISTED, pattern);

        return Future.succeededFuture(matchingKeys);
    }

    @Override
    public Future<Set<String>> keys() {
        // Get all keys from the cache
        Set<String> allKeys = new HashSet<>(cache.getCache().keySet());

        // Publish event for keys listed operation
        cache.events().publishEvent(CacheEvent.EventType.KEYS_LISTED, "*");

        return Future.succeededFuture(allKeys);
    }

    /**
     * Converts a glob pattern to a regex pattern.
     * 
     * @param glob The glob pattern to convert
     * @return The regex pattern
     */
    private String globToRegex(String glob) {
        StringBuilder regex = new StringBuilder("^");
        for (int i = 0; i < glob.length(); i++) {
            char c = glob.charAt(i);
            switch (c) {
                case '*':
                    regex.append(".*");
                    break;
                case '?':
                    regex.append(".");
                    break;
                case '.':
                case '(':
                case ')':
                case '+':
                case '|':
                case '^':
                case '$':
                case '@':
                case '%':
                    regex.append("\\");
                    regex.append(c);
                    break;
                default:
                    regex.append(c);
            }
        }
        regex.append("$");
        return regex.toString();
    }
}
