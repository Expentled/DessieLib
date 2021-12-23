package me.dessie.dessielib.particleapi.transform.transformations;


import me.dessie.dessielib.particleapi.transform.ParticleTransform;
import me.dessie.dessielib.particleapi.transform.TransformType;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.function.BiFunction;

/**
 * Translates the entire {@link me.dessie.dessielib.particleapi.shapes.ShapedParticle} based
 * on the provided function.
 */
public class ParticleTranslate extends ParticleTransform {

    /**
     * @param type The {@link TransformType} of this Transformation.
     * @param frames Over how many animation frames this transformation will be applied.
     *               A "frame" is when the {@link me.dessie.dessielib.particleapi.animation.ParticleAnimator} renders the Particle.
     *               The TransformType will determine how the cycle behaves once the frame cap is reached.
     *
     * @param transform The transformation function. Should return a {@link Vector}
     *                  with how much to translate, on the X, Y, and Z axes.
     *
     *                  The transform in done in blocks, so for example, a Vector with 1,1,1
     *                  will move the particle 1 block on all axes.
     */
    public ParticleTranslate(TransformType type, int frames, BiFunction<Location, Integer, Vector> transform) {
        super(type, frames, transform);
    }

    @Override
    public void applyToPoints(Location location, List<Vector> points) {
        Vector transform = this.apply(location);
        for(Vector point : points) {
            point.add(transform);
        }
    }
}
