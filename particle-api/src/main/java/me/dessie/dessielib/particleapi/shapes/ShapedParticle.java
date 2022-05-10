package me.dessie.dessielib.particleapi.shapes;

import me.dessie.dessielib.particleapi.animation.ParticleAnimator;
import me.dessie.dessielib.particleapi.collison.ParticleCollider;
import me.dessie.dessielib.particleapi.transform.ParticleTransform;
import me.dessie.dessielib.particleapi.transform.TransformType;
import me.dessie.dessielib.particleapi.transform.orientation.Axis;
import me.dessie.dessielib.particleapi.transform.orientation.Orientation;
import me.dessie.dessielib.particleapi.transform.transformations.ParticleRotate;
import me.dessie.dessielib.particleapi.wrapper.ParticleData;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * Defines a shape of {@link Particle} that can be rendered into the {@link org.bukkit.World} or for a specific {@link Player}.
 */
public class ShapedParticle {
    private Particle particle;
    private Object particleOptions;

    private final int points;
    private BiFunction<Location, Integer, Vector> shapeFunction;
    private double particleSpeed;
    private final List<ParticleTransform> transforms = new ArrayList<>();
    private final List<ParticleCollider<?>> colliders = new ArrayList<>();
    private ParticleAnimator animator;

    /**
     * @param particle The {@link Particle} to render.
     * @param points The number of points to render in the ShapedParticle.
     * @param shapeFunction Defines how the ShapedParticle is rendered. Takes in an initial location and a "step" value.
     *                      Should return a Vector position for the particle at the specific step.
     */
    public ShapedParticle(Particle particle, int points, BiFunction<Location, Integer, Vector> shapeFunction) {
        this(new ParticleData(particle, null), points, shapeFunction);
    }

    /**
     * @param data The {@link ParticleData} to render.
     * @param points The number of points to render in the ShapedParticle.
     * @param shapeFunction Defines how the ShapedParticle is rendered. Takes in an initial location and a "step" value.
     *                      Should return a Vector position for the particle at the specific step.
     */
    public ShapedParticle(ParticleData data, int points, BiFunction<Location, Integer, Vector> shapeFunction) {
        this.setParticle(data);
        this.setShapeFunction(shapeFunction);
        this.points = points;
        this.particleSpeed = 0;

        this.setAnimator(new ParticleAnimator(5, 1));
    }

    /**
     * @return The {@link Particle} that will be rendered.
     */
    public Particle getParticle() { return particle; }

    /**
     * @return The {@link BiFunction} that controls how this ShapedParticle is shaped.
     */
    public BiFunction<Location, Integer, Vector> getShapeFunction() { return shapeFunction; }

    /**
     * @return How many points, or steps, are used to create the Shaped Particle.
     */
    public int getPoints() { return points; }

    /**
     * @return The movement speed of the particle.
     * @see org.bukkit.World#spawnParticle(Particle, Location, int, double, double, double, double)
     */
    public double getParticleSpeed() { return particleSpeed; }

    /**
     * @return All applied {@link ParticleTransform}s that will be applied when the ShapedParticle is being calculated.
     */
    public List<ParticleTransform> getTransforms() { return transforms; }

    /**
     * @return The {@link ParticleAnimator} that controls the particle's animation behavior.
     */
    public ParticleAnimator getAnimator() { return animator; }

    /**
     * @return The particle options, used when spawning the particles.
     * @see org.bukkit.World#spawnParticle(Particle, Location, int, double, double, double, Object)
     */
    public Object getParticleOptions() { return particleOptions; }

    /**
     * @return The registered {@link ParticleCollider} list that are fired when a Particle collides.
     */
    public List<ParticleCollider<?>> getColliders() { return colliders; }

    /**
     * Sets the particle's spawn speed.
     *
     * @param particleSpeed How fast the particle should be when it is rendered.
     * @return The ShapedParticle instance.
     * @see org.bukkit.World#spawnParticle(Particle, Location, int, double, double, double, double)
     */
    public ShapedParticle setParticleSpeed(double particleSpeed) {
        this.particleSpeed = particleSpeed;
        return this;
    }

    /**
     * Adds a {@link ParticleTransform} to the ShapedParticle. When calculating a particle point, all transformations will be applied in order.
     *
     * @param transform The ParticleTransform to add.
     * @return The ShapedParticle instance.
     */
    public ShapedParticle addTransform(ParticleTransform transform) {
        Objects.requireNonNull(transform, "Cannot add a null transform!");

        this.getTransforms().add(transform);
        return this;
    }

    /**
     * Sets the {@link ParticleAnimator} for this ShapedParticle
     *
     * @param animator The ParticleAnimator to use.
     * @return The ShapedParticle instance.
     */
    public ShapedParticle setAnimator(ParticleAnimator animator) {
        Objects.requireNonNull(animator, "Cannot use a null animator!");

        this.animator = animator;
        this.getAnimator().setParticle(this);
        return this;
    }

