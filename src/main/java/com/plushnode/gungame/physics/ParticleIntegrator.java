package com.plushnode.gungame.physics;

public interface ParticleIntegrator {
    void integrate(Particle particle, double dt);
}
