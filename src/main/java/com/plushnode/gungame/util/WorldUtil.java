package com.plushnode.gungame.util;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class WorldUtil {
    private WorldUtil() {

    }

    public static List<LivingEntity> getEntitiesAroundPoint(Location location, double radius) {
        List<LivingEntity> nearbyEntities = new ArrayList<>();
        double radiusSq = radius * radius;

        Collection<Entity> entities = location.getWorld().getNearbyEntities(location, radius + 2, radius + 2, radius + 2);
        for (Entity entity : entities) {
            if (!(entity instanceof LivingEntity)) continue;
            if (entity.isDead()) continue;
            if (entity instanceof ArmorStand) continue;
            if (entity instanceof Player && ((Player) entity).getGameMode().equals(GameMode.SPECTATOR)) {
                continue;
            }

            if (entity.getLocation().distanceSquared(location) <= radiusSq) {
                nearbyEntities.add((LivingEntity)entity);
            }
        }
        return nearbyEntities;
    }
}
