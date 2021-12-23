package me.dessie.dessielib.particleapi.shapes;

import me.dessie.dessielib.particleapi.transform.TransformType;
import me.dessie.dessielib.particleapi.transform.transformations.ParticleRotate;
import me.dessie.dessielib.particleapi.wrapper.ParticleData;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

/**
 * Draws particle as a Square or Rectangle.
 */
public class RectangleParticle extends ShapedParticle {

    /**
     * @param particle The {@link Particle} to render.
     * @param points How many particles will make up the rectangle.
     * @param width The width, in blocks, of the rectangle.
     * @param height The height, in blocks, of the rectangle.
     * @param orientations Optional {@link Orientation}s to rotate the Rectangle a specific way.
     *                     This will be applied before any {@link me.dessie.dessielib.particleapi.transform.ParticleTransform}s are applied.
     */
    public RectangleParticle(Particle particle, int points, double width, double height, Orientation... orientations) {
        this(new ParticleData(particle), points, width, height, orientations);
    }

    /**
     * @param particle The {@link ParticleData} to render.
     * @param points How many particles will make up the rectangle.
     * @param width The width, in blocks, of the rectangle.
     * @param height The height, in blocks, of the rectangle.
     * @param orientations Optional {@link Orientation}s to rotate the Rectangle a specific way.
     *                     This will be applied before any {@link me.dessie.dessielib.particleapi.transform.ParticleTransform}s are applied.
     */
    public RectangleParticle(ParticleData particle, int points, double width, double height, Orientation... orientations) {
        super(particle, points, ((location, step) -> {

            //Make sure all particles are the same distance apart by using width/height proportions.
            double widthProportion = (width + height) / width;
            double heightProportion = (width + height) / height;

            //Find how many particles should be draw on the width and height lines.
            int pointsPerWidth = (int) (points / widthProportion) / 2;
            int pointsPerHeight = (int) (points / heightProportion) / 2;

            //Calculate the current step of each.
            double widthStep = width / pointsPerWidth * (pointsPerWidth - (step % pointsPerWidth));
            double heightStep = height / pointsPerHeight * (pointsPerHeight - (step % pointsPerHeight));

            //Draw them depending on which line of the Rectangle we're drawing.
            if(step < pointsPerWidth) {
                return new Vector(location.getX() + widthStep, location.getY(), location.getZ());
            } else if(step < pointsPerWidth + pointsPerHeight) {
                return new Vector(location.getX() + width, location.getY() + heightStep, location.getZ());
            } else if(step < pointsPerHeight + pointsPerWidth * 2) {
                return new Vector(location.getX() + width - widthStep, location.getY() + height, location.getZ());
            } else {
                return new Vector(location.getX(), location.getY() + height - heightStep, location.getZ());
            }
        }));

        //Apply the Orientations, since the Rectangle is always drawn in a static Orientation.
        //These can be used to make the Rectangle flat, or rotated.
        for(Orientation orientation : orientations) {
            this.addTransform(new ParticleRotate(TransformType.STATIC, 1, ((location, step) -> {
                switch(orientation.getAxis()) {
                    case X -> {
                        return new Vector(orientation.getRotation(), 0,0);
                    }
                    case Y -> {
                        return new Vector(0, orientation.getRotation(), 0);
                    }
                    case Z -> {
                        return new Vector(0, 0, orientation.getRotation());
                    }
                }

                return new Vector(0, 0, 0);
            })));
        }
    }

    /**
     * Used in {@link Orientation} to rotate a specific amount.
     */
    public enum Axis {
        /**
         * Rotates on the X Axis
         */
        X,

        /**
         * Rotates on the Y Axis
         */
        Y,

        /**
         * Rotates on the Z Axis
         */
        Z
    }

    public static class Orientation {
        private final Axis axis;
        private final double rotation;

        /**
         * @param axis The axis that the rectangle will be oriented on.
         * @param rotation The angle of rotation.
         *                 Generally, you'll want to use 45 degree increments.
         */
        public Orientation(Axis axis, double rotation) {
            this.axis = axis;
            this.rotation = rotation;
        }

        public Axis getAxis() {
            return axis;
        }
        public double getRotation() {
            return rotation;
        }
    }
}
