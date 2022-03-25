package com.plushnode.gungame.collision;

import com.plushnode.gungame.util.VectorUtil;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class Ray {
    public Vector3D origin;
    public Vector3D direction;
    public Vector3D directionReciprocal;

    public Ray(Vector3D origin, Vector3D direction) {
        this.direction = direction;
        this.origin = origin;
        this.directionReciprocal = new Vector3D(0, 0, 0);

        if (direction.getX() == 0.0) {
            directionReciprocal = new Vector3D(Double.MAX_VALUE, directionReciprocal.getY(), directionReciprocal.getZ());
        } else {
            directionReciprocal = new Vector3D(1.0 / direction.getX(), directionReciprocal.getY(), directionReciprocal.getZ());
        }

        if (direction.getY() == 0.0) {
            directionReciprocal = new Vector3D(directionReciprocal.getX(), Double.MAX_VALUE, directionReciprocal.getZ());
        } else {
            directionReciprocal = new Vector3D(directionReciprocal.getX(), 1.0 / direction.getY(), directionReciprocal.getZ());
        }

        if (direction.getZ() == 0.0) {
            directionReciprocal = new Vector3D(directionReciprocal.getX(), directionReciprocal.getY(), Double.MAX_VALUE);
        } else {
            directionReciprocal = new Vector3D(directionReciprocal.getX(), directionReciprocal.getY(), 1.0 / direction.getZ());
        }
    }

    public Ray(Location origin, Vector3D direction) {
        this(VectorUtil.adapt(origin.toVector()), direction);
    }

    public Ray(Location origin, Vector direction) {
        this(VectorUtil.adapt(origin.toVector()), new Vector3D(direction.getX(), direction.getY(), direction.getZ()));
    }

    public Ray(Vector origin, Vector direction) {
        this(VectorUtil.adapt(origin), new Vector3D(direction.getX(), direction.getY(), direction.getZ()));
    }
}
