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
    private int zoomAmount;
    private boolean nightVision;
    private long lastZoomTime;
    private long lastNightVisionTime;

    public ScopeAttachment(int zoomAmount, boolean nightVision) {
        this.zoomAmount = zoomAmount;
        this.nightVision = nightVision;
    }

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

    private void applyZoom() {
        long time = System.currentTimeMillis();

        if (time > lastZoomTime + 500) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000, -zoomAmount, true));
            lastZoomTime = time;
        }
    }

    private boolean needsZoom() {
        if (!player.hasPotionEffect(PotionEffectType.SPEED)) return true;

        PotionEffect current = player.getPotionEffect(PotionEffectType.SPEED);
        return current == null || current.getAmplifier() != -zoomAmount;
    }

    private void applyNightVision() {
        long time = System.currentTimeMillis();

        if (time > lastNightVisionTime + 500) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 100000, 1, true));
            lastNightVisionTime = time;
        }
    }

    private boolean needsNightVision() {
        return this.nightVision && !player.hasPotionEffect(PotionEffectType.NIGHT_VISION);
    }

    @Override
    public UpdateResult update() {
        if (!player.isOnline() || player.isDead() || player.getInventory().getHeldItemSlot() != this.slot) {
            return UpdateResult.Remove;
        }

        if (player.getGameMode() == GameMode.SPECTATOR) {
            return UpdateResult.Remove;
        }


        if (needsZoom()) {
            applyZoom();
        }

        if (needsNightVision()) {
            applyNightVision();
        }

        return UpdateResult.Continue;
    }

    @Override
    public void destroy() {
        player.removePotionEffect(PotionEffectType.SPEED);

        if (this.nightVision) {
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        }
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
