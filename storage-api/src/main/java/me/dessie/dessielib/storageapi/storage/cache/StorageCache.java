package me.dessie.dessielib.storageapi.storage.cache;

import java.util.HashMap;
import java.util.Map;

public class StorageCache {
    private final Map<String, CachedObject> cache = new HashMap<>();
    private final int cacheDuration;

    /**
     * @param cacheDuration How long to cache the object for
     */
    public StorageCache(int cacheDuration) {
        this.cacheDuration = cacheDuration;
    }

    /**
     * Caches a path to an object with the default cache duration.
     *
     * @param path The path of the object.
     * @param obj The object to cache.
     */
    public void cache(String path, Object obj) {
        this.cache(path, obj, this.getCacheDuration());
    }

    /**
     * Caches a path to an object with a custom cache duration, specified in seconds.
     *
     * @param path The path of the object.
     * @param obj The object to cache.
     */
    public void cache(String path, Object obj, int cacheDuration) {
        this.getCache().put(path, new CachedObject(this, obj, cacheDuration));
    }

    /**
     * Gets an object from the cache.
     * Getting an item from the cache will reset its cache removal timer.
     *
     * @param path The path to get
     * @return The cached Object
     */
    public CachedObject get(String path) {
        return this.getCache().get(path);
    }

    /**
     * Returns if the provided path is cached.
     *
     * @param path The path to check.
     * @return If the path is cached.
     */
    public boolean isCached(String path) {
        return this.getCache().containsKey(path);
    }

    /**
     * Removes a path from the cache.
     *
     * @param path The path to remove.
     */
    public void remove(String path) {
        this.getCache().remove(path);
    }

    /**
     * Removes a {@link CachedObject} from the cache.
     *
     * @param object The CachedObject to remove.
     */
    public void remove(CachedObject object) {
        String path = null;
        for(Map.Entry<String, CachedObject> entry : this.getCache().entrySet()) {
            if(entry.getValue() == object) {
                path = entry.getKey();
                break;
            }
        }

        if(path != null) this.getCache().remove(path);
    }

    /**
     * Clears the entire cache.
     */
    public void clearCache() {
        for(CachedObject object : this.getCache().values()) {
            object.getTask().cancel();
        }

        this.getCache().clear();
    }

    /**
     * Returns the default cache duration, in seconds, for objects.
     * @see StorageCache#cache(String, Object)
     *
     * @return The cache duration
     */
    public int getCacheDuration() {
        return this.cacheDuration;
    }

    /**
     * Returns the cache map.
     *
     * @return The cache map
     */
    public Map<String, CachedObject> getCache() {
        return cache;
    }
}
