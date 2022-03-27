package com.plushnode.gungame.weapons;

import com.plushnode.gungame.DamageTracker;
import com.plushnode.gungame.GunGamePlugin;
import com.plushnode.gungame.Trigger;
import com.plushnode.gungame.UpdateResult;
import com.plushnode.gungame.physics.PhysicsSystem;
import com.plushnode.gungame.util.WorldUtil;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;

public class Flamethrower implements Weapon {
    private static final Vector SPAWN_ACCELERATION = new Vector(0, 8, 0);
    private static final float SPAWN_PUSH_MIN = 20.0f;
    private static final float SPAWN_PUSH_MAX = 30.0f;
    private static final int SPAWN_AMOUNT_MIN = 12;
    private static final int SPAWN_AMOUNT_MAX = 24;
    private static final double SPAWN_DENSITY = 1.3;
    private static final double SPAWN_DRAG = 0.9;
    private static final float RENDER_SPREAD = 0.2f;

    private Player player;

    private List<SomethingParticle> particles = new ArrayList<>();
    private Map<BlockVector, DensityInformation> densityMap = new HashMap<>();

    //private Renderer renderer = new PerParticleRenderer();
    private Renderer renderer = new DensityRenderer();
    private double maxDensity;
    private long lastSoundTime;
    private int slot;
    private Map<Entity, Long> damageTimers = new HashMap<>();

    @Override
    public boolean activate(Player player, Trigger trigger) {
        if (trigger != Trigger.Sneak) return false;

        this.player = player;
        this.slot = player.getInventory().getHeldItemSlot();

        return true;
    }

    @Override
    public UpdateResult update() {
        if (!player.isSneaking()) return UpdateResult.Remove;
        if (player.getInventory().getHeldItemSlot() != slot) return UpdateResult.Remove;
        if (player.getGameMode() == GameMode.SPECTATOR) return UpdateResult.Remove;

        long time = System.currentTimeMillis();

        if (time - lastSoundTime >= 1000) {
            player.getWorld().playSound(player.getEyeLocation(), Sound.BLOCK_FIRE_AMBIENT, 2f, 1.0f);
            lastSoundTime = time;
        }

        densityMap.clear();

        spawnParticles();
        integrate();

        // TODO: Could calculate nearby range from density map
        Collection<Entity> nearbyEntities = WorldUtil.getEntitiesAroundPoint(player.getLocation(), 60);

        for (Map.Entry<BlockVector, DensityInformation> entry : densityMap.entrySet()) {
            DensityInformation info = entry.getValue();

            if (info.count <= 0) continue;

            Location location = info.averageLocation.clone().multiply(1.0f / info.count);
            BoundingBox collider = BoundingBox.of(location.getBlock());

            for (Entity entity : nearbyEntities) {
                if (entity == player) continue;

                if (entity.getBoundingBox().overlaps(collider)) {
                    double percent = info.density / maxDensity;

                    int ticks = 20 + (int)(percent * 80);
                    if (ticks > 80) ticks = 80;

                    if (entity.getFireTicks() < ticks) {
                        entity.setFireTicks(ticks);
                    }

                    Long lastDamageTime = damageTimers.get(entity);

                    if (lastDamageTime == null || time - lastDamageTime >= 1000) {
                        ((LivingEntity) entity).setNoDamageTicks(0);

                        GunGamePlugin.plugin.getDamageTracker().applyDamage(entity, new DamageTracker.DamageEvent(this, 5.0, false));

                        damageTimers.put(entity, time);
                    }
                }
            }
        }

        renderer.render();
        return UpdateResult.Continue;
    }

