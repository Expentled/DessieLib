package me.dessie.dessielib.particleapi.collison;

import me.dessie.dessielib.particleapi.shapes.ShapedParticle;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.function.Consumer;

/**
 * A collider that is called whenever a particle collides with a non-air block in the world.
 */
public class BlockCollider extends ParticleCollider<Block> {

    /**
     * {@inheritDoc}
     */
    public BlockCollider(Consumer<Block> collider, int delay) {
        this(collider, delay, false);
    }

    /**
     * {@inheritDoc}
     */
    public BlockCollider(Consumer<Block> collider, int delay, boolean multiCollide) {
        super(collider, delay, multiCollide);
    }

    @Override
    protected void attemptCollide(ShapedParticle particle, World world, List<Vector> points) {
        for(Vector point : points) {
            Block block = world.getBlockAt((int) point.getX(), (int) point.getY(), (int) point.getZ());
            if(block.getType() == Material.AIR || block.getType() == Material.CAVE_AIR) continue;

            this.getCollider().accept(block);
            this.add(block);
        }
    }
}
