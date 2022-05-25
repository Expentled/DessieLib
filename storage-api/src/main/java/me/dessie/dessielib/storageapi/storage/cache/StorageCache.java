package me.dessie.dessielib.storageapi.storage.cache;

import me.dessie.dessielib.storageapi.storage.container.StorageContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Caches all data into paths that have been retrieved from {@link StorageContainer}s.
 * 
 * This cache will be checked first before attempting to retrieve data from a StorageContainer again.
 * Data in this cache will expire, and can be changed by using {@link me.dessie.dessielib.storageapi.storage.settings.StorageSettings#setCacheDuration(int)}
 */
public class StorageCache {
    private final Map<String, CachedObject> cache = new HashMap<>();
    private final int cacheDuration;
    private final StorageContainer container;
    private final FlushTask flushTask;

    //Temporarily stores all things that were changed and will need to be pushed to the data source.
    private final Map<String, Object> setCache = new HashMap<>();

    //List of all paths that have been removed using the remove method.
    private final List<String> removeCache = new ArrayList<>();

    /**
     * @param container The StorageContainer that this cache attaches to.
     * @param cacheDuration How long to cache the object for. Set to -1 to cache forever
     */
    public StorageCache(StorageContainer container, int cacheDuration) {
        this.container = container;
        this.cacheDuration = cacheDuration;

        this.flushTask = new FlushTask(container);
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
     * @param cacheDuration How long, in seconds, an object should be kept in the cache for.
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
        this.getCache().entrySet().removeIf(entry -> entry.getValue() == object);
    }

    /**
     * Updates the {@link StorageContainer} with the set and remove caches.
     * After flushing, these maps will be cleared.
     *
     * Flushing will not empty the cached data, only the data that needs to be updated to the structure.
     *
     * @see StorageContainer#set(String, Object) for adding objects into the Set cache.
     * @see StorageContainer#remove(String) for adding paths into the Remove cache.
     */
    public void flush() {
        if(!this.getSetCache().isEmpty()) {
            this.getContainer().storeAll(this.getSetCache());
        }

        if(!this.getRemoveCache().isEmpty()) {
            this.getContainer().deleteAll(this.getRemoveCache());
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
     * Returns the {@link StorageContainer} that this cache is caching for.
     *
     * @return The StorageContainer.
     */
    public StorageContainer getContainer() {
        return container;
    }

    /**
     * Returns the {@link FlushTask} that defines when this cache is flushed.
     *
     * @return The FlushTask.
     */
    public FlushTask getFlushTask() {
        return this.flushTask;
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