    private void integrate() {
        double dt = PhysicsSystem.TIMESTEP;

        this.maxDensity = 0;

        // Create density map by summing particle densities in each block
        for (SomethingParticle particle : particles) {
            BlockVector blockVector = particle.location.toVector().toBlockVector();

            DensityInformation info = densityMap.get(blockVector);

            if (info == null) {
                info = new DensityInformation(particle);
            } else {
                info.count++;
                info.density += particle.density;
                info.averageLocation.add(particle.location);
            }

            if (info.density > maxDensity) {
                maxDensity = info.density;
            }

            densityMap.put(blockVector, info);
        }

        for (Iterator<SomethingParticle> iterator = particles.iterator(); iterator.hasNext();) {
            SomethingParticle particle = iterator.next();

            Vector dispersion = new Vector(0, 0, 0);

            Location above = particle.location.getBlock().getLocation().add(0.5, 1.5, 0.5);
            Location below = particle.location.getBlock().getLocation().add(0.5, -0.5, 0.5);
            Location forward = particle.location.getBlock().getLocation().add(0.5, 0.5, 1.5);
            Location behind = particle.location.getBlock().getLocation().add(0.5, 0.5, -0.5);
            Location left = particle.location.getBlock().getLocation().add(-0.5, 0.5, 0.5);
            Location right = particle.location.getBlock().getLocation().add(1.5, 0.5, 0.5);

            // Compute gradients for the dispersion
            double dCenter = getDensity(particle.location);
            double dAbove = getDensity(above);
            double dBelow = getDensity(below);
            double dForward = getDensity(forward);
            double dBehind = getDensity(behind);
            double dLeft = getDensity(left);
            double dRight = getDensity(right);

            double bX = particle.location.getBlockX();
            double bY = particle.location.getBlockY();
            double bZ = particle.location.getBlockZ();

            double kDA = (particle.location.getY() - bY);
            double kDBelow = 1.0 - (particle.location.getY() - bY);
            double kDF = (particle.location.getZ() - bZ);
            double kDBehind = 1.0 - (particle.location.getZ() - bZ);
            double kDL = 1.0 - (particle.location.getX() - bX);
            double kDR = (particle.location.getX() - bX);

            dispersion.add(new Vector(0, 1, 0).multiply(kDA * (dCenter - dAbove)));
            dispersion.add(new Vector(0, -1, 0).multiply(kDBelow * (dCenter - dBelow)));
            dispersion.add(new Vector(0, 0, 1).multiply(kDF * (dCenter - dForward)));
            dispersion.add(new Vector(0, 0, -1).multiply(kDBehind * (dCenter - dBehind)));
            dispersion.add(new Vector(-1, 0, 0).multiply(kDL * (dCenter - dLeft)));
            dispersion.add(new Vector(1, 0, 0).multiply(kDR * (dCenter - dRight)));

            // How fast the particles can disperse
            double kD = 20;

            if (dispersion.lengthSquared() > kD * kD) {
                dispersion.normalize().multiply(kD);
            }

            Vector acceleration = particle.acceleration.clone();
            acceleration.add(dispersion);

            acceleration.multiply(dt);

            Location pLocation = particle.location.clone();

            double oldY = particle.location.getY();

            particle.velocity.add(acceleration);
            particle.velocity.multiply(particle.drag);
            particle.location.add(particle.velocity.clone().multiply(dt));

            // If the particle changes blocks, ray cast to make sure it's allowed
            if (!pLocation.getBlock().equals(particle.location.getBlock())) {
                Vector velocity = particle.velocity.clone().multiply(dt);

                // Perform some basic collision resolution
                for (int i = 0; i < 10; ++i) {
                    Vector dp = velocity.clone();
                    Vector direction = velocity.clone().normalize();
                    double range = dp.length();

                    if (range <= 0) break;

                    RayTraceResult result = pLocation.getWorld().rayTraceBlocks(pLocation, direction, range, FluidCollisionMode.NEVER, true);

                    if (result != null) {
                        Vector normal = result.getHitBlockFace().getDirection();

                        // Push the particle back out of the block
                        Vector adjustment = normal.clone().multiply(dp.dot(normal));

                        velocity.subtract(adjustment);
                        particle.location.subtract(adjustment);
                    } else {
                        break;
                    }
                }
            }

            if (--particle.aliveTicks <= 0 || particle.location.distanceSquared(player.getEyeLocation()) > 100 * 100) {
                iterator.remove();
            }
        }
    }

    private double getDensity(Location location) {
        if (!location.getBlock().isPassable()) {
            return 55;
        }

        BlockVector blockVector = location.toVector().toBlockVector();

        DensityInformation info = densityMap.get(blockVector);

        if (info == null) {
            return 0;
        }

        return info.density;
    }

