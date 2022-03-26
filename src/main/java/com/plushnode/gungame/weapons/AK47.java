package com.plushnode.gungame.weapons;

import com.plushnode.gungame.GunGamePlugin;
import com.plushnode.gungame.Trigger;

import com.plushnode.gungame.UpdateResult;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class AK47 extends AbstractGun {
    private final static long BULLET_DELAY = 125;
    private final static long ACTIVATION_THRESHOLD = 250;

    private long lastActivationTime;
    private long lastBulletTime;

    @Override
    public boolean activate(Player player, Trigger trigger) {
        this.player = player;

        if (trigger == Trigger.RightClick) {
            if (player.getGameMode() == GameMode.SPECTATOR) {
                return false;
            }

            AK47 instance = GunGamePlugin.plugin.getInstanceManager().getFirstInstance(player, AK47.class);

            if (instance != null) {
                instance.lastActivationTime = System.currentTimeMillis();

                return false;
            }

            this.lastActivationTime = System.currentTimeMillis();
            this.player = player;

            return true;
        }

        return false;
    }

    private void fireBullet() {
        Bullet.Config config = new Bullet.Config();

        config.speed = 14.0;
        config.range = 120.0;
        config.damage = 4.0;
        config.headshotDamage = 10.0;
        config.swimmingDamage = 6.0;
        config.clearNoTicks = false;

        bullets.add(new Bullet(this, config, player, getEyeLocation(player), player.getEyeLocation().getDirection()));

        applyRecoil(0.1f);
        this.lastBulletTime = System.currentTimeMillis();

        player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 5.5f);
        Vector worldUp = new Vector(0, 1, 0);
        Vector right = player.getEyeLocation().getDirection().clone().crossProduct(worldUp).normalize();
        Location smokeLocation = player.getEyeLocation().clone().add(right.clone().multiply(1.5)).add(player.getEyeLocation().getDirection().clone().multiply(1.8));

        smokeLocation.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, smokeLocation, 1,0.1f, 0.1f, 0.1f, 0.0f, null, true);
    }

    @Override
    public UpdateResult update() {
        if (!player.isOnline()) return UpdateResult.Remove;
        if (player.getGameMode() == GameMode.SPECTATOR) return UpdateResult.Remove;

        long time = System.currentTimeMillis();

        boolean isActive = time - this.lastActivationTime < ACTIVATION_THRESHOLD;
        if (isActive) {
            // Continue to fire bullets if activated recently.
            if (!player.isDead() && time - this.lastBulletTime >= BULLET_DELAY) {
                fireBullet();
            }
        }

        super.update();

        if (bullets.isEmpty() && !isActive) {
            return UpdateResult.Remove;
        }

        return UpdateResult.Continue;
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public String getName() {
        return "AK47";
    }
}
