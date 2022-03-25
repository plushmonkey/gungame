package com.plushnode.gungame.weapons;

import com.plushnode.gungame.GunGamePlugin;
import com.plushnode.gungame.GunPlayer;
import com.plushnode.gungame.Trigger;
import com.plushnode.gungame.util.VectorUtil;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Shotgun extends AbstractGun {
    @Override
    public boolean activate(Player player, Trigger trigger) {
        if (trigger != Trigger.LeftClick && trigger != Trigger.RightClick) return false;

        GunPlayer gunPlayer = GunGamePlugin.plugin.getPlayerManager().getPlayer(player);
        if (gunPlayer == null || gunPlayer.isOnCooldown(getName())) return false;

        this.player = player;

        Bullet.Config config = new Bullet.Config();
        config.speed = 12.0;
        config.range = 30.0;
        config.damage = 6.0;
        config.headshotDamage = 10.0;
        config.swimmingDamage = 10.0;

        gunPlayer.addCooldown(getName(), 1500);

        Location location = player.getEyeLocation().clone();

        if (this.player.isSneaking()) {
            // getEyeLocation seems to not align correctly with the game camera's view position.
            // Sneak player height becomes 1.5 blocks tall, so game rendered eye becomes 1.5 - 0.2 = 1.3.
            location = player.getLocation().clone().add(0, 1.3, 0);
        }

        RealDistribution distribution = new NormalDistribution(0.0f, 2.5f);
        Vector3D view = VectorUtil.adapt(location.getDirection());
        Vector3D eye = VectorUtil.adapt(location.toVector());
        Vector3D hit = eye.add(view.scalarMultiply(config.range));

        for (int i = 0; i < 12; ++i) {
            Vector3D randomDirection = new Vector3D(distribution.sample(3));
            Vector3D randomHit = hit.add(randomDirection);
            Vector3D direction = randomHit.subtract(eye).normalize();

            bullets.add(new Bullet(config, player, location, VectorUtil.adapt(direction)));
        }

        Vector worldUp = new Vector(0, 1, 0);
        Vector right = player.getEyeLocation().getDirection().clone().crossProduct(worldUp).normalize();
        Location smokeLocation = player.getEyeLocation().clone().add(right.clone().multiply(1.5)).add(player.getEyeLocation().getDirection().clone().multiply(1.8));

        location.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, smokeLocation, 7,0.1f, 0.1f, 0.1f, 0.0f, null, true);
        player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_GENERIC_EXPLODE, 3.5f, 3.5f);

        applyRecoil(0.6f);

        return true;
    }

    @Override
    public String getName() {
        return "Shotgun";
    }
}
