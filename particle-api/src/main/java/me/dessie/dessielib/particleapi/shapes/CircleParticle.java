package me.dessie.dessielib.particleapi.shapes;

import me.dessie.dessielib.particleapi.wrapper.ParticleData;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

/**
 * Draws particles as a circle
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
        this(particle, points, radius, Orientation.LEVELED);
    }

    /**
     * @param particle The {@link Particle} to render.
     * @param points How many particles will make up the circle.
     * @param radius The radius of the circle
     * @param orientation The {@link Orientation} that the circle should be drawn on.
     */
    public CircleParticle(Particle particle, int points, int radius, Orientation orientation) {
        this(new ParticleData(particle), points, radius, orientation);
    }

    /**
     * @param particle The {@link ParticleData} to render
     * @param points How many particles will make up the circle.
     * @param radius The radius of the circle
     * @param orientation The {@link Orientation} that the circle should be drawn on.
     */
    public CircleParticle(ParticleData particle, int points, int radius, Orientation orientation) {
        super(particle, points, (((location, step) -> {
            double cos = radius * Math.cos(Math.PI * 2 * (step * ((double) 360 / points)) / 360);
            double sin = radius * Math.sin(Math.PI * 2 * (step * ((double) 360 / points)) / 360);

            boolean onX = orientation == Orientation.LEVELED || orientation == Orientation.AXIS_X;
            boolean onY = orientation == Orientation.AXIS_X || orientation == Orientation.AXIS_Z;
            boolean onZ = orientation == Orientation.LEVELED || orientation == Orientation.AXIS_Z;

            return new Vector(
                    location.getX() + (onX ? cos : 0),
                    location.getY() + (onY ? (onX ? sin : onZ ? cos : 0) : 0),
                    location.getZ() + (onZ ? sin : 0));
        })));
    }

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
