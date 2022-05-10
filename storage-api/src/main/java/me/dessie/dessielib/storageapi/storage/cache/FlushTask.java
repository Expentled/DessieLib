package me.dessie.dessielib.storageapi.storage.cache;

import me.dessie.dessielib.storageapi.StorageAPI;
import me.dessie.dessielib.storageapi.storage.container.StorageContainer;
import me.dessie.dessielib.storageapi.storage.settings.StorageSettings;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Handles how fast a {@link StorageContainer} can be written to.
 *
 * This task will make sure that calls are not made constantly by adding a cooldown,
 * and will automatically flush the container after a specified period of time.
 *
 * @see StorageSettings Use the Settings to change the cooldown and flush rate.
 */
public class FlushTask extends BukkitRunnable {

    private final StorageContainer container;
    private final int flushRate;
    private final int flushCooldown;

    private int currentFlushCooldown;
    private boolean running;
    private boolean queued;

    private final List<CompletableFuture<Void>> futures = new ArrayList<>();

    /**
     * @param container The container to create the FlushTask for.
     */
    public FlushTask(StorageContainer container) {
        this.container = container;
        this.flushRate = this.getContainer().getSettings().getFlushRate();
        this.flushCooldown = this.getContainer().getSettings().getFlushCooldown();

        this.currentFlushCooldown = 0;

        //Decrement the cooldown.
        if(this.getFlushCooldown() > 0) {
            Bukkit.getScheduler().runTaskTimer(StorageAPI.getPlugin(), () -> {
                this.currentFlushCooldown--;
                if(this.isQueued() && this.canFlush()) {
                    this.getContainer().flush();
                    this.queued = false;
                }
            }, 20, 20);
        }

        this.reset();
    }

    /**
     * Resets the FlushTask's scheduler to it's initial starting value.
     * @see FlushTask#getFlushRate()
     */
    public void reset() {
        if(this.isRunning()) {
            this.cancel();
        }

        this.runTaskTimer(StorageAPI.getPlugin(), this.getFlushRate() * 20L, this.getFlushRate() * 20L);
        this.running = true;
    }

    /**
     * Returns the {@link StorageContainer} that is using this task.
     *
     * @return The respective StorageContainer.
     */
    public StorageContainer getContainer() {
        return container;
    }

    /**
     * Returns how often, in seconds, the task is able to flush the container.
     * This method is a delegate method for {@link StorageSettings#getFlushCooldown()}, and will be the same result.
     *
     * @return How often in seconds the FlushTask is allowed to flush the container.
     */
    public int getFlushCooldown() {
        return flushCooldown;
    }

    /**
     * Returns how often, in seconds, the task will automatically flush the container.
     * This method is a delegate method for {@link StorageSettings#getFlushRate()}, and will be the same result.
     *
     * @return How often in seconds the FlushTask will automatically flush the container
     */
    public int getFlushRate() {
        return flushRate;
    }

    /**
     * Tells the FlushTask that a request for flushing has been attempted but failed.
     * When this is toggled, as soon as the cooldown for flushing is expired, the FlushTask will automatically
     * re-flush to accept the queue ticket.
     *
     * @see FlushTask#getFlushCooldown()
     */
    public void queueFlush() {
        this.queued = true;
    }

    /**
     * Returns if the FlushTask currently has a queue ticket in place.
     *
     * @see FlushTask#queueFlush() See for putting in a queue ticket.
     *
     * @return If the FlushTask has a queue in progress.
     */
    public boolean isQueued() {
        return queued;
    }

    /**
     * Returns if the FlushTask is currently running.
     *
     * @return If the task is currently running.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Returns the amount of seconds since the last flush.
     * If this is less than 0, the cooldown is expired and the cache can be flushed.
     *
     * @return The amount of seconds since the last flush.
     */
    public int getCurrentFlushCooldown() {
        return currentFlushCooldown;
    }

    /**
     * Sets the flush cooldown back to the original maximum value to start the countdown.
     */
    public void resetFlushCooldown() {
        this.currentFlushCooldown = this.getFlushCooldown();
    }

    /**
     * Returns if the {@link FlushTask#getFlushCooldown()} is expired.
     * If it has expired, then the FlushTask can be flushed.
     *
     * @return If the FlushTask can be flushed.
     */
    public boolean canFlush() {
        return this.getCurrentFlushCooldown() <= 0;
    }

    /**
     * Adds a {@link CompletableFuture} that will be completed when the FlushTask is flushed.
     *
     * @param future The Future to complete when the task is flushed.
     */
    public void addFuture(CompletableFuture<Void> future) {
        this.futures.add(future);
    }

    @Override
    public void run() {
        this.getContainer().flush();

        this.futures.forEach(future -> {
            future.complete(null);
        });
    }
}
