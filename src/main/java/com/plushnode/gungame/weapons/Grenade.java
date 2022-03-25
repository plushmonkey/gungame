package com.plushnode.gungame.weapons;

import com.plushnode.gungame.GunGamePlugin;
import com.plushnode.gungame.GunPlayer;
import com.plushnode.gungame.Trigger;
import com.plushnode.gungame.UpdateResult;
import com.plushnode.gungame.physics.Particle;
import com.plushnode.gungame.util.VectorUtil;
import com.plushnode.gungame.util.WorldUtil;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Grenade implements Weapon {
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

            for (Grenade instance : GunGamePlugin.plugin.getInstanceManager().getPlayerInstances(player, Grenade.class)) {
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
        return "Grenade";
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
            Vector3D position = VectorUtil.adapt(player.getEyeLocation().toVector());
            GrenadeParticle particle = new GrenadeParticle(position, 1.0);

            double force = 50.0 + (750.0 * strength);

            particle.addForce(direction.scalarMultiply(force));
            particle.setAcceleration(new Vector3D(0.0, -20.0, 0.0));
            particle.setDamping(0.8);
            particle.setRestitution(0.4);

            particles.add(particle);

            GunPlayer gunPlayer = GunGamePlugin.plugin.getPlayerManager().getPlayer(player);

            if (gunPlayer != null) {
                gunPlayer.addCooldown(getName(), 5000);
            }
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
        ExplodeState() {
            float size = 5.0f;
            double damage = 50.0;

            for (GrenadeParticle particle : particles) {
                Location location = new Location(world, particle.getPosition().getX(), particle.getPosition().getY(), particle.getPosition().getZ());

                world.createExplosion(location, 0.0f);
                render(location);

                for (Entity e : WorldUtil.getEntitiesAroundPoint(location, size)) {
                    if (e instanceof LivingEntity) {
                        ((LivingEntity) e).damage(damage);
                    }
                }
            }
        }

        private void render(Location location) {
            world.spawnParticle(org.bukkit.Particle.FLAME, location, 20, (float) Math.random(), (float) Math.random(), (float) Math.random(), 0.5f, null, true);
            world.spawnParticle(org.bukkit.Particle.SMOKE_LARGE, location, 20, (float) Math.random(), (float) Math.random(), (float) Math.random(), 0.5f, null, true);
            world.spawnParticle(org.bukkit.Particle.FIREWORKS_SPARK, location, 20, (float) Math.random(), (float) Math.random(), (float) Math.random(), 0.5f, null, true);
            world.spawnParticle(org.bukkit.Particle.EXPLOSION_HUGE, location, 5, (float) Math.random(), (float) Math.random(), (float) Math.random(), 0.5f, null, true);

            world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);
        }

        @Override
        public boolean update() {
            return false;
        }

        @Override
        public boolean onPunch() {
            return false;
        }
    }

    private class GrenadeParticle extends Particle {
        Vector3D prevPos;

        public GrenadeParticle(Vector3D position, double mass) {
            super(position, mass);

            this.prevPos = getPosition();

            GunGamePlugin.plugin.getPhysicsSystem().addParticle(this, player.getWorld());
        }

        public void render() {
            Vector3D pos = getPosition();
            Location location = new Location(player.getWorld(), pos.getX(), pos.getY(), pos.getZ());
            Location prevLocation = new Location(player.getWorld(), prevPos.getX(), prevPos.getY(), prevPos.getZ());

            Vector travel = location.clone().subtract(prevLocation).toVector();

            int red = 90;
            int green = 78;
            int blue = 36;

            Color color = Color.fromRGB(red, green, blue);

            double increment = 0.3;

            if (prevLocation.distanceSquared(location) < 0.25 * 0.25) {
                increment = 1.0;
            }

            for (double t = 0; t < 1.0; t += increment) {
                // Interpolate between the two positions to render multiple times.
                Location renderLocation = prevLocation.clone().add(travel.clone().multiply(t));

                player.getWorld().spawnParticle(org.bukkit.Particle.REDSTONE,
                        renderLocation, 0, red, green, blue, 0.005f, new org.bukkit.Particle.DustOptions(color, 1.0f),
                        true);
            }

            prevPos = pos;
        }

        public void remove() {
            GunGamePlugin.plugin.getPhysicsSystem().removeParticle(this);
        }
    }
}
