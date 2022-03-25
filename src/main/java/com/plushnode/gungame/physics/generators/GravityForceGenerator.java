package com.plushnode.gungame.physics.generators;

import com.plushnode.gungame.physics.Particle;
import com.plushnode.gungame.physics.ParticleForceGenerator;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class GravityForceGenerator implements ParticleForceGenerator {
    private Vector3D gravity;

    public GravityForceGenerator(Vector3D gravity) {
        this.gravity = gravity;
    }

    @Override
    public void updateForce(Particle particle, double dt) {
        if (particle.getInverseMass() == 0.0) return;

        particle.addForce(gravity.scalarMultiply(particle.getMass()));
    }
}
