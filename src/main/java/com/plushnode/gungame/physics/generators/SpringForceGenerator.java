package com.plushnode.gungame.physics.generators;

import com.plushnode.gungame.physics.Particle;
import com.plushnode.gungame.physics.ParticleForceGenerator;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class SpringForceGenerator implements ParticleForceGenerator {
    private Particle other;
    private double stiffness;
    private double restLength;
    private boolean bungee;

    public SpringForceGenerator(Particle other, double stiffness, double restLength, boolean bungee) {
        this.other = other;
        this.stiffness = stiffness;
        this.restLength = restLength;
        this.bungee = bungee;
    }

    @Override
    public void updateForce(Particle particle, double dt) {
        Vector3D toParticle = particle.getPosition().subtract(other.getPosition());
        double displacement = toParticle.getNorm() - restLength;

        if (toParticle.getNormSq() > 0.0 && (!bungee || displacement > 0.0)) {
            Vector3D force = toParticle.normalize().scalarMultiply(-stiffness * displacement);
            particle.addForce(force);
        }
    }
}