    /**
     * Changes which {@link Particle} is rendered by the ShapedParticle.
     *
     * @param particle The new particle to render.
     * @return The ShapedParticle instance.
     */
    public ShapedParticle setParticle(Particle particle) {
        Objects.requireNonNull(particle, "Cannot render a null particle!");
        this.particle = particle;
        return this;
    }

    /**
     * Changes which {@link ParticleData} is rendered by the ShapedParticle.
     * @param data The new ParticleData to render.
     * @return The ShapedParticle instance.
     */
    public ShapedParticle setParticle(ParticleData data) {
        Objects.requireNonNull(data, "Cannot render a null particle!");
        this.particle = data.getParticle();
        this.particleOptions = data.getOptions();
        return this;
    }

    /**
     * Changes the {@link BiFunction} that controls the ShapedParticle's shape.
     *
     * @param shapeFunction The new shape function.
     * @return The ShapedParticle instance.
     */
    public ShapedParticle setShapeFunction(BiFunction<Location, Integer, Vector> shapeFunction) {
        Objects.requireNonNull(shapeFunction, "Cannot use null function!");
        this.shapeFunction = shapeFunction;
        return this;
    }

    /**
     * Adds a {@link ParticleCollider} to the ShapedParticle.
     *
     * @param collider The ParticleCollider
     * @return The ShapedParticle instance.
     */
    public ShapedParticle addCollider(ParticleCollider<?> collider) {
        Objects.requireNonNull(collider, "Cannot add null collider!");
        this.getColliders().add(collider);
        return this;
    }

    /**
     * Applies any number of {@link Orientation}s to the particle. This will apply the necessary {@link ParticleRotate}
     * to orientate the particle with your specified angle on the axis.
     *
     * @param orientations The orientations to apply
     * @return The ShapedParticle instance.
     */
    public ShapedParticle addOrientation(Orientation... orientations) {
        for(Orientation orientation : orientations) {
            this.addTransform(new ParticleRotate(TransformType.STATIC, 1, ((location, step) -> {
                Axis axis = orientation.getAxis();
                return new Vector(
                        axis == Axis.X ? orientation.getRotation() : 0,
                        axis == Axis.Y ? orientation.getRotation() : 0,
                        axis == Axis.Z ? orientation.getRotation() : 0);
            })));
        }

        return this;
    }

    /**
     * Tells the {@link ParticleAnimator} to start rendering the ShapedParticle.
     * This particle will ONLY be rendered to the specified player, and only they will see it.
     *
     * @param player The {@link Player} to render the particle for.
     * @param location The {@link Location} to render the particle.
     */
    public void display(Player player, Location location) {
        //Start the animation if it's not already running
        if(!this.getAnimator().isRunning()) {
            this.getAnimator().start(player, location);
        }
    }

    /**
     * Tells the {@link ParticleAnimator} to start rendering the ShapedParticle.
     * This particle will be rendered to all players.
     *
     * @param location The {@link Location} to render the particle.
     */
    public void display(Location location) {
        //Start the animation if it's not already running
        if(!this.getAnimator().isRunning()) {
            this.getAnimator().start(location);
        }
    }

    /**
     * Calculates all the {@link Vector} locations for the ShapedParticle.
     * This applies all needed {@link ParticleTransform}s, and applies any necessary {@link ParticleCollider}s.
     *
     * This method, however, does not render the particles.
     * If you wish to use a ShapedParticle and just the points for some other application,
     * this method should work flawlessly.
     *
     * @param location The {@link Location} to render the particle.
     * @return A list containing all locations for each particle that makeup the ShapedParticle.
     */
    public List<Vector> getPoints(Location location) {
        List<Vector> points = new ArrayList<>();

        //Calculate each particle point, by applying them to the shape function.
        for (int i = 0; i < this.getPoints(); i++) {
            points.add(this.getShapeFunction().apply(location, i));
        }

        //Apply the Transformations
        this.getTransforms().forEach(transform -> {
            if(transform.isStatic()) {
                //Get a complete copy of the Shape Points.
                List<Vector> temp = new ArrayList<>(points.stream().map(point -> new Vector(point.getX(), point.getY(), point.getZ())).toList());

                //Apply to the first set of points.
                transform.applyToPoints(location, points);

                //Now apply the transformation to each subsequent frame.
                List<Vector> toApply = new ArrayList<>(temp.stream().map(point -> new Vector(point.getX(), point.getY(), point.getZ())).toList());
                for(int i = 1; i < transform.getFrames(); i++) {
                    transform.applyToPoints(location, toApply);
                    points.addAll(toApply);
                    toApply = new ArrayList<>(temp.stream().map(point -> new Vector(point.getX(), point.getY(), point.getZ())).toList());
                }
            } else {
                transform.applyToPoints(location, points);
            }
        });

        this.getColliders().forEach(collider -> {
            collider.startCollide(this, location.getWorld(), points);
        });

        return points;
    }
}
