package me.dessie.dessielib.storageapi.storage.settings;

import me.dessie.dessielib.storageapi.storage.cache.StorageCache;
import me.dessie.dessielib.storageapi.storage.container.StorageContainer;
import me.dessie.dessielib.storageapi.storage.container.hooks.StorageHook;

/**
 * Defines the settings for a {@link StorageContainer}
 *
 * The following are default settings, and are applied automatically if a Container is created without modifying the settings:
 *   - Use Cache: true
 *   - Cache on Retrieve: true
 *   - Cache on Store: true
 *   - Cache Duration: 1 minute
 *   - Flush Rate: 5 minutes
 *   - Flush Cooldown: 5 seconds
 *
 * Some containers will change these default settings.
 *
 */
public class StorageSettings {

    private int cacheDuration;

    private boolean useCache;
    private boolean cacheOnStore;
    private boolean cacheOnRetrieve;

    private int flushRate;
    private int flushCooldown;

    /**
     * Creates a settings instance for a {@link StorageContainer} with default settings.
     *
     */
    public StorageSettings() {
        this.useCache = true;
        this.cacheOnRetrieve = true;
        this.cacheOnStore = true;

        this.cacheDuration = 60;
        this.flushRate = 300;

        this.flushCooldown = 5;
    }

    /**
     * Sets how long a cached object is cached in seconds.
     *
     * @param cacheDuration The new cache duration.
     * @return The StorageSettings instance.
     */
    public StorageSettings setCacheDuration(int cacheDuration) {
        this.cacheDuration = cacheDuration;
        return this;
    }

    /**
     * Sets how often the entire cache is pushed to the data structure. This value is in seconds.
     * Set to 0 to never auto-update the cache.
     *
     * @see StorageContainer#flush()
     *
     * @param flushRate How often, in seconds, to push the cache to the container.
     * @return The StorageSettings instance.
     */
    public StorageSettings setFlushRate(int flushRate) {
        this.flushRate = flushRate;
        return this;
    }

    /**
     * Sets whether the {@link StorageContainer} should cache objects.
     *
     * @param usesCache True if objects should be cached, false if not.
     * @return The StorageSettings instance.
     */
    public StorageSettings setUsesCache(boolean usesCache) {
        this.useCache = usesCache;
        return this;
    }

    /**
     * Sets whether objects should be cached when storing.
     *
     * If {@link StorageSettings#isUseCache()} is false, this setting is ignored.
     *
     * @see StorageContainer#store(String, Object)
     * @see StorageContainer#set(String, Object)
     * @see StorageSettings#isUseCache()
     *
     * @param cacheOnStore Whether the object should be cached when it's stored.
     * @return The StorageSettings instance.
     */
    public StorageSettings setCacheOnStore(boolean cacheOnStore) {
        this.cacheOnStore = cacheOnStore;
        return this;
    }

    /**
     * Sets whether objects should be cached when retrieving.
     *
     * If {@link StorageSettings#isUseCache()} is false, this setting is ignored.
     *
     * @see StorageContainer#retrieve(String)
     * @see StorageSettings#isUseCache()
     *
     * @param cacheOnRetrieve Whether the object should be cached when it's retrieved.
     * @return The StorageSettings instance.
     */
    public StorageSettings setCacheOnRetrieve(boolean cacheOnRetrieve) {
        this.cacheOnRetrieve = cacheOnRetrieve;
        return this;
    }

    /**
     * Sets how often the cache can be flushed.
     *
     * It is not recommended decreasing this value too low, otherwise files may become
     * saved improperly (especially when using {@link me.dessie.dessielib.storageapi.storage.format.flatfile.YAMLContainer}).
     *
     * If {@link StorageContainer#flush()} is called while on cooldown, the call will be delayed until the cooldown timer has expired.
     *
     * This setting is also applied to {@link StorageContainer#store(String, Object)} and {@link StorageContainer#delete(String)}.
     * Calling these methods while the cooldown is active will never call {@link StorageHook#complete()}.
     * Once the cooldown has completed, the {@link java.util.concurrent.CompletableFuture} will be completed.
     *
     * The main hook functions will still be executed immediately, even if this cooldown is active.
     *
     * @param flushCooldown How long, in seconds, the cache can be flushed to the data structure.
     * @return The StorageSettings instance.
     */
    public StorageSettings setFlushCooldown(int flushCooldown) {
        this.flushCooldown = flushCooldown;

        return this;
    }

    /**
     * Returns how long, in seconds, a {@link me.dessie.dessielib.storageapi.storage.cache.CachedObject} will be cached within
     * a {@link StorageContainer}'s {@link StorageCache}.
     *
     * Once this timer has expired, the data will have to be retrieved from the data structure again.
     *
     * @return The cache duration
     */
    public int getCacheDuration() {
        return this.cacheDuration;
    }

    /**
     * Returns how long, in seconds, the {@link StorageContainer} will update it's set cache
     * to the data structure.
     *
     * Once this timer expires, the set cache is pushed and is cleared.
     *
     * @see StorageContainer#store(String, Object) how items are stored into the data structure.
     * @see StorageCache#getSetCache() to get the items that will be stored.
     *
     * @return The update time.
     */
    public int getFlushRate() {
        return this.flushRate;
    }

    /**
     * Returns how often the cache can be flushed.
     *
     * @return How long, in seconds, the cache can be flushed to the data structure.
     */
    public int getFlushCooldown() {
        return this.flushCooldown;
    }

    /**
     * Returns if the {@link StorageContainer} should cache objects.
     *
     * @return If the StorageContainer should cache objects.
     */
    public boolean isUseCache() {
        return this.useCache;
    }

    /**
     * Returns if the {@link StorageContainer} caches objects when they're retrieved.
     *
     * @return If the StorageContainer caches objects when they're retrieved.
     */
    public boolean isCachedOnRetrieve() {
        return cacheOnRetrieve;
    }

    /**
     * Returns if the {@link StorageContainer} caches objects when they're stored.
     *
     * @return If the StorageContainer caches objects when they're stored.
     */
    public boolean isCachedOnStore() {
        return cacheOnStore;
    }

}
