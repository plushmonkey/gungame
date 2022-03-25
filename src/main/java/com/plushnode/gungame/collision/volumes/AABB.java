package com.plushnode.gungame.collision.volumes;

import com.plushnode.gungame.collision.Ray;
import com.plushnode.gungame.util.VectorUtil;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.Optional;

public class AABB implements Collider {
    public static final AABB PLAYER_BOUNDS = new AABB(new Vector3D(-0.3, 0.0, -0.3), new Vector3D(0.3, 1.8, 0.3), null);
    public static final AABB BLOCK_BOUNDS = new AABB(new Vector3D(0.0, 0.0, 0.0), new Vector3D(1.0, 1.0, 1.0), null);

    private Vector3D min;
    private Vector3D max;
    private World world;

    public AABB(Vector3D min, Vector3D max) {
        this(min, max, null);
    }

    public AABB(Vector3D min, Vector3D max, World world) {
        this.min = min;
        this.max = max;
        this.world = world;
    }

    public AABB at(Vector3D pos) {
        if (min == null || max == null) return new AABB(null, null, world);

        return new AABB(min.add(pos), max.add(pos), world);
    }

    public AABB at(Location location) {
        if (min == null || max == null) return new AABB(null, null, location.getWorld());

        return at(VectorUtil.adapt(location.toVector()));
    }

    public AABB offset(Vector3D offset) {
        if (min == null || max == null) return new AABB(null, null, world);

        return new AABB(min.add(offset), max.add(offset), world);
    }

    public AABB grow(double x, double y, double z) {
        Vector3D change = new Vector3D(x, y, z);

        return new AABB(min.subtract(change), max.add(change), this.world);
    }

    public AABB scale(double x, double y, double z) {
        Vector3D extents = getHalfExtents();
        Vector3D newExtents = VectorUtil.hadamard(extents, new Vector3D(x, y, z));
        Vector3D diff = newExtents.subtract(extents);

        return grow(diff.getX(), diff.getY(), diff.getZ());
    }

    public AABB scale(double amount) {
        Vector3D extents = getHalfExtents();
        Vector3D newExtents = extents.scalarMultiply(amount);
        Vector3D diff = newExtents.subtract(extents);

        return grow(diff.getX(), diff.getY(), diff.getZ());
    }

    public Vector3D min() {
        return this.min;
    }

    public Vector3D max() {
        return this.max;
    }

    public Vector3D mid() {
        return this.min.add(this.max().subtract(this.min()).scalarMultiply(0.5));
    }

    public boolean contains(Vector3D test) {
        if (min == null || max == null) return false;

        return (test.getX() >= min.getX() && test.getX() <= max.getX()) &&
                (test.getY() >= min.getY() && test.getY() <= max.getY()) &&
                (test.getZ() >= min.getZ() && test.getZ() <= max.getZ());
    }

    // Gets the closest point on the bounding box to the specified p.
    public Vector3D closestPoint(Vector3D p) {
        double x = p.getX();
        double y = p.getY();
        double z = p.getZ();

        if (x < min.getX()) x = min.getX();
        if (x > max.getX()) x = max.getX();

        if (y < min.getY()) y = min.getY();
        if (y > max.getY()) y = max.getY();

        if (z < min.getZ()) z = min.getZ();
        if (z > max.getZ()) z = max.getZ();

        return new Vector3D(x, y, z);
    }

    public static class RayIntersection {
        public Vector3D position;
        public Vector3D normal;
        public double distance;
        public boolean hit;

        RayIntersection() {
            this.hit = false;
            position = Vector3D.ZERO;
            normal = Vector3D.ZERO;
            distance = 0;
        }
    }

