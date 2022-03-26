package com.plushnode.gungame.listeners;

import com.plushnode.gungame.GunGamePlugin;
import com.plushnode.gungame.Trigger;
import com.plushnode.gungame.attachments.ScopeAttachment;
import com.plushnode.gungame.weapons.*;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayerListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        GunGamePlugin.plugin.getPlayerManager().createPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        GunGamePlugin.plugin.getPlayerManager().destroyPlayer(event.getPlayer());
        GunGamePlugin.plugin.getInstanceManager().destroyPlayerInstances(event.getPlayer());

        // Not really necessary, but remove them just in case there's an accidental accumulation.
        GunGamePlugin.plugin.getDamageTracker().clearPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        GunGamePlugin.plugin.getDamageTracker().handleDeath(event);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Trigger trigger;

        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            trigger = Trigger.LeftClick;
        } else if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            trigger = Trigger.RightClick;
        } else {
            return;
        }

        if (activateWeapon(event.getPlayer(), trigger)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) return;
        Player player = (Player)event.getEntity().getShooter();

        ItemStack item = player.getInventory().getItemInMainHand();

        if (!item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();

        if (meta == null || !meta.hasLore()) return;
        if (meta.getLore().isEmpty()) return;

        for (String lore : meta.getLore()) {
            if (lore.startsWith("gg:")) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;

        activateWeapon(event.getPlayer(), Trigger.Sneak);
    }


    private boolean activateWeapon(Player player, Trigger trigger) {
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();

        if (meta == null || !meta.hasLore()) return false;
        if (meta.getLore().isEmpty()) return false;

        boolean activated = false;

        for (String lore : meta.getLore()) {
            if (!lore.startsWith("gg:")) continue;

            String weaponType = lore.substring(3);

            Weapon weapon = getWeapon(weaponType);

            if (weapon != null && weapon.activate(player, trigger)) {
                activated = true;
                GunGamePlugin.plugin.getInstanceManager().addWeapon(player, weapon);
            }
        }

        return activated;
    }

    // TODO: Create types from config file and stick in a map
    private Weapon getWeapon(String weaponType) {
        if ("shotgun".equalsIgnoreCase(weaponType)) {
            return new Shotgun();
        } else if ("ak47".equalsIgnoreCase(weaponType)) {
            return new AK47();
        } else if ("scope".equalsIgnoreCase(weaponType)) {
            return new ScopeAttachment();
        } else if ("grenade".equalsIgnoreCase(weaponType)) {
            return new Grenade();
        } else if ("flamethrower".equalsIgnoreCase(weaponType)) {
            return new Flamethrower();
        }

        return null;
    }
}
