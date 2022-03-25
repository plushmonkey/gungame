package com.plushnode.gungame.collision.volumes;

import com.plushnode.gungame.util.VectorUtil;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class Sphere implements Collider {
    public Vector3D center;
    public double radius;
    public World world;

    public Sphere(Vector center, double radius) {
        this(VectorUtil.adapt(center), radius, null);
    }

    public Sphere(Vector3D center, double radius) {
        this(center, radius, null);
    }

    public Sphere(Location center, double radius) {
        this(VectorUtil.adapt(center.toVector()), radius, center.getWorld());
    }

    public Sphere(Vector3D center, double radius, World world) {
        this.center = center;
        this.radius = radius;
        this.world = world;
    }

    public Sphere at(Vector3D newCenter) {
        return new Sphere(newCenter, radius, world);
    }

    public Sphere at(Location newCenter) {
        return new Sphere(newCenter, radius);
    }

    public boolean intersects(AABB aabb) {
        if (this.world != null && aabb.getWorld() != null && !aabb.getWorld().equals(this.world)) {
            return false;
        }

        Vector3D min = aabb.min();
        Vector3D max = aabb.max();

        if (min == null || max == null) return false;

        // Get the point closest to sphere center on the aabb.
        double x = Math.max(min.getX(), Math.min(center.getX(), max.getX()));
        double y = Math.max(min.getY(), Math.min(center.getY(), max.getY()));
        double z = Math.max(min.getZ(), Math.min(center.getZ(), max.getZ()));

        // Check if that point is inside of the sphere.
        return contains(new Vector3D(x, y, z));
    }

    public boolean intersects(Sphere other) {
        if (this.world != null && other.getWorld() != null && !other.getWorld().equals(this.world)) {
            return false;
        }

        double distSq = other.center.distanceSq(center);
        double rsum = radius + other.radius;

        // Spheres will be colliding if their distance apart is less than the sum of the radii.
        return distSq <= rsum * rsum;
    }

    @Override
    public boolean intersects(Collider collider) {
        if (this.world != null && collider.getWorld() != null && !collider.getWorld().equals(this.world)) {
            return false;
        }

        if (collider instanceof Sphere) {
            return intersects((Sphere)collider);
        } else if (collider instanceof AABB) {
            return intersects((AABB)collider);
        }

        return false;
    }

    @Override
    public Vector3D getPosition() {
        return center;
    }

    @Override
    public Vector3D getHalfExtents() {
        return new Vector3D(radius, radius, radius);
    }

    @Override
    public World getWorld() {
        return world;
    }

    public boolean contains(Vector3D point) {
        double distSq = center.distanceSq(point);
        return distSq <= radius * radius;
    }
}