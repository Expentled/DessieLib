package me.dessie.dessielib.particleapi.shapes;


import me.dessie.dessielib.particleapi.wrapper.ParticleData;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

/**
 * Draws a single particle at the specified Location.
 */
public class DotParticle extends ShapedParticle {
    /**
     * @param particle The {@link Particle} to render.
     */
    public DotParticle(Particle particle) {
        this(new ParticleData(particle));
    }

    /**
     * @param particle The {@link ParticleData} to render.
     */
    public DotParticle(ParticleData particle) {
        super(particle, 1, ((location, step) -> new Vector(location.getX(), location.getY(), location.getZ())));
    }
}
