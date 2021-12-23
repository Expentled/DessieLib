package me.dessie.dessielib.particleapi.wrapper;

import org.bukkit.Particle;

/**
 * Used to easily represent a {@link Particle} and any option Particle Data that it may have.
 * For example, {@link Particle#REDSTONE} uses {@link org.bukkit.Particle.DustOptions} as data.
 *
 * If you need to modify the data of a Particle in any {@link me.dessie.dessielib.particleapi.shapes.ShapedParticle}
 * this class will make it possible.
 */
public class ParticleData {

    private final Particle particle;
    private final Object options;

    /**
     * @param particle The Particle represented. Data will be null.
     */
    public ParticleData(Particle particle) {
        this(particle, null);
    }

    /**
     * @param particle The Particle represented
     * @param options The options used by this particle.
     */
    public ParticleData(Particle particle, Object options) {
        this.particle = particle;
        this.options = options;
    }

    /**
     * @return The Particle that this data instance represents.
     */
    public Particle getParticle() {
        return particle;
    }

    /**
     * @return The Options that this
     */
    public Object getOptions() {
        return options;
    }
}
