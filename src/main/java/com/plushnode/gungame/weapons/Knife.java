package com.plushnode.gungame.weapons;

import com.plushnode.gungame.DamageTracker;
import com.plushnode.gungame.GunGamePlugin;
import com.plushnode.gungame.Trigger;
import com.plushnode.gungame.UpdateResult;
import com.plushnode.gungame.collision.Ray;
import com.plushnode.gungame.listeners.PlayerListener;
import com.plushnode.gungame.util.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class Knife implements Weapon {
    private static final float ACTIVATION_ANGLE = 90.0f;
    private static final int SPEED_AMPLIFIER = 1;
    private World world;
    private Player player;
    private int slot;
    private long lastSpeedTime;

    @Override
    public boolean activate(Player player, Trigger trigger) {
        if (trigger == Trigger.HotbarSwap) {
            this.player = player;
            this.world = player.getWorld();
            this.slot = PlayerListener.getPlayerSlot(player);
            applySpeed();
            return true;
        }

        return false;
    }

    @Override
    public UpdateResult update() {
        if (player.getInventory().getHeldItemSlot() != slot) return UpdateResult.Remove;
        if (!(player.getWorld().equals(world))) return UpdateResult.Remove;
        if (!player.isOnline()) return UpdateResult.Remove;
        if (player.getInventory().getItemInMainHand().getType() != Material.IRON_SWORD) return UpdateResult.Remove;

        if (!player.isDead() && needsSpeed()) {
            applySpeed();
        }

        return UpdateResult.Continue;
    }

    private void applySpeed() {
        long time = System.currentTimeMillis();

        if (time > lastSpeedTime + 500) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 500, SPEED_AMPLIFIER, true));
            lastSpeedTime = time;
        }
    }

    private boolean needsSpeed() {
        if (!player.hasPotionEffect(PotionEffectType.SPEED)) return true;

        PotionEffect current = player.getPotionEffect(PotionEffectType.SPEED);
        return current == null || current.getAmplifier() < SPEED_AMPLIFIER || current.getDuration() < 60;
    }

    public void applyDamage(EntityDamageByEntityEvent event) {
        Player damager = (Player)event.getDamager();
        Entity target = event.getEntity();
        double activationAngle = Math.toRadians(ACTIVATION_ANGLE);
        Vector targetDirection = target.getLocation().getDirection().setY(0).normalize();
        Vector toTarget = target.getLocation().toVector().subtract(player.getLocation().toVector()).setY(0).normalize();

        double angle = toTarget.angle(targetDirection);

        Location eye = PlayerUtil.getEye(damager);
        boolean headshot = false;

        if (PlayerUtil.isHeadshot(target, new Ray(eye, eye.getDirection()), 0.1)) {
            headshot = true;
        }

        if (angle <= activationAngle) {
            DamageTracker.DamageEvent damageEvent = new DamageTracker.DamageEvent(this, 100.0f, headshot, true);
            GunGamePlugin.plugin.getDamageTracker().applyEvent(target, damageEvent);
            event.setDamage(100.0f);
        } else {
            float damage = headshot ? 100.0f : 10.0f;

            DamageTracker.DamageEvent damageEvent = new DamageTracker.DamageEvent(this, damage, headshot);
            GunGamePlugin.plugin.getDamageTracker().applyEvent(target, damageEvent);
            event.setDamage(damage);
        }

        if (target instanceof Player) {
            // Clear the knife damage next tick so new knife events can go through.
            Bukkit.getScheduler().runTaskLater(GunGamePlugin.plugin, () -> {
                GunGamePlugin.plugin.getDamageTracker().clearPlayerType((Player)target, Knife.class);
            }, 1);
        }
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
        return "Knife";
    }
}
