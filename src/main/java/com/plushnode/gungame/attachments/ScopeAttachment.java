package com.plushnode.gungame.attachments;

import com.plushnode.gungame.GunGamePlugin;
import com.plushnode.gungame.Trigger;
import com.plushnode.gungame.UpdateResult;
import com.plushnode.gungame.weapons.Weapon;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ScopeAttachment implements Weapon {
    private Player player;
    private int slot;
    private int zoomAmount = 5;
    private long lastApplicationTime;

    @Override
    public boolean activate(Player player, Trigger trigger) {
        if (trigger != Trigger.LeftClick) return false;

        ScopeAttachment instance = GunGamePlugin.plugin.getInstanceManager().getFirstInstance(player, ScopeAttachment.class);

        if (instance != null) {
            GunGamePlugin.plugin.getInstanceManager().destroyInstance(player, instance);
            return false;
        }

        this.player = player;
        this.slot = player.getInventory().getHeldItemSlot();
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000, -zoomAmount, true));

        return true;
    }

    private void applyEffect() {
        long time = System.currentTimeMillis();

        if (time > lastApplicationTime + 500) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000, -zoomAmount, true));
            lastApplicationTime = time;
        }
    }

    private boolean needsEffect() {
        if (!player.hasPotionEffect(PotionEffectType.SPEED)) return true;

        PotionEffect current = player.getPotionEffect(PotionEffectType.SPEED);
        return current == null || current.getAmplifier() != -zoomAmount;
    }

    @Override
    public UpdateResult update() {
        if (!player.isOnline() || player.isDead() || player.getInventory().getHeldItemSlot() != this.slot) {
            return UpdateResult.Remove;
        }

        if (player.getGameMode() == GameMode.SPECTATOR) {
            return UpdateResult.Remove;
        }


        if (needsEffect()) {
            applyEffect();
        }

        return UpdateResult.Continue;
    }

    @Override
    public void destroy() {
        player.removePotionEffect(PotionEffectType.SPEED);
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

    @Override
    public String getName() {
        return "Scope";
    }
}
