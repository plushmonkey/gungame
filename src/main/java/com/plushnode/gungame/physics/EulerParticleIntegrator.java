package com.plushnode.gungame.physics;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class EulerParticleIntegrator implements ParticleIntegrator {
    @Override
    public void integrate(Particle particle, double dt) {
        if (particle.getInverseMass() <= 0.0) return;

        Vector3D position = particle.getPosition();
        Vector3D velocity = particle.getVelocity();
        Vector3D forceAccumulation = particle.getForceAccumulator();

        position = position.add(velocity.scalarMultiply(dt));

        Vector3D resultAcceleration = particle.getAcceleration();

        resultAcceleration = resultAcceleration.add(forceAccumulation.scalarMultiply(particle.getInverseMass()));

        velocity = velocity.add(resultAcceleration.scalarMultiply(dt));
        velocity = velocity.scalarMultiply(Math.pow(particle.getDamping(), dt));

        particle.setPosition(position);
        particle.setVelocity(velocity);
        particle.clearForceAccumulation();
    }
}
