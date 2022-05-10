package me.dessie.dessielib.particleapi.transform.orientation;

/**
 * Specifies a Particle Orientation to rotate them when they're created.
 *
 * Multiple orientations can be chained together to rotate around multiple axes.
 */
public record Orientation(Axis axis, double rotation) {
    /**
     * @param axis     The axis that the rectangle will be rotated around, to orientate it.
     * @param rotation The angle of rotation, in degrees.
     *                 Generally, you'll want to use 45 degree increments.
     */
    public Orientation {}

    /**
     * Returns {@link Axis} this orientation will rotate around
     *
     * @return The axis to rotate around
     */
    public Axis getAxis() {
        return axis;
    }

    /**
     * The rotation in degrees, to rotate around the axis.
     *
     * @return How much to rotate on the specified axis.
     */
    public double getRotation() {
        return rotation;
    }
}
