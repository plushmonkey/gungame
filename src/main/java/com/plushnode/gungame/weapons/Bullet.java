package com.plushnode.gungame.weapons;

import com.plushnode.gungame.DamageTracker;
import com.plushnode.gungame.GunGamePlugin;
import com.plushnode.gungame.collision.CollisionDetector;
import com.plushnode.gungame.collision.Ray;
import com.plushnode.gungame.collision.volumes.AABB;
import com.plushnode.gungame.collision.volumes.Sphere;
import com.plushnode.gungame.util.PlayerUtil;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class Bullet {
    private Weapon weapon;
    private Player shooter;
    private Location location;
    private Location origin;
    private Vector direction;

    protected Config config;

    public Bullet(Weapon weapon, Config config, Player shooter, Location location, Vector direction) {
        this.weapon = weapon;
        this.location = location.clone();
        this.origin = this.location.clone();
        this.config = config;
        this.shooter = shooter;
        this.direction = direction.clone();
    }

    public void render() {
        location.getWorld().spawnParticle(Particle.BLOCK_DUST, location, 1,0.0f, 0.0f, 0.0f, 0.0f, Material.OBSIDIAN.createBlockData(), true);
    }

    // Returns true if being removed
    public boolean update() {
        if (!shooter.isOnline()) {
            return true;
        }

        final double movement = 1.0 / 5.0 * config.speed;

        for (int i = 0; i < 5; ++i) {
            final Ray ray = new Ray(location, direction);
            RayTraceResult result = location.getWorld().rayTraceBlocks(location, direction, movement, FluidCollisionMode.NEVER, true);

            if (intersectEntities(ray, location, movement)) {
                return true;
            }

            if (result != null) {
                this.location = result.getHitPosition().toLocation(location.getWorld());

                location.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, location, 5,0.1f, 0.1f, 0.1f, 0.0f, null, true);

                return true;
            }

            location = location.add(direction.clone().multiply(movement));

            render();
        }

        if (location.distanceSquared(origin) >= config.range * config.range) {
            return true;
        }

        return false;
    }

    private boolean intersectEntities(Ray ray, Location previous, double colliderSize) {
        Sphere collider = new Sphere(location.toVector(), colliderSize);

        boolean collided = CollisionDetector.checkEntityCollisions(shooter, collider, entity -> {
            AABB hitbox = AABB.fromEntity(entity).grow(config.radius, config.radius, config.radius).at(entity.getLocation());

            AABB.RayIntersection intersectResult = hitbox.intersects(ray);
            if (intersectResult.hit) {
                double distance = intersectResult.distance;

                Location hitLocation = previous.clone().add(direction.clone().multiply(distance));

                int bloodAmount = 50;
                double damage = config.damage;

                boolean headshot = PlayerUtil.isHeadshot(entity, ray, config.radius);
                if (headshot) {
                    damage = config.headshotDamage;

                    bloodAmount = 200;
                } else if (PlayerUtil.isSwimming(entity)) {
                    damage = config.swimmingDamage;
                }

                GunGamePlugin.plugin.getDamageTracker().applyDamage(entity, new DamageTracker.DamageEvent(weapon, damage, headshot));

                if (config.clearNoTicks) {
                    ((LivingEntity)entity).setNoDamageTicks(0);
                }

                PlayerUtil.renderBlood(hitLocation, bloodAmount);

                return true;
            }

            return false;
        });

        return collided;
    }

    public static class Config {
        double speed;
        double range;
        double damage;
        double radius;
        double headshotDamage;
        double swimmingDamage;
        boolean clearNoTicks;
    }
}
