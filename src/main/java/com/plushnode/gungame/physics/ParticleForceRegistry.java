package com.plushnode.gungame.physics;

import java.util.ArrayList;
import java.util.List;

public class ParticleForceRegistry {
    private List<Registration> registrations = new ArrayList<>();

    public void add(Particle particle, ParticleForceGenerator generator) {
        Registration reg = new Registration();
        reg.particle = particle;
        reg.generator = generator;

        registrations.add(reg);
    }

    public void remove(Particle particle, ParticleForceGenerator generator) {
        registrations.removeIf((registration) ->
                registration.particle == particle && registration.generator == generator);
    }

    public void clear() {
        registrations.clear();
    }

    public void clear(Particle particle) {
        registrations.removeIf((registration) -> registration.particle == particle);
    }

    public void updateForces(double dt) {
        for (Registration registration : registrations) {
            registration.generator.updateForce(registration.particle, dt);
        }
    }

    private class Registration {
        Particle particle;
        ParticleForceGenerator generator;
    }
}
