package com.plushnode.gungame.attachments;

import com.plushnode.gungame.GunGamePlugin;
import com.plushnode.gungame.Trigger;
import com.plushnode.gungame.UpdateResult;
import com.plushnode.gungame.weapons.Weapon;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ScopeAttachment implements Weapon {
    private Player player;
    private int slot;
    private int zoomAmount = 5;

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

    @Override
    public UpdateResult update() {
        if (!player.isOnline() || player.isDead() || player.getInventory().getHeldItemSlot() != this.slot) {
            return UpdateResult.Remove;
        }

        if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000, -zoomAmount, true));
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
