package com.plushnode.gungame.physics;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class Particle {
    private Vector3D position = Vector3D.ZERO;
    private Vector3D velocity = Vector3D.ZERO;
    private Vector3D acceleration = Vector3D.ZERO;
    private Vector3D forceAccumulator = Vector3D.ZERO;
    private double mass = 0.0;
    private double inverseMass = 0.0;
    private double damping = 0.97;
    private double restitution = 0.6;

    public Particle(double mass) {
        setMass(mass);
    }

    public Particle(Vector3D position, double mass) {
        this.position = position;
        setMass(mass);
    }

    public Particle(Vector3D position, Vector3D velocity, double mass) {
        this.position = position;
        this.velocity = velocity;
        setMass(mass);
    }

    public Particle(Vector3D position, Vector3D velocity, Vector3D acceleration, double mass) {
        this.position = position;
        this.velocity = velocity;
        this.acceleration = acceleration;
        setMass(mass);
    }

    public Vector3D getPosition() {
        return position;
    }

    public void setPosition(Vector3D position) {
        this.position = position;
    }

    public Vector3D getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector3D velocity) {
        this.velocity = velocity;
    }

    public Vector3D getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(Vector3D acceleration) {
        this.acceleration = acceleration;
    }

    public void setMass(double mass) {
        if (mass == 0.0) {
            this.inverseMass = 0.0;
        } else {
            this.inverseMass = 1.0 / mass;
        }

        this.mass = mass;
    }

    public double getMass() {
        return mass;
    }

    public double getInverseMass() {
        return inverseMass;
    }

    public void setInverseMass(double inverseMass) {
        this.inverseMass = inverseMass;
    }

    public double getDamping() {
        return damping;
    }

    public void setDamping(double damping) {
        this.damping = damping;
    }

    public void addForce(Vector3D force) {
        this.forceAccumulator = this.forceAccumulator.add(force);
    }

    public Vector3D getForceAccumulator() {
        return this.forceAccumulator;
    }

    public void clearForceAccumulation() {
        this.forceAccumulator = Vector3D.ZERO;
    }

    public double getRestitution() {
        return restitution;
    }

    public void setRestitution(double restitution) {
        this.restitution = restitution;
    }
}
