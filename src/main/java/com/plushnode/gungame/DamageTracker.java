package com.plushnode.gungame;

import com.plushnode.gungame.weapons.Weapon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashMap;
import java.util.Map;

public class DamageTracker {
    private Map<Player, Weapon> currentDamage = new HashMap<>();

    public void applyDamage(Entity entity, Weapon weapon, double damage) {
        if (entity instanceof Player) {
            currentDamage.put((Player)entity, weapon);
        }

        ((LivingEntity)entity).damage(damage, weapon.getPlayer());

        if (entity instanceof Player) {
            currentDamage.remove(entity);
        }
    }

    public void clearPlayer(Player player) {
        currentDamage.remove(player);
    }

    public void handleDeath(PlayerDeathEvent event) {
        Weapon weapon = currentDamage.get(event.getEntity());

        if (weapon == null) {
            return;
        }

        String name = event.getEntity().getName();
        String weaponName = weapon.getName();
        String shooter = weapon.getPlayer().getName();

        String message = name + " was slain by " + shooter + " using [" + weaponName + "]";
        event.setDeathMessage(message);
    }
}
