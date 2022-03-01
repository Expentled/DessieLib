package me.dessie.dessielib.core.utils.tuple;


/**
 * Holds a single set of two Objects
 *
 * @param <K> The type of the Key
 * @param <V> The type of the Value
 */
public record Pair<K, V>(K key, V value) {

    /**
     * Returns the key Object
     * @return The key
     */
    public K getKey() {
        return key;
    }

    /**
     * Returns the value Object
     * @return The value
     */
    public V getValue() {
        return value;
    }
}