    private void spawnParticles() {
        Random random = new Random();

        Vector worldUp = new Vector(0, 1, 0);
        Vector lookingDir = player.getEyeLocation().getDirection();
        Vector right = lookingDir.clone().crossProduct(worldUp).normalize();
        Vector up = right.clone().crossProduct(lookingDir);

        Location spawnLocation = player.getEyeLocation().clone().add(player.getEyeLocation().getDirection());

        if (!spawnLocation.getBlock().isPassable()) {
            spawnLocation = player.getEyeLocation().clone();
        }

        double spawnRadius = 0.3;

        int spawnAmount = SPAWN_AMOUNT_MIN + random.nextInt(SPAWN_AMOUNT_MAX - SPAWN_AMOUNT_MIN);
        for (int i = 0; i < spawnAmount; ++i) {
            SomethingParticle particle = new SomethingParticle();

            particle.location = spawnLocation.clone();

            // Generate a random spawn point in the player's face plane
            particle.location.add(right.clone().multiply(getRandomOffset(random) * spawnRadius));
            particle.location.add(up.clone().multiply(getRandomOffset(random) * spawnRadius));
            particle.renderParticle = Particle.REDSTONE;
            particle.acceleration = SPAWN_ACCELERATION.clone();
            particle.density = SPAWN_DENSITY;
            particle.drag = SPAWN_DRAG;
            particle.aliveTicks = 15 + random.nextInt(10);
            particle.renderColor = Color.fromRGB(255, 0, 0);

            float push_range = SPAWN_PUSH_MAX - SPAWN_PUSH_MIN;
            particle.velocity = player.getEyeLocation().getDirection().clone().multiply(SPAWN_PUSH_MIN + random.nextDouble() * push_range);

            particles.add(particle);
        }
    }

    private double getRandomOffset(Random random) {
        return (random.nextDouble() - random.nextDouble()) * 0.5;
    }

    @Override
    public void destroy() {

    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

    @Override
    public String getName() {
        return "Flamethrower";
    }

    private static class SomethingParticle {
        Vector acceleration;
        Vector velocity;
        Location location;
        long aliveTicks;
        double density;
        double drag;
        Particle renderParticle;
        Color renderColor;
        Material renderMaterial;
    }

    private static class DensityInformation {
        Location averageLocation;
        double density;
        int count;

        DensityInformation(SomethingParticle particle) {
            this.count = 1;
            this.density = particle.density;
            this.averageLocation = particle.location.clone();
        }
    }

    private interface Renderer {
        void render();
    }

    private class PerParticleRenderer implements Renderer {
        @Override
        public void render() {
            for (SomethingParticle particle : particles) {
                Location render = particle.location;

                if (particle.renderParticle == Particle.BLOCK_CRACK) {
                    render.getWorld().spawnParticle(particle.renderParticle, render, 1, 0.0f, 0.0f, 0.0f, 0.0f, particle.renderMaterial.createBlockData(), true);
                } else if (particle.renderParticle == Particle.REDSTONE) {
                    Particle.DustOptions dustOptions = new Particle.DustOptions(particle.renderColor, 1.0f);
                    render.getWorld().spawnParticle(particle.renderParticle, render, 1, 0.0f, 0.0f, 0.0f, 0.0f, dustOptions, true);
                } else {
                    render.getWorld().spawnParticle(particle.renderParticle, render, 1, 0.0f, 0.0f, 0.0f, 0.0f, null, true);
                }
            }
        }
    }

    private class RandomSampleRenderer implements Renderer {
        private Random random = new Random();
        private int samples;
        private int particalAmount;

        public RandomSampleRenderer(int samples, int particleAmount) {
            this.samples = samples;
            this.particalAmount = particleAmount;
        }

        @Override
        public void render() {
            for (int i = 0; i < samples; ++i) {
                SomethingParticle particle = particles.get(random.nextInt(particles.size()));
                Location render = particle.location;
                render.getWorld().spawnParticle(particle.renderParticle, render, particalAmount, RENDER_SPREAD, RENDER_SPREAD, RENDER_SPREAD, 0.001f, null, true);
            }
        }
    }

    private class DensityRenderer implements Renderer {
        @Override
        public void render() {
            RealDistribution distribution = new NormalDistribution(0.0f, 30.0f);

            for (Map.Entry<BlockVector, DensityInformation> entry : densityMap.entrySet()) {
                DensityInformation info = entry.getValue();
                double density = info.density;

                //int amount = (int) Math.min(Math.max((density / 20) * maxDensity, 1), maxDensity);
                int amount = (int) Math.min((density / 20) * maxDensity, maxDensity);

                if (amount >= 6) {
                    amount = 4;

                    Location render = info.averageLocation.clone().multiply(1.0f / info.count);

                    int sample = (int)Math.abs(distribution.sample()) % 40;

                    float hue = sample / 360.0f;
                    int rgb = java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f);
                    java.awt.Color color = new java.awt.Color(rgb);
                    Color dustColor = Color.fromRGB(color.getRed(), color.getGreen(), color.getBlue());

                    Particle.DustOptions dustOptions = new Particle.DustOptions(dustColor, 1.0f);

                    render.getWorld().spawnParticle(Particle.REDSTONE, render, amount, RENDER_SPREAD, RENDER_SPREAD, RENDER_SPREAD, 0.001f, dustOptions, true);
                }
            }
        }
    }

