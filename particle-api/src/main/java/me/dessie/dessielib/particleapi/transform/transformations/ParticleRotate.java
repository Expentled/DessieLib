package me.dessie.dessielib.particleapi.transform.transformations;


import me.dessie.dessielib.particleapi.transform.ParticleTransform;
import me.dessie.dessielib.particleapi.transform.TransformType;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.function.BiFunction;

/**
 * Rotates the entire {@link me.dessie.dessielib.particleapi.shapes.ShapedParticle} based
 * on the provided function.
 */
public class ParticleRotate extends ParticleTransform {

    private final Vector offset;

    /**
     * @param type The {@link TransformType} of this Transformation.
     * @param frames Over how many animation frames this transformation will be applied.
     *               A "frame" is when the {@link me.dessie.dessielib.particleapi.animation.ParticleAnimator} renders the Particle.
     *               The TransformType will determine how the cycle behaves once the frame cap is reached.
     *
     * @param transform The transformation function. Should return a {@link Vector}
     *                  with how much to rotate on the X, Y, and Z axes.
     */
    public ParticleRotate(TransformType type, int frames, BiFunction<Location, Integer, Vector> transform) {
        this(type, frames, transform, new Vector(0, 0, 0));
    }

    /**
     * @param type The {@link TransformType} of this Transformation.
     * @param frames Over how many animation frames this transformation will be applied.
     *               A "frame" is when the {@link me.dessie.dessielib.particleapi.animation.ParticleAnimator} renders the Particle.
     *               The TransformType will determine how the cycle behaves once the frame cap is reached.
     *
     * @param transform The transformation function. Should return a {@link Vector}
     *                  with how much to rotate on the X, Y, and Z axes.
     * @param offset A Vector with the origin offset for the rotation.
     */
    public ParticleRotate(TransformType type, int frames, BiFunction<Location, Integer, Vector> transform, Vector offset) {
        super(type, frames, transform);
        this.offset = offset;
    }

    @Override
    public void applyToPoints(Location location, List<Vector> points) {
        Vector rotateDegrees = this.apply(location);

        for (Vector point : points) {
            Vector origin = new Vector(location.getX(), location.getY(), location.getZ()).add(this.offset);

            /*
            May not be a perfect solution, and may have issues when
            rotating on more than one axis.
            */
            Vector rotated = rotateAroundX(origin, point, rotateDegrees.getX());
            rotated = rotateAroundY(origin, rotated, rotateDegrees.getY());
            rotated = rotateAroundZ(origin, rotated, rotateDegrees.getZ());

            point.setX(rotated.getX()).setY(rotated.getY()).setZ(rotated.getZ());
        }
    }

    private Vector rotateAroundX(Vector origin, Vector point, double angle) {
        double angleCos = Math.cos(Math.toRadians(angle));
        double angleSin = Math.sin(Math.toRadians(angle));
        double y = (point.getY() - origin.getY()) * angleCos - (point.getZ() - origin.getZ()) * angleSin + origin.getY();
        double z = (point.getY() - origin.getY()) * angleSin + (point.getZ() - origin.getZ()) * angleCos + origin.getZ();
        return new Vector(point.getX(), y, z);
    }

    private Vector rotateAroundY(Vector origin, Vector point, double angle) {
        double angleCos = Math.cos(Math.toRadians(angle));
        double angleSin = Math.sin(Math.toRadians(angle));
        double x = angleSin * (point.getZ() - origin.getZ()) + angleCos * (point.getX() - origin.getX()) + origin.getX();
        double z = angleCos * (point.getZ() - origin.getZ()) - angleSin * (point.getX() - origin.getX()) + origin.getZ();
        return new Vector(x, point.getY(), z);
    }

    private Vector rotateAroundZ(Vector origin, Vector point, double angle) {
        double angleCos = Math.cos(Math.toRadians(angle));
        double angleSin = Math.sin(Math.toRadians(angle));
        double x = angleCos * (point.getX() - origin.getX()) - angleSin * (point.getY() - origin.getY()) + origin.getX();
        double y = angleSin * (point.getX() - origin.getX()) + angleCos * (point.getY() - origin.getY()) + origin.getY();
        return new Vector(x, y, point.getZ());
    }
}