    public RayIntersection intersects(Ray ray) {
        RayIntersection result = new RayIntersection();

        if (min == null || max == null) return result;

        double t1 = (min.getX() - ray.origin.getX()) * ray.directionReciprocal.getX();
        double t2 = (max.getX() - ray.origin.getX()) * ray.directionReciprocal.getX();

        double t3 = (min.getY() - ray.origin.getY()) * ray.directionReciprocal.getY();
        double t4 = (max.getY() - ray.origin.getY()) * ray.directionReciprocal.getY();

        double t5 = (min.getZ() - ray.origin.getZ()) * ray.directionReciprocal.getZ();
        double t6 = (max.getZ() - ray.origin.getZ()) * ray.directionReciprocal.getZ();

        double tmin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
        double tmax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));

        if (tmax < 0 || tmin > tmax) {
            return result;
        }

        if (tmin == t1) {
            result.normal = new Vector3D(-1, 0, 0);
        } else if (tmin == t2) {
            result.normal = new Vector3D(1, 0, 0);
        } else if (tmin == t3) {
            result.normal = new Vector3D(0, -1, 0);
        } else if (tmin == t4) {
            result.normal = new Vector3D(0, 1, 0);
        } else if (tmin == t5) {
            result.normal = new Vector3D(0, 0, -1);
        } else if (tmin == t6) {
            result.normal = new Vector3D(0, 0, 1);
        }

        result.hit = true;
        result.distance = tmin;
        result.position = ray.origin.add(ray.direction.scalarMultiply(tmin));

        return result;
    }

    public boolean intersects(AABB other) {
        if (this.world != null && other.getWorld() != null && !other.getWorld().equals(this.world)) {
            return false;
        }

        if (min == null || max == null || other.min == null || other.max == null) {
            return false;
        }

        return (max.getX() > other.min.getX() &&
                min.getX() < other.max.getX() &&
                max.getY() > other.min.getY() &&
                min.getY() < other.max.getY() &&
                max.getZ() > other.min.getZ() &&
                min.getZ() < other.max.getZ());
    }

    public boolean intersects(Sphere sphere) {
        if (this.world != null && sphere.getWorld() != null && !sphere.getWorld().equals(this.world)) {
            return false;
        }

        return sphere.intersects(this);
    }

    @Override
    public boolean intersects(Collider collider) {
        if (this.world != null && collider.getWorld() != null && !collider.getWorld().equals(this.world)) {
            return false;
        }

        if (collider instanceof Sphere) {
            return intersects((Sphere) collider);
        } else if (collider instanceof AABB) {
            return intersects((AABB) collider);
        }

        return false;
    }

    @Override
    public Vector3D getPosition() {
        return mid();
    }

    @Override
    public Vector3D getHalfExtents() {
        if (max == null || min == null) return new Vector3D(0, 0, 0);

        Vector3D half = max.subtract(min).scalarMultiply(0.5);
        return new Vector3D(Math.abs(half.getX()), Math.abs(half.getY()), Math.abs(half.getZ()));
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public String toString() {
        return "[AABB min: " + min + ", max: " + max + "]";
    }

    public static AABB fromEntity(Entity entity) {
        Vector worldMin = entity.getBoundingBox().getMin();
        Vector localMin = worldMin.clone().subtract(entity.getLocation().toVector());
        Vector worldMax = entity.getBoundingBox().getMax();
        Vector localMax = worldMax.clone().subtract(entity.getLocation().toVector());

        Vector3D min = VectorUtil.adapt(localMin);
        Vector3D max = VectorUtil.adapt(localMax);

        return new AABB(min, max, entity.getWorld());
    }

    public static AABB fromBlock(Block block) {
        Vector worldMin = block.getBoundingBox().getMin();
        Vector localMin = worldMin.clone().subtract(block.getLocation().toVector());
        Vector worldMax = block.getBoundingBox().getMax();
        Vector localMax = worldMax.clone().subtract(block.getLocation().toVector());

        Vector3D min = VectorUtil.adapt(localMin);
        Vector3D max = VectorUtil.adapt(localMax);

        return new AABB(min, max, block.getWorld());
    }
}