    private class KMeansClusterRenderer implements Renderer {
        private int k;
        private int particleAmount;

        KMeansClusterRenderer(int k, int particleAmount) {
            this.k = k;
            this.particleAmount = particleAmount;
        }

        @Override
        public void render() {
            Random random = new Random();
            List<Location> centroids = new ArrayList<>();
            List<List<SomethingParticle>> clusters = new ArrayList<>();

            if (particles.isEmpty()) return;

            // Initialize the clustering with random particles
            for (int i = 0; i < k; ++i) {
                SomethingParticle particle = particles.get(random.nextInt(particles.size()));

                centroids.add(particle.location.clone());
                clusters.add(new ArrayList<>());
            }

            // Initial assignment to the nearest cluster
            for (SomethingParticle particle : particles) {
                int bestIndex = getBestCluster(centroids, particle.location);
                clusters.get(bestIndex).add(particle);
            }

            boolean finished = false;
            int attempts = 0;
            while (!finished && attempts < 100) {
                finished = true;

                // Compute new centroids
                for (int i = 0; i < k; ++i) {
                    Location centroid = centroids.get(i);

                    centroid.setX(0);
                    centroid.setY(0);
                    centroid.setZ(0);

                    for (SomethingParticle particle : clusters.get(i)) {
                        centroid = centroid.add(particle.location);
                    }

                    if (!clusters.get(i).isEmpty()) {
                        centroid = centroid.multiply(1.0 / clusters.get(i).size());
                    }

                    centroids.set(i, centroid);
                }

                List<List<SomethingParticle>> newClusters = new ArrayList<>();

                for (int i = 0; i < k; ++i) {
                    newClusters.add(new ArrayList<>());
                }

                // Reassign observations to nearest cluster
                for (int i = 0; i < k; ++i) {
                    for (SomethingParticle particle : clusters.get(i)) {
                        int bestIndex = getBestCluster(centroids, particle.location);

                        if (bestIndex != i) {
                            finished = false;
                        }

                        newClusters.get(bestIndex).add(particle);
                    }
                }

                clusters = newClusters;

                ++attempts;
            }

            for (int i = 0; i < k; ++i) {
                Location location = centroids.get(i);

                location.getWorld().spawnParticle(Particle.ASH, location, particleAmount, RENDER_SPREAD, RENDER_SPREAD, RENDER_SPREAD, 0.001f, null, true);
            }
        }

        private int getBestCluster(List<Location> centroids, Location location) {
            int bestIndex = 0;
            double bestDistanceSq = Double.MAX_VALUE;

            for (int i = 0; i < centroids.size(); ++i) {
                double distanceSq = centroids.get(i).distanceSquared(location);

                if (distanceSq < bestDistanceSq) {
                    bestIndex = i;
                    bestDistanceSq = distanceSq;
                }
            }

            return bestIndex;
        }
    }
}
