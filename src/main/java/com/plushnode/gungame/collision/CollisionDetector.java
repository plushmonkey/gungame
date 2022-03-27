package com.plushnode.gungame.collision;

import com.plushnode.gungame.collision.volumes.AABB;
import com.plushnode.gungame.collision.volumes.Collider;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class CollisionDetector {
    public static boolean checkEntityCollisions(Player player, Collider collider, CollisionCallback function) {
        return checkEntityCollisions(player, collider, function, true);
    }

    // Checks a collider to see if it's hitting any entities near it.
    // Calls the CollisionCallback when hitting a target.
    // Returns true if it hits a target.
    public static boolean checkEntityCollisions(Player player, Collider collider, CollisionCallback callback, boolean livingOnly) {
        // This is used to increase the lookup volume for nearby entities.
        // Entity locations can be out of the collider volume while still intersecting.
        final double ExtentBuffer = 4.0;

        // Create the extent vector to use as size of bounding box to find nearby entities.
        Vector3D extent = collider.getHalfExtents().add(new Vector3D(ExtentBuffer, ExtentBuffer, ExtentBuffer));

        World world = player.getWorld();
        Vector3D pos = collider.getPosition();
        Location location = new Location(world, pos.getX(), pos.getY(), pos.getZ());

        for (Entity entity : location.getWorld().getNearbyEntities(location, extent.getX(), extent.getY(), extent.getZ())) {
            if (entity == player) continue;
            if (entity instanceof ArmorStand) continue;
            if (entity.isDead()) continue;

            if (entity instanceof Player && ((Player) entity).getGameMode().equals(GameMode.SPECTATOR)) {
                continue;
            }

            if (livingOnly && !(entity instanceof LivingEntity)) {
                continue;
            }

            AABB entityBounds = AABB.fromEntity(entity).at(entity.getLocation());

            if (collider.intersects(entityBounds)) {
                if (callback.onCollision(entity)) {
                    return true;
                }
            }
        }

        return false;
    }

    // Checks if the entity is on the ground. Uses NMS bounding boxes for accuracy.
    public static boolean isOnGround(Entity entity) {
        final double epsilon = 0.01;

        Location location = entity.getLocation();
        AABB entityBounds = AABB.fromEntity(entity).at(location).offset(new Vector3D(0, -epsilon, 0));

        for (int x = -1; x <= 1; ++x) {
            for (int z = -1; z <= 1; ++z) {
                Block checkBlock = location.clone().add(x, -epsilon, z).getBlock();
                if (isAir(checkBlock)) continue;

                AABB checkBounds = AABB.fromBlock(checkBlock).at(checkBlock.getLocation());

                if (entityBounds.intersects(checkBounds)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static double distanceAboveGround(Entity entity) {
        return distanceAboveGround(entity, Collections.emptySet());
    }

    // Cast a ray down to find how far above the ground this entity is.
    public static double distanceAboveGround(Entity entity, Set<Material> groundMaterials) {
        Location location = entity.getLocation().clone();
        Ray ray = new Ray(location, new Vector(0, -1, 0));

        for (double y = location.getY() - 1; y >= 0; --y) {
            location.setY(y);

            Block block = location.getBlock();
            AABB checkBounds;

            if (groundMaterials.contains(block.getType())) {
                checkBounds = AABB.BLOCK_BOUNDS.at(block.getLocation());
            } else {
                checkBounds = AABB.fromBlock(block).at(block.getLocation());
            }

            AABB.RayIntersection rayHit = checkBounds.intersects(ray);

            if (rayHit.hit) {
                return rayHit.distance;
            }
        }

        return Double.MAX_VALUE;
    }

    private static boolean isAir(Block block) {
        Material type = block.getType();

        return type == Material.AIR || type == Material.CAVE_AIR || type == Material.VOID_AIR;
    }

    public interface CollisionCallback {
        // return true to break out of the loop
        boolean onCollision(Entity e);
    }
}
