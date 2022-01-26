package me.dessie.dessielib.storageapi.storage.cache;

import me.dessie.dessielib.storageapi.storage.container.StorageContainer;

import java.util.*;

public class StorageCache {
    private final Map<String, CachedObject> cache = new HashMap<>();
    private final int cacheDuration;

    //Temporarily stores all things that were changed and will need to be pushed to the data source.
    private final Map<String, Object> setCache = new HashMap<>();

    //List of all paths that have been removed using the remove method.
    private final List<String> removeCache = new ArrayList<>();

    /**
     * @param cacheDuration How long to cache the object for. Set to -1 to cache forever
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
     * Updates the {@link StorageContainer} with the set and remove caches.
     * After flushing, these maps will be cleared.
     *
     * Flushing will not empty the cached data, only the data that needs to be updated to the structure.
     *
     * @see StorageContainer#set(String, Object) for adding objects into the Set cache.
     * @see StorageContainer#remove(String) for adding paths into the Remove cache.
     *
     * @param container The StorageContainer to flush the cache to.
     */
    public void flush(StorageContainer container) {
        if(this.getSetCache().isEmpty() && this.getRemoveCache().isEmpty()) return;

        //Store and Delete the caches.
        this.getSetCache().keySet().forEach(path -> container.store(path, this.getSetCache().get(path)));


        //We don't have to clear the setCache and removeCache because delete does that already
        //We also can't use a for loop since that would throw a ConcurrentModification
        Iterator<String> iterator = this.getRemoveCache().iterator();
        while(iterator.hasNext()) {
            container.delete(iterator.next());
        }
    }

    /**
     * Returns if the provided path is cached.
     *
     * @param path The path to check.
     * @return If the path is cached.
     */
    public boolean isCached(String path) {
        return this.getCache().containsKey(path) || this.getSetCache().containsKey(path);
    }

    /**
     * Clears the cache.
     */
    public void clearCache() {
        for(CachedObject object : this.getCache().values()) {
            if(object.getTask() != null) {
                object.getTask().cancel();
            }
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

    /**
     * Returns the cache of objects that have been set and not updated to the data structure.
     * This cache is cleared once the cache has been pushed to the structure.
     * @return The current set cache
     */
    public Map<String, Object> getSetCache() {
        return setCache;
    }

    /**
     * Returns the cache of objects that have been removed and not updated to the data structure.
     * This cache is cleared once the cache has been pushed to the structure.
     * @return The current remove cache
     */
    public List<String> getRemoveCache() {
        return removeCache;
    }
}
