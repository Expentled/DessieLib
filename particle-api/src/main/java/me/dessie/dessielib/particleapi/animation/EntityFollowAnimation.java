package me.dessie.dessielib.particleapi.animation;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

/**
 * An Animator that will track the Location of a specific Entity.
 * This means that the Location object passed into the created ShapedParticle
 * Will be modified to "follow" the passed Entity's location.
 */
public class EntityFollowAnimation extends ParticleAnimator {

    private Entity entity;
    private Vector offset;

    /**
     * @param entity The Entity to animate around.
     * @param animationSpeed How often, in ticks, to render the particles.
     */
    public EntityFollowAnimation(Entity entity, int animationSpeed) {
        this(entity, animationSpeed, 0);
    }

    /**
     * @param entity The Entity to animate around.
     * @param animationSpeed How often, in ticks, to render the particles.
     * @param offset How many blocks to offset the animation around the Entity's location.
     */
    public EntityFollowAnimation(Entity entity, int animationSpeed, Vector offset) {
        this(entity, animationSpeed, 0, offset);
    }

    /**
     * @param entity The Entity to animate around.
     * @param animationSpeed How often, in ticks, to render the particles.
     * @param loops How many times to loop the animation, set to 0 for infinite.
     */
    public EntityFollowAnimation(Entity entity, int animationSpeed, int loops) {
        this(entity, animationSpeed, loops, new Vector(0, 0, 0));
    }

    /**
     * @param entity The Entity to animate around.
     * @param animationSpeed How often, in ticks, to render the particles.
     * @param loops How many times to loop the animation, set to 0 for infinite.
     * @param offset How many blocks to offset the animation around the Entity's location.
     */
    public EntityFollowAnimation(Entity entity, int animationSpeed, int loops, Vector offset) {
        super(animationSpeed, loops);
        this.entity = entity;
        this.offset = offset;
    }

    /**
     * @return The Entity that this animator is currently following.
     */
    public Entity getEntity() { return entity; }

    /**
     * @return The current offset that is being applied to the particles.
     */
    public Vector getOffset() {
        return offset;
    }

    /**
     * @param entity The new Entity to start following
     * @return The EntityFollowAnimation instance.
     */
    public EntityFollowAnimation setEntity(Entity entity) {
        this.entity = entity;
        return this;
    }

    /**
     * @param offset The new offset to start applying
     * @return The EntityFollowAnimation instance.
     */
    public EntityFollowAnimation setOffset(Vector offset) {
        this.offset = offset;
        return this;
    }

    @Override
    public void run() {
        Location location = this.getEntity().getLocation();
        this.location.setX(location.getX() + this.getOffset().getX());
        this.location.setY(location.getY() + this.getOffset().getY());
        this.location.setZ(location.getZ() + this.getOffset().getZ());
        super.run();
    }
}
