package com.plushnode.gungame.weapons;

import com.plushnode.gungame.collision.CollisionDetector;
import com.plushnode.gungame.collision.Ray;
import com.plushnode.gungame.collision.RayCaster;
import com.plushnode.gungame.collision.volumes.AABB;
import com.plushnode.gungame.collision.volumes.Sphere;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Collections;

public class Bullet {
    private static AABB HEAD_BOUNDS = new AABB(new Vector3D(-0.3D, 0.0D, -0.3D), new Vector3D(0.3D, 0.4D, 0.3D));

    private Player shooter;
    private Location location;
    private Location origin;
    private Vector direction;

    protected Config config;

    public Bullet(Config config, Player shooter, Location location, Vector direction) {
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
            RayCaster.CastResult result = RayCaster.cast(location.getWorld(), ray, movement, false, Collections.emptyList());

            if (intersectEntities(ray, location, movement)) {
                return true;
            }

            if (result.hit) {
                this.location = result.location;

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
            AABB hitbox = AABB.fromEntity(entity).at(entity.getLocation());

            AABB.RayIntersection intersectResult = hitbox.intersects(ray);
            if (intersectResult.hit) {
                double distance = intersectResult.distance;

                Location hitLocation = previous.clone().add(direction.clone().multiply(distance));

                int bloodAmount = 50;
                double damage = config.damage;

                if (isHeadshot(entity, ray)) {
                    damage = config.headshotDamage;

                    bloodAmount = 200;
                } else if (isSwimming(entity)) {
                    damage = config.swimmingDamage;
                }

                ((LivingEntity)entity).damage(damage, shooter);

                renderBlood(hitLocation, bloodAmount);

                return true;
            }

            return false;
        });

        return collided;
    }

    private boolean isHeadshot(Entity entity, Ray ray) {
        if (!(entity instanceof Player)) return false;

        Location headLocation = entity.getLocation().clone().add(0, 1.4, 0);

        if (((Player) entity).isSneaking()) {
            headLocation.subtract(0, 0.3, 0);
        }

        AABB headCollider = HEAD_BOUNDS.at(headLocation);

        return headCollider.intersects(ray).hit;
    }

    private boolean isSwimming(Entity entity) {
        if (!(entity instanceof Player)) return false;

        return ((Player) entity).isSwimming();
    }

    protected void renderBlood(Location location, int bloodAmount) {
        final float bloodSpread = 0.1f;
        final float bloodSpeed = 1.25f;

        location.getWorld().spawnParticle(Particle.BLOCK_DUST, location, bloodAmount, bloodSpread, bloodSpread, bloodSpread, bloodSpeed, Material.REDSTONE_BLOCK.createBlockData(), true);
    }

    public static class Config {
        double speed;
        double range;
        double damage;
        double headshotDamage;
        double swimmingDamage;
    }
}
