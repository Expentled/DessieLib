package me.dessie.dessielib.particleapi.transform;


import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.function.BiFunction;

/**
 * Used to transform {@link me.dessie.dessielib.particleapi.shapes.ShapedParticle} as they are being rendered in the world.
 *
 * This class is used as a baseline for defining these transformations.
 *
 * @see me.dessie.dessielib.particleapi.transform.transformations.ParticleScale
 * @see me.dessie.dessielib.particleapi.transform.transformations.ParticleRotate
 * @see me.dessie.dessielib.particleapi.transform.transformations.ParticleTranslate
 */
public abstract class ParticleTransform {
    private final int frames;
    private int currentStep = 0;
    private BiFunction<Location, Integer, Vector> transform;
    private TransformType type;
    private boolean isOscillating = false;

    /**
     * @param type The {@link TransformType} of this Transformation.
     * @param frames Over how many animation frames this transformation will be applied.
     *               A "frame" is when the {@link me.dessie.dessielib.particleapi.animation.ParticleAnimator} renders the Particle.
     *               The TransformType will determine how the cycle behaves once the frame cap is reached.
     *
     * @param transform The transformation function. If you're creating a custom transformation,
     *                  you can get the result of the function using {@link ParticleTransform#apply(Location)}
     */
    public ParticleTransform(TransformType type, int frames, BiFunction<Location, Integer, Vector> transform) {
        this.type = type;
        this.frames = frames;
        this.transform = transform;
    }

    /**
     * Applies the transformation function onto a Location.
     * You'll call this function in your overriding {@link ParticleTransform#applyToPoints(Location, List)}.
     *
     * @param location The location of the {@link me.dessie.dessielib.particleapi.shapes.ShapedParticle}
     * @return The Vector that is returned by the Transformation Function.
     */
    protected Vector apply(Location location) {
        Vector point = this.getTransform().apply(location, this.currentStep);
        
        if(this.getType() == TransformType.OSCILLATE) {
            if (this.isOscillating) {
                this.currentStep--;
                if(this.currentStep <= 0) {
                    this.isOscillating = false;
                }
            } else {
                this.currentStep++;
                if(this.currentStep > this.getFrames()) {
                    this.isOscillating = true;
                }
            }
        } else if(this.getType() == TransformType.RESTART || this.getType() == TransformType.STATIC) {
            this.currentStep++;
            if(this.currentStep > this.getFrames()) {
                this.currentStep = 0;
            }
        }

        return point;
    }

    /**
     * @return The Transformation Function
     */
    public BiFunction<Location, Integer, Vector> getTransform() {
        return transform;
    }

    /**
     * @return The {@link TransformType} that is used to control the behavior
     *         of the Transformation when the frame count is reached.
     */
    public TransformType getType() {
        return type;
    }

    /**
     * @return Over how many animation frames this transformation will be applied.
     */
    public int getFrames() {
        return frames;
    }

    /**
     * @return If the transformation is static.
     */
    public boolean isStatic() { return this.getType() == TransformType.STATIC; }

    /**
     * @param type The new {@link TransformType}.
     * @return The {@link ParticleTransform} instance.
     */
    public ParticleTransform setType(TransformType type) {
        this.type = type;
        return this;
    }

    /**
     * @param transform The new transformation function.
     * @return The {@link ParticleTransform} instance.
     */
    public ParticleTransform setTransform(BiFunction<Location, Integer, Vector> transform) {
        this.transform = transform;
        return this;
    }

    /**
     * This method is used to directly apply the transformation function.
     * The location of each Vector can be modified directly.
     *
     * To obtain the transformation function result, use {@link ParticleTransform#apply(Location)}.
     *
     * @param location The location of the {@link me.dessie.dessielib.particleapi.shapes.ShapedParticle} that is being transformed.
     * @param points The locations of all the particles that make up the ShapedParticle.
     */
    public abstract void applyToPoints(Location location, List<Vector> points);
}
