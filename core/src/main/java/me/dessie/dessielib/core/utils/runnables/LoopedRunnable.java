package me.dessie.dessielib.core.utils.runnables;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.function.Consumer;

/**
 * BukkitTask timer that will loop for a specified amount of times before automatically stopping.
 */
public class LoopedRunnable extends BukkitRunnable {

    private Consumer<LoopedRunnable> consumer;
    private int delay;
    private int timer;

    private int currentLoop = 0;
    private int loops;

    private Runnable complete;

    /**
     * @param plugin Your plugin instance
     * @param delay The delay in ticks before the Runnable starts
     * @param timer How long in ticks between each execution
     * @param loops How many times to run the execution before stopping
     * @param runnable The Runnable to run
     */
    public LoopedRunnable(JavaPlugin plugin, int delay, int timer, int loops, Runnable runnable) {
        this.delay = delay;
        this.timer = timer;
        this.loops = loops - 1;
        this.consumer = loopedRunnable -> runnable.run();
        this.runTaskTimer(plugin, delay, timer);
    }

    /**
     * @param plugin Your plugin instance
     * @param delay The delay in ticks before the Runnable starts
     * @param timer How long in ticks between each execution
     * @param loops How many times to run the execution before stopping
     * @param runnable A Consumer that will run with this provided LoopedRunnable
     */
    public LoopedRunnable(JavaPlugin plugin, int delay, int timer, int loops, Consumer<LoopedRunnable> runnable) {
        this.delay = delay;
        this.timer = timer;
        this.loops = loops - 1;
        this.consumer = runnable;
        this.runTaskTimer(plugin, delay, timer);
    }

    /**
     * Runs a Runnable when this LoopedRunnable has completed all of it's cycles, and stops.
     *
     * @param complete The Runnable to execute when completed.
     * @return This LoopedRunnable
     */
    public LoopedRunnable onComplete(Runnable complete) {
        this.complete = complete;
        return this;
    }

    /**
     * @return The Runnable that will run when this Task completes.
     */
    public Runnable getOnComplete() {return complete;}

    /**
     * Returns what loop the task is currently running.
     *
     * @return The current task loop
     */
    public int getCurrentLoop() {return currentLoop;}

    /**
     * Returns the initial delay of the task, in ticks.
     *
     * @return The delay before the task starts, in ticks.
     */
    public int getDelay() {return delay;}

    /**
     * Returns how many times the task will loop before automatically stopping.
     *
     * @return The amount of loops the task will execute.
     */
    public int getLoops() {return loops;}

    /**
     * Returns the amount of time between each loop, in ticks.
     *
     * @return The amount of ticks between each loop
     */
    public int getTimer() {return timer;}

    /**
     * Returns a {@link Consumer} that will be ran with each run of the Task.
     * The Consumer will be passed the LoopedRunnable as a parameter.
     *
     * @return The Consumer that will run.
     */
    public Consumer<LoopedRunnable> getConsumer() {return consumer;}

    @Override
    public void run() {
        if(this.getConsumer() != null) {
            this.getConsumer().accept(this);
        }

        if(currentLoop++ >= loops) {
            this.cancel();
            if(this.getOnComplete() != null) this.getOnComplete().run();
        }
    }
}
