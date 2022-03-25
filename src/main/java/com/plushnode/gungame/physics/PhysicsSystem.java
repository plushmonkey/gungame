package com.plushnode.gungame.physics;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhysicsSystem {
    public static final double TIMESTEP = 1000.0 / 20 / 1000.0;

    private List<Particle> particles = new ArrayList<>();
    private Map<Particle, World> particleWorlds = new HashMap<>();
    private ParticleIntegrator integrator = new EulerParticleIntegrator();
    private ParticleForceRegistry forceRegistry = new ParticleForceRegistry();
    private BasicCollisionDetector collisionDetector = new BasicCollisionDetector();

    public void update() {
        List<Vector3D> previous = new ArrayList<>();

        for (Particle particle : particles) {
            previous.add(particle.getPosition());
        }

        forceRegistry.updateForces(TIMESTEP);

        for (int i = 0; i < particles.size(); ++i) {
            Particle particle = particles.get(i);
            Vector3D prevPos = previous.get(i);
            integrator.integrate(particle, TIMESTEP);
            World world = particleWorlds.get(particle);
            if (world != null) {
                collisionDetector.update(particle, prevPos, world, particle.getRestitution());
            }
        }
    }

    public void addParticle(Particle particle, World world) {
        this.particles.add(particle);
        particleWorlds.put(particle, world);
    }

    public void removeParticle(Particle particle) {
        this.particles.remove(particle);
        this.particleWorlds.remove(particle);

        forceRegistry.clear(particle);
    }

    public int getSize() {
        return particles.size();
    }

    public ParticleForceRegistry getForceRegistry() {
        return forceRegistry;
    }
}
