package com.plushnode.gungame.weapons;

import com.plushnode.gungame.GunGamePlugin;
import com.plushnode.gungame.GunPlayer;
import com.plushnode.gungame.Trigger;
import com.plushnode.gungame.util.PlayerUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class SniperRifle extends AbstractGun {
    @Override
    public boolean activate(Player player, Trigger trigger) {
        this.player = player;

        if (trigger == Trigger.RightClick) {
            if (player.getGameMode() == GameMode.SPECTATOR) {
                return false;
            }

            GunPlayer gunPlayer = GunGamePlugin.plugin.getPlayerManager().getPlayer(player);
            if (gunPlayer == null || gunPlayer.isOnCooldown(getName())) return false;

            Bullet.Config config = new Bullet.Config();
            config.speed = 20.0;
            config.range = 300.0;
            config.damage = 12.0;
            config.headshotDamage = 24.0;
            config.swimmingDamage = 16.0;
            config.clearNoTicks = true;
            config.radius = 0.2;

            Location location = PlayerUtil.getEye(player);

            bullets.add(new Bullet(this, config, player, location, player.getEyeLocation().getDirection()));

            gunPlayer.addCooldown(getName(), 2500);

            Vector worldUp = new Vector(0, 1, 0);
            Vector right = player.getEyeLocation().getDirection().clone().crossProduct(worldUp).normalize();
            Location smokeLocation = player.getEyeLocation().clone().add(right.clone().multiply(1.5)).add(player.getEyeLocation().getDirection().clone().multiply(1.8));

            location.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, smokeLocation, 7,0.1f, 0.1f, 0.1f, 0.0f, null, true);
            player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_GENERIC_EXPLODE, 6.0f, 1.5f);

            return true;
        }

        return false;
    }

    @Override
    public String getName() {
        return "Sniper";
    }
}
