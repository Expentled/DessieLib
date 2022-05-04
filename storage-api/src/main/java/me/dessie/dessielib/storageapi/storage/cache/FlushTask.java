package me.dessie.dessielib.storageapi.storage.cache;

import me.dessie.dessielib.storageapi.StorageAPI;
import me.dessie.dessielib.storageapi.storage.container.StorageContainer;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FlushTask extends BukkitRunnable {

    private final StorageContainer container;
    private final int flushRate;
    private final int flushCooldown;

    private int currentFlushCooldown;
    private boolean running;
    private boolean queued;

    private final List<CompletableFuture<Void>> futures = new ArrayList<>();

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
                }
            }, 20, 20);
        }

        this.reset();
    }

    public void reset() {
        if(this.isRunning()) {
            this.cancel();
        }

        this.runTaskTimer(StorageAPI.getPlugin(), this.getFlushRate() * 20L, this.getFlushRate() * 20L);
        this.running = true;
    }

    public StorageContainer getContainer() {
        return container;
    }

    public int getFlushCooldown() {
        return flushCooldown;
    }

    public int getFlushRate() {
        return flushRate;
    }

    public void queueFlush() {
        this.queued = true;
    }

    public boolean isQueued() {
        return queued;
    }

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

    public boolean canFlush() {
        return this.getCurrentFlushCooldown() <= 0;
    }

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
