package me.dessie.dessielib.particleapi.collison;

import me.dessie.dessielib.particleapi.animation.ParticleAnimator;
import me.dessie.dessielib.particleapi.shapes.ShapedParticle;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 *
 * Calls consumers when a {@link ShapedParticle}'s particle collides with something in the world.
 *
 * @see BlockCollider
 * @see EntityCollider
 *
 * If creating a custom collider, extend this class and override {@link ParticleCollider#attemptCollide(ShapedParticle, World, List)}
 *
 * @param <T> Any object within the Minecraft world that could be collided with.
 */
public abstract class ParticleCollider<T> {
    private int delay;

    private Consumer<T> collider;

    //Determines if this Collider can activate an infinite amount of times per tick.
    //If it's false, an Object will only be collided with a maximum one time per frame.
    //If this is true, and an object collides with multiple particles,
    //this will fire for all colliding particles.
    private boolean multiCollide;

    //Keeps track of the current frame collisions.
    //Is not used if multiCollide is true.
    private List<T> frameCollisions;

    private final Map<T, Integer> delays = new HashMap<>();

    /**
     * @param collider A consumer for what to do when a particle collides with the object.
     * @param delay How much time, in ticks, to wait between each collision event.
     *              For example, if this is 60, the collider will only be called for the same object every 3 seconds.
     */
    public ParticleCollider(Consumer<T> collider, int delay) {
        this(collider, delay, false);
    }

    /**
     * @param collider A consumer for what to do when a particle collides with the object.
     * @param delay How much time, in ticks, to wait between each collision event.
     *              For example, if this is 60, the collider will only be called for the same object every 3 seconds.
     * @param multiCollide If multiple particles can collide with the same object per tick. (Meaning multiple calls to the consumer)
     */
    public ParticleCollider(Consumer<T> collider, int delay, boolean multiCollide) {
        this.collider = collider;
        this.delay = delay;
        this.multiCollide = multiCollide;
    }

    /**
     * @return The amount of ticks between each call to the collision consumer for the same object.
     */
    public int getDelay() {
        return delay;
    }

    /**
     * @return If multiple particles can collide with the same object per tick.
     */
    public boolean isMultiCollide() {
        return multiCollide;
    }

    /**
     * @return The consumer that is applied when there is a collision.
     */
    public Consumer<T> getCollider() {
        return collider;
    }

    /**
     * @param collider Sets the consumer for what is applied when a particle collides with a block.
     * @return The ParticleCollider instance.
     */
    public ParticleCollider<T> setCollider(Consumer<T> collider) {
        this.collider = collider;
        return this;
    }

    /**
     * @param multiCollide Sets if multiple particles can collide with the same object per tick.
     * @return The ParticleCollider instance.
     */
    public ParticleCollider<T> setMultiCollide(boolean multiCollide) {
        this.multiCollide = multiCollide;
        return this;
    }

    /**
     * @param delay Sets the amount of ticks between each call to the collision consumer for the same object.
     * @return The ParticleCollider instance.
     */
    public ParticleCollider<T> setDelay(int delay) {
        this.delay = delay;
        return this;
    }

    /**
     * If you're writing your own Colliders for an object, you'll need to override this method.
     * To call your Consumer, you can use {@link Consumer#accept(Object)}
     *
     * @param particle The {@link ShapedParticle} that was rendered and could collide with something.
     * @param world The world that the ShapedParticle was rendered in.
     * @param points A list of particle locations within the rendered ShapedParticle.
     */
    protected abstract void attemptCollide(ShapedParticle particle, World world, List<Vector> points);

    /**
     * Marks an object as collided with.
     * This should always be called when the object has been collidied with in your {@link ParticleCollider#attemptCollide(ShapedParticle, World, List)}
     *
     * The delay and frame collision will be applied to this object, to properly prevent it from being collided with improperly.
     *
     * @param object The object to mark as a collided with
     */
    protected void add(T object) {
        delays.put(object, this.getDelay());

        //Add this Object to the frame collision.
        if(!this.isMultiCollide()) {
            this.frameCollisions.add(object);
        }
    }

    /**
     * Returns if a provided object is able to be collided with.
     * An object must not have an active delay, and if MultiCollide is off, it must not have been collided with this frame.
     *
     * @param object The object to check collision for.
     * @return If the Object is valid for a collision.
     */
    protected boolean canCollide(T object) {
        return !delays.containsKey(object) && (!this.isMultiCollide() && !frameCollisions.contains(object));
    }

    /**
     * Decrements the delay for the ShapedParticle.
     *
     * This should always been called in your {@link ParticleCollider#startCollide(ShapedParticle, World, List)} method, if overrode.
     * Generally, before any {@link ParticleCollider#canCollide(Object)} calls have been made.
     *
     * @param particle The ShapedParticle to calculate for.
     */
    protected void doDelayCalculate(ShapedParticle particle) {
        //Decrement the Collision Delay for all entities by the Particle's Animation Speed.
        for(T object : delays.keySet()) {
            delays.compute(object, ((object1, delay) -> delay -= particle.getAnimator().getAnimationSpeed()));
        }

        //Remove them from this delay if their delay is lower than 0.
        delays.entrySet().removeIf(entry -> entry.getValue() <= 0);
    }

    /**
     * Starts a collision for a Particle.
     * This will be called on every animation from of a ShapedParticle.
     *
     * @see me.dessie.dessielib.particleapi.animation.ParticleAnimator
     * @see ParticleAnimator#getAnimationSpeed() To change how often collisions occur.
     *
     * @param particle The ShapedParticle to check collisions for
     * @param world The World to check collisions in
     * @param points The points on the ShapedParticle to check collisions for.
     */
    public void startCollide(ShapedParticle particle, World world, List<Vector> points) {
        this.doDelayCalculate(particle);
        this.frameCollisions = new ArrayList<>();

        this.attemptCollide(particle, world, points);
    }
}
