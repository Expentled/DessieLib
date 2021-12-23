package me.dessie.dessielib.particleapi.shapes;

import me.dessie.dessielib.particleapi.animation.ParticleAnimator;
import me.dessie.dessielib.particleapi.collison.ParticleCollider;
import me.dessie.dessielib.particleapi.transform.ParticleTransform;
import me.dessie.dessielib.particleapi.wrapper.ParticleData;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

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
     * @param points
     * @param shapeFunction
     */
    public ShapedParticle(Particle particle, int points, BiFunction<Location, Integer, Vector> shapeFunction) {
        this(new ParticleData(particle, null), points, shapeFunction);
    }

    public ShapedParticle(ParticleData data, int points, BiFunction<Location, Integer, Vector> shapeFunction) {
        this.particle = data.getParticle();
        this.particleOptions = data.getOptions();
        this.points = points;
        this.shapeFunction = shapeFunction;
        this.particleSpeed = 0;

        this.setAnimator(new ParticleAnimator(5, 1));
    }

    public Particle getParticle() { return particle; }
    public BiFunction<Location, Integer, Vector> getShapeFunction() { return shapeFunction; }
    public int getPoints() { return points; }
    public double getParticleSpeed() { return particleSpeed; }
    public List<ParticleTransform> getTransforms() { return transforms; }
    public ParticleAnimator getAnimator() { return animator; }
    public Object getParticleOptions() { return particleOptions; }
    public List<ParticleCollider<?>> getColliders() { return colliders; }

    public ShapedParticle setParticleSpeed(double particleSpeed) {
        this.particleSpeed = particleSpeed;
        return this;
    }

    public ShapedParticle addTransform(ParticleTransform transform) {
        this.getTransforms().add(transform);
        return this;
    }

    public ShapedParticle setAnimator(ParticleAnimator animator) {
        this.animator = animator;
        this.getAnimator().particle = this;
        return this;
    }

    public ShapedParticle setParticle(Particle particle) {
        this.particle = particle;
        return this;
    }

    public ShapedParticle setParticle(ParticleData data) {
        this.particle = data.getParticle();
        this.particleOptions = data.getOptions();
        return this;
    }

    public ShapedParticle setShapeFunction(BiFunction<Location, Integer, Vector> shapeFunction) {
        this.shapeFunction = shapeFunction;
        return this;
    }

    public ShapedParticle addCollider(ParticleCollider<?> collider) {
        this.getColliders().add(collider);
        return this;
    }

    public void display(Player player, Location location) {
        //Start the animation if it's not already running
        if(!this.getAnimator().isRunning()) {
            this.getAnimator().start(player, location);
        }
    }

    public void display(Location location) {
        //Start the animation if it's not already running
        if(!this.getAnimator().isRunning()) {
            this.getAnimator().start(location);
        }
    }

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
