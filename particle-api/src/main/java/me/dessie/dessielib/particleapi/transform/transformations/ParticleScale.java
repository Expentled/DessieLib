package me.dessie.dessielib.particleapi.transform.transformations;


import me.dessie.dessielib.particleapi.transform.ParticleTransform;
import me.dessie.dessielib.particleapi.transform.TransformType;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.function.BiFunction;

/**
 * Scales the size of the entire {@link me.dessie.dessielib.particleapi.shapes.ShapedParticle} based
 * on the provided function.
 */
public class ParticleScale extends ParticleTransform {

    /**
     * @param type The {@link TransformType} of this Transformation.
     * @param frames Over how many animation frames this transformation will be applied.
     *               A "frame" is when the {@link me.dessie.dessielib.particleapi.animation.ParticleAnimator} renders the Particle.
     *               The TransformType will determine how the cycle behaves once the frame cap is reached.
     *
     * @param transform The transformation function. Should return a {@link Vector}
     *                  with how much to scale, on the X, Y, and Z dimensions.
     *
     *                  For example, to make the ShapedParticle 10% larger on all dimensions,
     *                  The vector should be 1.1, 1.1, 1.1.
     *
     *                  To make it 10% smaller, use 0.9, 0.9, 0.9.
     */
    public ParticleScale(TransformType type, int frames, BiFunction<Location, Integer, Vector> transform) {
        super(type, frames, transform);
    }

    @Override
    public void applyToPoints(Location location, List<Vector> points) {
        Vector scaleFactors = this.apply(location);

        for(Vector point : points) {
            org.bukkit.util.Vector vector = new org.bukkit.util.Vector(point.getX(), point.getY(), point.getZ()).subtract(location.toVector());
            vector.multiply(new org.bukkit.util.Vector(scaleFactors.getX(), scaleFactors.getY(), scaleFactors.getZ()));
            point.add(new Vector(vector.getX(), vector.getY(), vector.getZ()));
        }
    }
}
