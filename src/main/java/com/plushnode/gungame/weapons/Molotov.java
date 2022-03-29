package com.plushnode.gungame.weapons;

import com.plushnode.gungame.*;
import com.plushnode.gungame.physics.Particle;
import com.plushnode.gungame.util.PlayerUtil;
import com.plushnode.gungame.util.VectorUtil;
import com.plushnode.gungame.util.WorldUtil;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Molotov implements Weapon {
    private List<GrenadeParticle> particles = new ArrayList<>();
    private Player player;
    private World world;
    private State state;

    @Override
    public boolean activate(Player player, Trigger trigger) {
        this.player = player;
        this.world = player.getWorld();

        if (trigger == Trigger.LeftClick || trigger == Trigger.RightClick) {
            boolean handledPunch = false;

            for (Molotov instance : GunGamePlugin.plugin.getInstanceManager().getPlayerInstances(player, Molotov.class)) {
                if (instance.state.onPunch()) {
                    handledPunch = true;
                }
            }

            if (handledPunch) {
                return false;
            }

            GunPlayer gunPlayer = GunGamePlugin.plugin.getPlayerManager().getPlayer(player);
            if (gunPlayer == null || gunPlayer.isOnCooldown(this.getName())) {
                return false;
            }

            state = new ThrownState(0.5);
            return true;
        } else if (trigger == Trigger.Sneak) {
            GunPlayer gunPlayer = GunGamePlugin.plugin.getPlayerManager().getPlayer(player);
            if (gunPlayer == null || gunPlayer.isOnCooldown(this.getName())) {
                return false;
            }

            state = new PowerSelectState();

            return true;
        }

        return false;
    }

    @Override
    public UpdateResult update() {
        if (!player.getWorld().equals(this.world)) {
            return UpdateResult.Remove;
        }

        if (player.getGameMode() == GameMode.SPECTATOR) {
            return UpdateResult.Remove;
        }

        if (!player.isOnline() || !this.state.update()) {
            return UpdateResult.Remove;
        }

        return UpdateResult.Continue;
    }

    @Override
    public void destroy() {
        for (GrenadeParticle particle : particles) {
            particle.remove();
        }
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

    @Override
    public String getName() {
        return "Molotov";
    }

    private interface State {
        boolean update();
        boolean onPunch();
    }

    private class PowerSelectState implements State {
        private double oldLevel;
        private double strength = 0.5;
        private long startTime;

        PowerSelectState() {
            this.oldLevel = player.getLevel() + player.getExp();
            player.setLevel(0);
            startTime = System.currentTimeMillis();
        }

        void setLevel(double level) {
            player.setExp((float)(level - (int)level));
            player.setLevel((int)level);
        }

        @Override
        public boolean update() {
            long time = System.currentTimeMillis();

            if (!player.isSneaking()) {
                this.setLevel(oldLevel);
                return false;
            }

            long dt = time - startTime - 300;

            strength = Math.sin((double)dt / 300.0f) * 0.5 + 0.5;
            player.setExp((float)strength);

            return true;
        }

        @Override
        public boolean onPunch() {
            state = new ThrownState(strength);
            this.setLevel(oldLevel);
            return true;
        }
    }

    private class ThrownState implements State {
        long throwTime;

        ThrownState(double strength) {
            this.throwTime = System.currentTimeMillis();

            Vector3D direction = VectorUtil.adapt(player.getEyeLocation().getDirection());
            Vector3D position = VectorUtil.adapt(PlayerUtil.getEye(player).toVector());
            GrenadeParticle particle = new GrenadeParticle(position, 1.0);

            double force = 50.0 + (750.0 * strength);

            particle.addForce(direction.scalarMultiply(force));
            particle.setAcceleration(new Vector3D(0.0, -20.0, 0.0));
            particle.setDamping(0.8);
            particle.setRestitution(0.4);

            particles.add(particle);

            GunPlayer gunPlayer = GunGamePlugin.plugin.getPlayerManager().getPlayer(player);

            if (gunPlayer != null) {
                gunPlayer.addCooldown(getName(), 10000);
            }

            //ItemStack item = player.getInventory().getItemInMainHand();
            //PlayerUtil.consumeItem(player, item);
        }

        public boolean update() {
            long time = System.currentTimeMillis();

            for (GrenadeParticle renderable : particles) {
                renderable.render();
            }

            if (time > throwTime + 3000) {
                state = new ExplodeState();
            }

            return player.isOnline();
        }

        @Override
        public boolean onPunch() {
            return false;
        }
    }

    private class ExplodeState implements State {
        private final static int RADIUS = 5;
        private final static float RENDER_SPREAD = 0.25f;
        private final static double DAMAGE = 4.0;
        private List<Location> locations = new ArrayList<>();
        private long lastRenderTime;
        private long explodeTime;
        private Location explosionCenter;
        private Map<Entity, Long> damageMap = new HashMap<>();

        ExplodeState() {
            explodeTime = System.currentTimeMillis();

            for (GrenadeParticle particle : particles) {
                Location location = new Location(world, particle.getPosition().getX(), particle.getPosition().getY(), particle.getPosition().getZ());

                RayTraceResult result = location.getWorld().rayTraceBlocks(location, new Vector(0, -1, 0), 2.0);

                world.playSound(location, Sound.BLOCK_FIRE_AMBIENT, 4f, 1f);

                if (result == null) continue;

                location = result.getHitPosition().toLocation(location.getWorld());
                explosionCenter = location.clone().add(0, 1, 0);
                Location centerTop = location.clone().add(0, 1, 0);

                locations.add(location.add(0.0, 0.25, 0.0));

                for (float z = -RADIUS; z < RADIUS; ++z) {
                    for (float x = -RADIUS; x < RADIUS; ++x) {
                        Location current = location.clone().add(x, 0.25, z);

                        if (current.distanceSquared(location) <= RADIUS * RADIUS && current.getBlock().isPassable() && !current.getBlock().isLiquid()) {
                            Location currentTop = current.clone().add(0, 0.75, 0);
                            Vector direction = currentTop.subtract(centerTop).toVector();

                            double length = direction.length();

                            if (location.getWorld().rayTraceBlocks(centerTop, direction.normalize(), length) == null) {
                                locations.add(current);
                            }
                        }
                    }
                }
            }
        }

        private void renderFire() {
            long time = System.currentTimeMillis();

            if (time - lastRenderTime >= 150) {
                RealDistribution distribution = new NormalDistribution(0.0f, 30.0f);

                for (Location location : locations) {
                    int sample = (int)Math.abs(distribution.sample()) % 40;

                    float hue = sample / 360.0f;
                    int rgb = java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f);
                    java.awt.Color color = new java.awt.Color(rgb);
                    Color dustColor = Color.fromRGB(color.getRed(), color.getGreen(), color.getBlue());

                    org.bukkit.Particle.DustOptions dustOptions = new org.bukkit.Particle.DustOptions(dustColor, 2.0f);

                    world.spawnParticle(org.bukkit.Particle.REDSTONE, location, 1, RENDER_SPREAD, RENDER_SPREAD, RENDER_SPREAD, 0.001f, dustOptions, true);
                }

                lastRenderTime = time;
            }
        }

        @Override
        public boolean update() {
            long time = System.currentTimeMillis();
            renderFire();

            for (LivingEntity entity : WorldUtil.getEntitiesAroundPoint(explosionCenter, RADIUS + 1)) {
                Long lastHitTime = damageMap.get(entity);

                // Put a delay on player damage per second
                if (lastHitTime != null && time - lastHitTime < 1000) continue;

                for (Location location : locations) {
                    if (BoundingBox.of(location.getBlock()).overlaps(entity.getBoundingBox())) {
                        GunGamePlugin.plugin.getDamageTracker().applyDamage(entity, new DamageTracker.DamageEvent(Molotov.this, DAMAGE, false));
                        damageMap.put(entity, time);
                        break;
                    }
                }
            }

            return System.currentTimeMillis() - explodeTime < 7000 && !locations.isEmpty();
        }

        @Override
        public boolean onPunch() {
            return false;
        }
    }

    private class GrenadeParticle extends Particle {
        private static final float ROTATION_SPEED = 2*3.14f / 10.0f;
        Vector3D prevPos;
        float rotation;

        public GrenadeParticle(Vector3D position, double mass) {
            super(position, mass);

            this.prevPos = getPosition();
            this.rotation = 0.0f;

            GunGamePlugin.plugin.getPhysicsSystem().addParticle(this, player.getWorld());
        }

        public void render() {
            Vector3D pos = getPosition();
            Location location = new Location(player.getWorld(), pos.getX(), pos.getY(), pos.getZ());
            Location prevLocation = new Location(player.getWorld(), prevPos.getX(), prevPos.getY(), prevPos.getZ());

            Vector travel = location.clone().subtract(prevLocation).toVector();

            Color bottleColor = Color.fromRGB(24, 141, 86);
            Color fireColor = Color.fromRGB(215, 28, 6);

            double increment = 0.3;

            if (prevLocation.distanceSquared(location) < 0.25 * 0.25) {
                increment = 1.0;
            }

            Vector worldUp = new Vector(0, 1, 0);
            Vector direction = travel.clone().normalize().setY(0).normalize();
            Vector rotateAxis = worldUp.clone().crossProduct(direction).normalize();

            for (double t = 0; t < 1.0; t += increment) {
                // Interpolate between the two positions to render multiple times.
                Location bottleLocation = prevLocation.clone().add(travel.clone().multiply(t));

                player.getWorld().spawnParticle(org.bukkit.Particle.REDSTONE,
                        bottleLocation, 1, 0.001f, 0.001f, 0.001f, 0.001f, new org.bukkit.Particle.DustOptions(bottleColor, 1.0f),
                        true);

                Vector offset = worldUp.clone().rotateAroundAxis(rotateAxis, rotation + t * ROTATION_SPEED);
                Location fireLocation = bottleLocation.clone().add(offset.clone().multiply(0.35f));

                player.getWorld().spawnParticle(org.bukkit.Particle.REDSTONE,
                        fireLocation, 1, 0.001f, 0.001f, 0.001f, 0.001f, new org.bukkit.Particle.DustOptions(fireColor, 1.0f),
                        true);
            }

            rotation += ROTATION_SPEED;

            prevPos = pos;
        }

        public void remove() {
            GunGamePlugin.plugin.getPhysicsSystem().removeParticle(this);
        }

        @Override
        public boolean resolveCollision(Vector3D normal) {
            double explodeSlope = Math.cos(Math.toRadians(30.0f));

            if (normal.dotProduct(Vector3D.PLUS_J) >= explodeSlope) {
                state = new ExplodeState();
                return true;
            }
            return super.resolveCollision(normal);
        }
    }
}
