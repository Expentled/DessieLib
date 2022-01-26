package me.dessie.dessielib.storageapi.storage.container.settings;

import me.dessie.dessielib.storageapi.storage.cache.StorageCache;
import me.dessie.dessielib.storageapi.storage.container.StorageContainer;

public class StorageSettings {

    private final int cacheDuration;
    private final int update;

    /**
     * Creates a settings instance for a {@link StorageContainer} with a 1-minute cache duration and 5-minute update timer.
     */
    public StorageSettings() {
        this(60, 300);
    }

    /**
     * Creates a settings instance for a {@link StorageContainer} with the cache duration and 5-minute update timer.
     *
     * @param cacheDuration The cache duration in seconds. Use -1 to cache an object forever.
     *                      If -1 is used, {@link StorageContainer#clearCache()} should be manually
     *                      called periodically to avoid memory leaks.
     */
    public StorageSettings(int cacheDuration) {
        this(cacheDuration, 300);
    }

    /**
     * Creates a settings instance for a {@link StorageContainer} with the provided cache duration and update timer.
     *
     * @param cacheDuration The cache duration in seconds. Use -1 to cache an object forever.
     *                      If -1 is used, {@link StorageContainer#clearCache()} should be manually
     *                      called periodically to avoid memory leaks.
     *
     * @param update The update timer in seconds. Set to -1 to never automatically update.
     *               If -1 is used, you will need to manually call {@link StorageContainer#flush()}
     */
    public StorageSettings(int cacheDuration, int update) {
        this.cacheDuration = cacheDuration;
        this.update = update;
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
        return cacheDuration;
    }

    /**
     * Returns how long, in seconds, the {@link StorageContainer} will update it's set cache
     * to the data structure.
     *
     * Once this timer expires, the set cache is pushed and is cleared.
     *
     * @see StorageContainer#store(String, Object) how this items are stored into the data structure.
     * @see StorageCache#getSetCache() to get the items that will be stored.
     *
     * @return The update time.
     */
    public int getUpdate() {
        return update;
    }
}
