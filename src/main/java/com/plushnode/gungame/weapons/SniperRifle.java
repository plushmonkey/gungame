package com.plushnode.gungame.weapons;

import com.plushnode.gungame.GunGamePlugin;
import com.plushnode.gungame.GunPlayer;
import com.plushnode.gungame.Trigger;
import com.plushnode.gungame.util.PlayerUtil;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

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
            config.speed = 12.0;
            config.range = 30.0;
            config.damage = 12.0;
            config.headshotDamage = 24.0;
            config.swimmingDamage = 16.0;
            config.clearNoTicks = true;

            bullets.add(new Bullet(this, config, player, PlayerUtil.getEye(player), player.getEyeLocation().getDirection()));

            gunPlayer.addCooldown(getName(), 2500);

            return true;
        }

        return false;
    }

    @Override
    public String getName() {
        return "Sniper";
    }
}
