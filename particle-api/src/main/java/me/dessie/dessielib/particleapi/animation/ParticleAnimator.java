package me.dessie.dessielib.particleapi.animation;

import me.dessie.dessielib.particleapi.ParticleAPI;
import me.dessie.dessielib.particleapi.shapes.ShapedParticle;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class ParticleAnimator extends BukkitRunnable {

    public ShapedParticle particle;

    //Data for re-applying the Particle's draw methods.
    protected Player player;
    protected Location location;

    //How many loops the Animator does before automatically cancelling.
    //Setting to 0 will loop forever.
    private int loops;

    //Tracks how far along the Animator is
    private int currentLoop;

    private boolean running = false;
    private int animationSpeed;

    /**
     * @param animationSpeed How often, in ticks, to render the particles.
     */
    public ParticleAnimator(int animationSpeed) {
        this(animationSpeed, 0);
    }

    /**
     * @param animationSpeed How often, in ticks, to render the particles.
     * @param loops How many times to loop the animation, set to 0 for infinite.
     */
    public ParticleAnimator(int animationSpeed, int loops) {
        this.animationSpeed = animationSpeed;
        this.loops = loops;
    }

    /**
     * @return How often, in ticks, this animator will render the particles.
     */
    public int getAnimationSpeed() { return animationSpeed; }

    /**
     * @return How many loops this animator will run before automatically stopping. If 0, the animator loops infinitely.
     */
    public int getLoops() { return loops; }

    /**
     * @return The {@link ShapedParticle} that this Animator renders.
     */
    public ShapedParticle getParticle() { return particle; }

    /**
     * @return If the animator is currently running.
     */
    public boolean isRunning() { return running; }

    /**
     * @param animationSpeed Sets how often the animator will render the {@link ShapedParticle} in ticks.
     * @return The ParticleAnimator instance
     */
    public ParticleAnimator setAnimationSpeed(int animationSpeed) {
        this.animationSpeed = animationSpeed;
        return this;
    }

    /**
     * @param loops Sets the amount of times this animator will execute.
     * @return The ParticleAnimator instance
     */
    public ParticleAnimator setLoops(int loops) {
        this.loops = loops;
        return this;
    }

    /**
     * Renders the attached {@link ShapedParticle} to a specific {@link Player}
     * These particles will only be rendered to the provided Player.
     *
     * If you want to render the particles to all Players, use {@link ParticleAnimator#start(Location)}
     *
     * @param player The Player to render the particles for
     * @param location The location to render the particles.
     */
    public void start(Player player, Location location) {
        this.player = player;
        this.start(location);
    }

    /**
     * Renders the attached {@link ShapedParticle} to all {@link Player}s
     *
     * If you want to render the particles to a specific Player, use {@link ParticleAnimator#start(Player, Location)}
     *
     * @param location The location to render the particles.
     */
    public void start(Location location) {
        if(ParticleAPI.getPlugin() == null) throw new IllegalStateException("ParticleAPI not registered!");
        if(this.isRunning()) throw new IllegalStateException("Already running!");

        this.location = location;
        this.running = true;
        this.runTaskTimer(ParticleAPI.getPlugin(), 0, this.getAnimationSpeed());
    }

    /**
     * Forcefully stop the Animator from rendering particles.
     */
    public void stop() {
        this.cancel();
        this.player = null;
        this.location = null;
        this.running = false;
    }

    @Override
    public void run() {
        if(this.getLoops() != 0 && this.currentLoop >= this.getLoops()) {
            this.cancel();
            return;
        }

        //Display the next Iteration.
        display(this.getParticle().getPoints(this.location));
        this.currentLoop++;
    }

    private void display(List<Vector> points) {
        if(this.location == null || this.location.getWorld() == null) return;

        if(this.player != null) {
            for(Vector point : points) {
                this.player.spawnParticle(this.getParticle().getParticle(), point.getX(), point.getY(), point.getZ(), 1, 0, 0 ,0, this.getParticle().getParticleSpeed(), this.getParticle().getParticleOptions());
            }
        } else {
            for(Vector point : points) {
                this.location.getWorld().spawnParticle(this.getParticle().getParticle(), point.getX(), point.getY(), point.getZ(), 1, 0, 0 ,0, this.getParticle().getParticleSpeed(), this.getParticle().getParticleOptions());
            }
        }
    }
}
