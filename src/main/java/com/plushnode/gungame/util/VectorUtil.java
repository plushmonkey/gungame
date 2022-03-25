package com.plushnode.gungame.util;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.bukkit.util.Vector;

public final class VectorUtil {
    private VectorUtil() {

    }

    public static Vector3D adapt(Vector v) {
        if (v == null) return null;
        return new Vector3D(v.getX(), v.getY(), v.getZ());
    }

    public static Vector adapt(Vector3D v) {
        if (v == null) return null;
        return new Vector(v.getX(), v.getY(), v.getZ());
    }

    public static Vector3D hadamard(Vector3D v1, Vector3D v2) {
        return new Vector3D(v1.getX() * v2.getX(), v1.getY() * v2.getY(), v1.getZ() * v2.getZ());
    }
}
