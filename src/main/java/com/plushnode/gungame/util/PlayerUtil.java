package com.plushnode.gungame.util;

import com.plushnode.gungame.GunGamePlugin;
import com.plushnode.gungame.attachments.BipodAttachment;
import com.plushnode.gungame.collision.Ray;
import com.plushnode.gungame.collision.volumes.AABB;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public final class PlayerUtil {
    private static AABB HEAD_BOUNDS = new AABB(new Vector3D(-0.3, 0.0D, -0.3D), new Vector3D(0.3D, 0.4, 0.3D));

    private PlayerUtil() {

    }

    public static Location getEye(Player player) {
        Location location = player.getEyeLocation().clone();

        if (player.isSneaking()) {
            // getEyeLocation seems to not align correctly with the game camera's view position.
            // Sneak player height becomes 1.5 blocks tall, so game rendered eye becomes 1.5 - 0.2 = 1.3.
            location = player.getLocation().clone().add(0, 1.3, 0);
        }

        if (GunGamePlugin.plugin.getInstanceManager().getFirstInstance(player, BipodAttachment.class) != null) {
            // Swimming hitbox is 0.6m, so eye should be 0.2 below that.
            location = player.getLocation().clone().add(0, 0.4, 0);
        }

        return location;
    }

    public static boolean isHeadshot(Entity entity, Ray ray, double growRadius) {
        if (!(entity instanceof Player)) return false;
        if (PlayerUtil.isSwimming(entity)) return false;

        Location headLocation = entity.getLocation().clone().add(0, 1.4 + growRadius, 0);

        if (((Player) entity).isSneaking()) {
            headLocation.subtract(0, 0.3, 0);
        }

        AABB headCollider = HEAD_BOUNDS.grow(growRadius, growRadius, growRadius).at(headLocation);

        return headCollider.intersects(ray).hit;
    }

    public static boolean isSwimming(Entity entity) {
        if (!(entity instanceof Player)) return false;

        return ((Player) entity).isSwimming();
    }

    public static void renderBlood(Location location, int bloodAmount) {
        final float bloodSpread = 0.1f;
        final float bloodSpeed = 1.25f;

        location.getWorld().spawnParticle(Particle.BLOCK_DUST, location, bloodAmount, bloodSpread, bloodSpread, bloodSpread, bloodSpeed, Material.REDSTONE_BLOCK.createBlockData(), true);
    }
}
