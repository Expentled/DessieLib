package me.dessie.dessielib.storageapi.storage.container.settings;

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
     * @param cacheDuration The cache duration in seconds.
     */
    public StorageSettings(int cacheDuration) {
        this(cacheDuration, 300);
    }

    /**
     * Creates a settings instance for a {@link StorageContainer} with the provided cache duration and update timer.
     *
     * @param cacheDuration The cache duration in seconds.
     * @param update The update timer in seconds.
     */
    public StorageSettings(int cacheDuration, int update) {
        this.cacheDuration = cacheDuration;
        this.update = update;
    }

    /**
     * Returns how long, in seconds, a {@link me.dessie.dessielib.storageapi.storage.cache.CachedObject} will be cached within
     * a {@link StorageContainer}'s {@link me.dessie.dessielib.storageapi.storage.cache.StorageCache}.
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
     * @see StorageContainer#getSetCache() to get the items that will be stored.
     *
     * @return The update time.
     */
    public int getUpdate() {
        return update;
    }
}
