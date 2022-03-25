package com.plushnode.gungame.physics.generators;

import com.plushnode.gungame.physics.Particle;
import com.plushnode.gungame.physics.ParticleForceGenerator;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class AnchoredSpringForceGenerator implements ParticleForceGenerator {
    private Vector3D anchor;
    private double stiffness;
    private double restLength;

    public AnchoredSpringForceGenerator(Vector3D anchor, double stiffness, double restLength) {
        this.anchor = anchor;
        this.stiffness = stiffness;
        this.restLength = restLength;
    }

    @Override
    public void updateForce(Particle particle, double dt) {
        Vector3D toParticle = particle.getPosition().subtract(anchor);
        double displacement = Math.abs(toParticle.getNorm() - restLength);

        if (toParticle.getNormSq() > 0.0) {
            double magnitude = -stiffness * displacement;

            Vector3D force = toParticle.normalize().scalarMultiply(magnitude);

            particle.addForce(force);
        }
    }

    public void setAnchor(Vector3D anchor) {
        this.anchor = anchor;
    }
}
