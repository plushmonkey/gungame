package com.plushnode.gungame.listeners;

import com.plushnode.gungame.DamageTracker;
import com.plushnode.gungame.GunGamePlugin;
import com.plushnode.gungame.Trigger;
import com.plushnode.gungame.attachments.BipodAttachment;
import com.plushnode.gungame.attachments.ScopeAttachment;
import com.plushnode.gungame.weapons.*;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayerListener implements Listener {
    private static SwapAction currentSwap;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        GunGamePlugin.plugin.getPlayerManager().createPlayer(event.getPlayer());

        activateWeapon(event.getPlayer(), Trigger.HotbarSwap);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        GunGamePlugin.plugin.getPlayerManager().destroyPlayer(event.getPlayer());
        GunGamePlugin.plugin.getInstanceManager().destroyPlayerInstances(event.getPlayer());

        // Not really necessary, but remove them just in case there's an accidental accumulation.
        GunGamePlugin.plugin.getDamageTracker().clearPlayer(event.getPlayer());
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (!event.getFrom().getWorld().equals(event.getTo().getWorld())) {
            GunGamePlugin.plugin.getInstanceManager().destroyPlayerInstances(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        GunGamePlugin.plugin.getDamageTracker().handleDeath(event);
    }

    @EventHandler
    public void onEntityToggleSwim(EntityToggleSwimEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player)event.getEntity();

        if (!event.isSwimming()) {
            BipodAttachment bipod = GunGamePlugin.plugin.getInstanceManager().getFirstInstance(player, BipodAttachment.class);

            if (bipod != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();

        if (action == Action.RIGHT_CLICK_BLOCK || action == Action.LEFT_CLICK_BLOCK) {
            Block clickedBlock = event.getClickedBlock();

            if (clickedBlock == null || clickedBlock.getType() == Material.AIR) {
                BipodAttachment bipod = GunGamePlugin.plugin.getInstanceManager().getFirstInstance(event.getPlayer(), BipodAttachment.class);

                if (bipod != null) {
                    if (action == Action.RIGHT_CLICK_BLOCK) {
                        Bukkit.getScheduler().runTaskLater(GunGamePlugin.plugin, bipod::sendBlocker, 1);
                    } else {
                        return;
                    }
                }
            }
        }

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
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        int newSlot = event.getNewSlot();

        ItemStack item = event.getPlayer().getInventory().getItem(newSlot);
        if (item != null && isWeapon(item)) {
            currentSwap =  new SwapAction(event.getPlayer(), newSlot);
            activateWeapon(event.getPlayer(), Trigger.HotbarSwap, item);
            currentSwap = null;
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK && event.getCause() != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) return;

        Player damager = (Player)event.getDamager();

        // Minimize swaps from other types of damage.
        if (damager.getLocation().distanceSquared(event.getEntity().getLocation()) > 15 * 15) return;

        // Prevent projectiles/explosives from triggering as a knife with a swap.
        if (!GunGamePlugin.plugin.getDamageTracker().isDamaging(damager)) {
            Knife instance = GunGamePlugin.plugin.getInstanceManager().getFirstInstance(damager, Knife.class);

            if (instance != null) {
                instance.applyDamage(event);
            }
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void onPlayerItemDamage(PlayerItemDamageEvent event) {
        if (isWeapon(event.getItem())) {
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

        return activateWeapon(player, trigger, item);
    }

    public boolean isWeapon(ItemStack item) {
        if (!item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();

        if (meta == null || !meta.hasLore()) return false;
        if (meta.getLore().isEmpty()) return false;

        for (String lore : meta.getLore()) {
            if (lore.startsWith("gg:")) {
                return true;
            }
        }

        return false;
    }

    private boolean activateWeapon(Player player, Trigger trigger, ItemStack item) {
        if (!item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();

        if (meta == null || !meta.hasLore()) return false;
        if (meta.getLore().isEmpty()) return false;

        boolean hasWeapon = false;

        for (String lore : meta.getLore()) {
            if (!lore.startsWith("gg:")) continue;

            String weaponType = lore.substring(3);

            Weapon weapon = getWeapon(weaponType);

            if (weapon != null) {
                hasWeapon = true;
            }

            if (weapon != null && weapon.activate(player, trigger)) {
                GunGamePlugin.plugin.getInstanceManager().addWeapon(player, weapon);
            }
        }

        return hasWeapon;
    }

    // TODO: Create types from config file and stick in a map
    private Weapon getWeapon(String weaponType) {
        if ("shotgun".equalsIgnoreCase(weaponType)) {
            return new Shotgun();
        } else if ("ak47".equalsIgnoreCase(weaponType)) {
            return new AK47();
        } else if ("scope".equalsIgnoreCase(weaponType)) {
            return new ScopeAttachment(5, false);
        } else if ("nightscope".equalsIgnoreCase(weaponType)) {
            return new ScopeAttachment(5, true);
        }  else if ("bipod".equalsIgnoreCase(weaponType)) {
            return new BipodAttachment();
        } else if ("grenade".equalsIgnoreCase(weaponType)) {
            return new Grenade();
        } else if ("flamethrower".equalsIgnoreCase(weaponType)) {
            return new Flamethrower();
        } else if ("sniper".equalsIgnoreCase(weaponType)) {
            return new SniperRifle();
        } else if ("knife".equalsIgnoreCase(weaponType)) {
            return new Knife();
        } else if ("molotov".equalsIgnoreCase(weaponType)) {
            return new Molotov();
        }

        return null;
    }

    public static int getPlayerSlot(Player player) {
        if (currentSwap != null && currentSwap.player == player) {
            return currentSwap.newSlot;
        }

        return player.getInventory().getHeldItemSlot();
    }

    private static class SwapAction {
        Player player;
        int newSlot;

        private SwapAction(Player player, int slot) {
            this.player = player;
            this.newSlot = slot;
        }
    }
}
