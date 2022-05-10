package me.dessie.dessielib.particleapi.shapes;

import me.dessie.dessielib.particleapi.wrapper.ParticleData;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

/**
 * Draws particles as a circle, all circles will be drawn parallel to the ground by default.
 */
public class CircleParticle extends ShapedParticle {

    /**
     * @param particle The {@link Particle} to render.
     * @param points How many particles will make up the circle.
     * @param radius The radius of the circle
     */
    public CircleParticle(Particle particle, int points, int radius) {
        this(new ParticleData(particle), points, radius);
    }

    /**
     * @param particle The {@link ParticleData} to render
     * @param points How many particles will make up the circle.
     * @param radius The radius of the circle
     */
    public CircleParticle(ParticleData particle, int points, int radius) {
        super(particle, points, (((location, step) -> {
            double cos = radius * Math.cos(Math.PI * 2 * (step * ((double) 360 / points)) / 360);
            double sin = radius * Math.sin(Math.PI * 2 * (step * ((double) 360 / points)) / 360);

            return new Vector(location.getX() + cos, location.getY(), location.getZ() + sin);
        })));
    }

    /**
     * Modifies the orientation that the Circle will be drawn on.
     */
    public enum Orientation {
        /**
         * The Circle will be oriented to be rendered on the ground.
         */
        LEVELED,

        /**
         * The Circle will be drawn on the X axis, and get taller on the Y axis.
         */
        AXIS_X,

        /**
         * The Circle will be drawn on the Z axis, and get taller on the Y axis.
         */
        AXIS_Z
    }

}
