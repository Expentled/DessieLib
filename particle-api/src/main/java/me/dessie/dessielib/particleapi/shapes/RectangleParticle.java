package me.dessie.dessielib.particleapi.shapes;

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
     */
    public RectangleParticle(Particle particle, int points, double width, double height) {
        this(new ParticleData(particle), points, width, height);
    }

    /**
     * @param particle The {@link ParticleData} to render.
     * @param points How many particles will make up the rectangle.
     * @param width The width, in blocks, of the rectangle.
     * @param height The height, in blocks, of the rectangle.
     */
    public RectangleParticle(ParticleData particle, int points, double width, double height) {
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
    }
}
