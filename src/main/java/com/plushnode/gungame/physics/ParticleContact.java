package com.plushnode.gungame.physics;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class ParticleContact {
    private Particle[] particles = new Particle[2];
    private double restitution;
    private Vector3D contactNormal;

    public ParticleContact(Particle first, Particle second, double restitution, Vector3D normal) {
        this.particles[0] = first;
        this.particles[1] = second;
        this.restitution = restitution;
        this.contactNormal = normal;
    }

    public Particle[] getParticles() {
        return particles;
    }

    public void setParticle(Particle particle, int index) {
        this.particles[index] = particle;
    }

    public double getRestitution() {
        return restitution;
    }

    public void setRestitution(double restitution) {
        this.restitution = restitution;
    }

    public Vector3D getContactNormal() {
        return contactNormal;
    }

    public void setContactNormal(Vector3D contactNormal) {
        this.contactNormal = contactNormal;
    }

    public void resolve(double dt) {
        double separatingVelocity = calculateSeparatingVelocity();

        if (separatingVelocity > 0.0) {
            return;
        }

        double newSeparatingVelocity = -separatingVelocity * restitution;

        Vector3D accCausedVelocity = particles[0].getAcceleration();
        if (particles[1] != null) {
            accCausedVelocity = accCausedVelocity.subtract(particles[1].getAcceleration());
        }

        double accCausedSepVelocity = accCausedVelocity.dotProduct(contactNormal.scalarMultiply(dt));

        if (accCausedSepVelocity < 0) {
            newSeparatingVelocity += restitution * accCausedSepVelocity;
            if (newSeparatingVelocity < 0) {
                newSeparatingVelocity = 0;
            }
        }

        double deltaVelocity = newSeparatingVelocity - separatingVelocity;
        double totalInverseMass = particles[0].getInverseMass();

        if (particles[1] != null) {
            totalInverseMass += particles[1].getInverseMass();
        }

        // Both objects are immovable.
        if (totalInverseMass <= 0.0) {
            return;
        }

        double impulse = deltaVelocity / totalInverseMass;
        Vector3D impulsePerInvMass = contactNormal.scalarMultiply(impulse);

        particles[0].setVelocity(particles[0].getVelocity().add(impulsePerInvMass.scalarMultiply(particles[0].getInverseMass())));

        if (particles[1] != null) {
            particles[1].setVelocity(particles[1].getVelocity().add(impulsePerInvMass.scalarMultiply(particles[1].getInverseMass())));
        }
    }

    private double calculateSeparatingVelocity() {
        Vector3D relativeVelocity = particles[0].getVelocity();

        if (particles[1] != null) {
            relativeVelocity = relativeVelocity.subtract(particles[1].getVelocity());
        }

        return relativeVelocity.dotProduct(contactNormal);
    }
}
