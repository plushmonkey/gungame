package com.plushnode.gungame.physics;

import com.plushnode.gungame.collision.Ray;
import com.plushnode.gungame.collision.RayCaster;
import com.plushnode.gungame.util.VectorUtil;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.bukkit.World;

import java.util.Collections;

// Detect and resolve particle-world contacts
public class BasicCollisionDetector {
    public void update(Particle particle, Vector3D prevPos, World world, double restitution) {
        Vector3D movement = particle.getPosition().subtract(prevPos);
        if (movement.getNormSq() <= 0.0) return;

        Ray ray = new Ray(prevPos, movement.normalize());

        double maxDistance = movement.getNorm();
        RayCaster.CastResult result = RayCaster.cast(world, ray, maxDistance, false, Collections.emptyList());

        if (result.hit) {
            Vector3D hitPosition = VectorUtil.adapt(result.location.toVector());

            particle.setPosition(hitPosition);

            ParticleContact contact = new ParticleContact(particle, null, restitution, result.normal);
            contact.resolve(PhysicsSystem.TIMESTEP);
        }
    }
}
