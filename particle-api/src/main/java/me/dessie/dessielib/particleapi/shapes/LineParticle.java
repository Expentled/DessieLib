package me.dessie.dessielib.particleapi.shapes;


import me.dessie.dessielib.particleapi.wrapper.ParticleData;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

/**
 * Draws a line of particles from the display location to the passed location.
 */
public class LineParticle extends ShapedParticle {

    /**
     * @param particle The {@link Particle} to render.
     * @param points How many particles will make up the line.
     * @param toLocation The end location for the line.
     */
    public LineParticle(Particle particle, int points, Location toLocation) {
        this(new ParticleData(particle), points, toLocation);
    }

    /**
     * @param particle The {@link ParticleData} to render.
     * @param points How many particles will make up the line.
     * @param toLocation The end location for the line.
     */
    public LineParticle(ParticleData particle, int points, Location toLocation) {
       this(particle, points, toLocation.toVector());
    }

    /**
     * @param particle The {@link Particle} to render.
     * @param points How many particles will make up the line.
     * @param toVector The end vector for the line.
     */
    public LineParticle(Particle particle, int points, Vector toVector) {
        this(new ParticleData(particle), points, toVector);
    }

    /**
     * @param particle The {@link ParticleData} to render.
     * @param points How many particles will make up the line.
     * @param toVector The end vector for the line.
     */
    public LineParticle(ParticleData particle, int points, Vector toVector) {
        super(particle, points, (((location, step) -> {
            double slopeX = toVector.getX() - location.getX();
            double slopeY = toVector.getY() - location.getY();
            double slopeZ = toVector.getZ() - location.getZ();

            return new Vector(location.getX() + (slopeX / points * step),
                    location.getY() + (slopeY / points * step),
                    location.getZ() + (slopeZ / points * step));
        })));
    }
}
