package com.plushnode.gungame.physics.generators;

import com.plushnode.gungame.physics.Particle;
import com.plushnode.gungame.physics.ParticleForceGenerator;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class StickForceGenerator implements ParticleForceGenerator {
    private Particle other;
    private double length;

    public StickForceGenerator(Particle other, double length) {
        this.other = other;
        this.length = length;
    }

    @Override
    public void updateForce(Particle particle, double dt) {
        double currentLength = particle.getPosition().distance(other.getPosition());
        double penetration = (currentLength - length);
        double totalInverseMass = particle.getInverseMass() + other.getInverseMass();

        Vector3D normal = other.getPosition().subtract(particle.getPosition());

        if (normal.getNormSq() > 0) {
            normal = normal.normalize();
        }

        Vector3D resolutionPerInvMass = normal.scalarMultiply(penetration / totalInverseMass);

        Vector3D pos1 = particle.getPosition().add(resolutionPerInvMass.scalarMultiply(particle.getInverseMass()));
        Vector3D pos2 = other.getPosition().subtract(resolutionPerInvMass.scalarMultiply(other.getInverseMass()));

        particle.setPosition(pos1);
        other.setPosition(pos2);
    }
}
