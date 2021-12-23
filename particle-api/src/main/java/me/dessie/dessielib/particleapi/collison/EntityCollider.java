package me.dessie.dessielib.particleapi.collison;

import me.dessie.dessielib.particleapi.shapes.ShapedParticle;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * A collider that is called whenever a particle collides with an entity in the world.
 */
public class EntityCollider extends ParticleCollider<Entity> {

    /**
     * {@inheritDoc}
     */
    public EntityCollider(Consumer<Entity> collider, int delay) {
        this(collider, delay, false);
    }

    /**
     * {@inheritDoc}
     */
    public EntityCollider(Consumer<Entity> collider, int delay, boolean multiCollide) {
        super(collider, delay, multiCollide);
    }

    @Override
    protected void attemptCollide(ShapedParticle particle, World world, List<Vector> points) {
        //For all points, attempt to find any Entity that collides with this point.
        for(Vector point : points) {
            //So first we can get the Chunk that this point is in
            Chunk chunk = world.getChunkAt((int) point.getX() >> 4, (int) point.getZ() >> 4);
            if(!chunk.isLoaded()) continue;

            //Look through all the Chunk's entities & attempt a collision
            //Provided that the entity is valid for a collision attempt.
            Arrays.stream(chunk.getEntities())
                    .filter(entity -> this.canCollide(entity) && entity.getBoundingBox().contains(point))
                    .forEach(entity -> {
                        this.getCollider().accept(entity);
                        this.add(entity);
                    });
        }
    }
}
