package me.dessie.dessielib.storageapi.storage.cache;

import me.dessie.dessielib.storageapi.StorageAPI;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class CachedObject {

    private final StorageCache cache;
    private final Object object;
    private final int duration;

    private final BukkitTask task;

    /**
     * @param cache The {@link StorageCache} that cached this object.
     * @param object The object to cache
     * @param duration The duration to cache in seconds.
     */
    CachedObject(StorageCache cache, Object object, int duration) {
        this.cache = cache;
        this.object = object;
        this.duration = duration;

        if(duration > 0) {
            this.task = Bukkit.getScheduler().runTaskLater(StorageAPI.getPlugin(), () -> {
                this.getCache().remove(this);
            }, duration * 20L);
        } else this.task = null;
    }

    /**
     * Returns the {@link StorageCache} that cached this object.
     * @return The StorageCache
     */
    public StorageCache getCache() {
        return cache;
    }

    /**
     * Returns the {@link BukkitTask} that controls when this CachedObject will be removed.
     *
     * @return The BukkitTask
     */
    public BukkitTask getTask() {
        return task;
    }

    /**
     * Returns the amount of time this object will be cached in seconds.
     * @return The duration
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Returns the cached object.
     *
     * @return The cached object.
     */
    public Object getObject() {
        return object;
    }
}
