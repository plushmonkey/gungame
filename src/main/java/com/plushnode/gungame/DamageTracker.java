package com.plushnode.gungame;

import com.plushnode.gungame.weapons.Weapon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashMap;
import java.util.Map;

public class DamageTracker {
    private Map<Player, DamageEvent> currentDamage = new HashMap<>();

    public void applyDamage(Entity entity, DamageEvent event) {
        if (entity instanceof Player) {
            currentDamage.put((Player)entity, event);
        }

        ((LivingEntity)entity).damage(event.damage, event.weapon.getPlayer());

        if (entity instanceof Player) {
            currentDamage.remove(entity);
        }
    }

    public void clearPlayer(Player player) {
        currentDamage.remove(player);
    }

    public void handleDeath(PlayerDeathEvent event) {
        DamageEvent damageEvent = currentDamage.get(event.getEntity());

        if (damageEvent == null) {
            return;
        }

        String name = event.getEntity().getName();
        String weaponName = damageEvent.weapon.getName();
        String shooter = damageEvent.weapon.getPlayer().getName();

        String headshotIndicator = "";

        if (damageEvent.headshot) {
            headshotIndicator = "*";
        }

        String message = name + " was slain by " + shooter + " using [" + weaponName + headshotIndicator + "]";
        event.setDeathMessage(message);
    }

    public static class DamageEvent {
        Weapon weapon;
        double damage;
        boolean headshot;

        public DamageEvent(Weapon weapon, double damage, boolean headshot) {
            this.weapon = weapon;
            this.damage = damage;
            this.headshot = headshot;
        }
    }
}
