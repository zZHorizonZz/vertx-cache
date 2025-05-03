package io.vertx.cache.common.operation.text;

import io.vertx.cache.common.operation.CacheOperation;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;

/**
 * Interface for string-related operations in the cache.
 */
@VertxGen
public interface StringOperation extends CacheOperation<String> {
    /**
     * Gets the length of the string value stored at a key.
     *
     * @param key The key to get the length for
     * @return A Future that will be completed with the length of the string, or 0 if the key doesn't exist
     */
    Future<Long> length(String key);

    /**
     * Appends a string to the value of a key.
     *
     * @param key The key to append to
     * @param value The string to append
     * @return A Future that will be completed with the length of the string after the append operation
     */
    Future<Integer> append(String key, String value);

    /**
     * Gets a substring of the string stored at a key.
     *
     * @param key The key to get the substring from
     * @param start The start index (inclusive, 0-based)
     * @param end The end index (exclusive, 0-based)
     * @return A Future that will be completed with the substring
     */
    Future<String> getRange(String key, int start, int end);

    /**
     * Sets the string value of a key from the given offset.
     *
     * @param key The key to set
     * @param offset The offset to start setting from
     * @param value The value to set
     * @return A Future that will be completed with the length of the string after the operation
     */
    Future<Long> setRange(String key, long offset, String value);
}
