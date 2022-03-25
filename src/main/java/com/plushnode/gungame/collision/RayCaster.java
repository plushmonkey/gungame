package com.plushnode.gungame.collision;

import com.plushnode.gungame.collision.volumes.AABB;
import com.plushnode.gungame.util.VectorUtil;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class RayCaster {
    private static final List<Vector3D> DIRECTIONS = Arrays.asList(
            Vector3D.ZERO,
            Vector3D.PLUS_I, Vector3D.PLUS_J, Vector3D.PLUS_K,
            Vector3D.MINUS_I, Vector3D.MINUS_J, Vector3D.MINUS_K,

            new Vector3D(0, 1, 1), new Vector3D(0, 1, -1),
            new Vector3D(1, 1, 0), new Vector3D(1, 1, 1), new Vector3D(1, 1, -1),
            new Vector3D(-1, 1, 0), new Vector3D(-1, 1, 1), new Vector3D(-1, 1, -1),

            new Vector3D(0, -1, 1), new Vector3D(0, -1, -1),
            new Vector3D(1, -1, 0), new Vector3D(1, -1, 1), new Vector3D(1, -1, -1),
            new Vector3D(-1, -1, 0), new Vector3D(-1, -1, 1), new Vector3D(-1, -1, -1)
    );

    public static Block blockCast(World world, Ray ray, double maxRange, boolean liquidCollision) {
        Location origin = new Location(world, ray.origin.getX(), ray.origin.getY(), ray.origin.getZ());
        double closestDistance = Double.MAX_VALUE;
        Block closestBlock = null;

        // Progress through each block and check all neighbors for ray intersection.
        for (double i = 0; i < maxRange + 1; ++i) {
            Location current = origin.clone().add(VectorUtil.adapt(ray.direction.scalarMultiply(i)));
            for (Vector3D direction : DIRECTIONS) {
                Location check = current.clone().add(VectorUtil.adapt(direction));
                Block block = check.getBlock();
                AABB blockBounds = AABB.fromBlock(block);

                if (isTransparent(block)) {
                    continue;
                }

                if (liquidCollision && block.isLiquid()) {
                    blockBounds = AABB.BLOCK_BOUNDS.at(block.getLocation());
                }

                AABB.RayIntersection result = blockBounds.intersects(ray);
                if (result.hit) {
                    double distance = result.distance;
                    if (distance < closestDistance && distance >= 0) {
                        closestDistance = distance;
                        closestBlock = block;
                    }
                }
            }

            // Break early after checking all neighbors for intersection.
            if (closestDistance < maxRange) {
                //break;
            }
        }

        return closestBlock;
    }

    public static CastResult cast(World world, Ray ray, double maxRange, boolean liquidCollision, List<Block> ignoreBlocks) {
        Location origin = new Location(world, ray.origin.getX(), ray.origin.getY(), ray.origin.getZ());
        double closestDistance = Double.MAX_VALUE;
        Block collidedBlock = null;
        Vector3D normal = Vector3D.ZERO;

        // Progress through each block and check all neighbors for ray intersection.
        for (double i = 0; i < maxRange + 1; ++i) {
            Location current = origin.clone().add(VectorUtil.adapt(ray.direction.scalarMultiply(i)));
            for (Vector3D direction : DIRECTIONS) {
                Location check = current.clone().add(VectorUtil.adapt(direction));
                Block block = check.getBlock();

                if (ignoreBlocks.contains(block)) {
                    continue;
                }

                if (isTransparent(block)) {
                    continue;
                }

                AABB blockBounds;

                if (liquidCollision && block.isLiquid()) {
                    blockBounds = AABB.BLOCK_BOUNDS.at(block.getLocation());
                } else {
                    blockBounds = AABB.fromBlock(block).at(block.getLocation());
                }

                AABB.RayIntersection result = blockBounds.intersects(ray);
                if (result.hit) {
                    double distance = result.distance;
                    if (distance < closestDistance && distance >= 0) {
                        closestDistance = distance;
                        collidedBlock = block;
                        normal = result.normal;
                    }
                }
            }

            // Break early after checking all neighbors for intersection.
            if (closestDistance < maxRange) {
                break;
            }
        }

        CastResult result = new CastResult();

        result.hit = closestDistance < maxRange;
        closestDistance = Math.min(closestDistance, maxRange);
        result.location = origin.add(VectorUtil.adapt(ray.direction.scalarMultiply(closestDistance)));
        result.distance = closestDistance;
        result.collidedBlock = collidedBlock;
        result.normal = normal;

        return result;
    }

    private static boolean isTransparent(Block block) {
        Material type = block.getType();

        return !type.isOccluding() && !type.isSolid();
    }

    public static class CastResult {
        public boolean hit;
        public Location location;
        public double distance;
        public Block collidedBlock;
        public Vector3D normal;
    }
}
