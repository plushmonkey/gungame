package com.plushnode.gungame.commands;

import com.plushnode.gungame.GunGamePlugin;
import com.plushnode.gungame.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CreateCommand implements MultiplexableCommand {
    private static final String[] ALIASES = { "create", "c" };
    private GunGamePlugin plugin;

    public CreateCommand(GunGamePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            sendUsage(sender);
            return true;
        }

        String weaponName = args[1];
        Player player = null;

        if (args.length >= 3) {
            String playerName = args[2];

            player = Bukkit.getPlayer(playerName);

            if (player == null) {
                ChatUtil.sendError(sender, "Could not find player with name of '" + playerName + "'.");
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                sendUsage(sender);
                return true;
            }

            player = (Player)sender;
        }

        // TODO: Check weapons from config file / global map
        if ("shotgun".equalsIgnoreCase(weaponName)) {
            ItemStack item = new ItemStack(Material.STICK, 1);

            applyWeapon(item, "Shotgun", true);

            applyDescription(item, ChatColor.GREEN + "Left-click or right-click to shoot.");

            player.getInventory().addItem(item);
        } else if ("ak47".equalsIgnoreCase(weaponName)) {
            ItemStack item = new ItemStack(Material.STICK, 1);

            applyWeapon(item, "AK47", true);
            applyWeapon(item, "Scope", false);

            applyDescription(item, ChatColor.GREEN + "Left-click to activate scope. Hold right-click to shoot.");

            player.getInventory().addItem(item);
        } else if ("grenade".equalsIgnoreCase(weaponName)) {
            ItemStack item = new ItemStack(Material.GUNPOWDER, 1);

            applyWeapon(item, "Grenade", true);

            applyDescription(item, ChatColor.GREEN + "Left-click to throw grenade.");
            applyDescription(item, ChatColor.GREEN + "Optionally hold sneak before throwing to set throw strength.");

            player.getInventory().addItem(item);
        } else if ("flamethrower".equalsIgnoreCase(weaponName)) {
            ItemStack item = new ItemStack(Material.BLAZE_ROD, 1);

            applyWeapon(item, "Flamethrower", true);

            applyDescription(item, ChatColor.GREEN + "Hold sneak to activate.");

            player.getInventory().addItem(item);
        } else if ("sniper".equalsIgnoreCase(weaponName)) {
            ItemStack item = new ItemStack(Material.STONE_HOE, 1);

            applyWeapon(item, "Sniper", true);
            applyWeapon(item, "NightScope", false);

            applyDescription(item, ChatColor.GREEN + "Left-click to activate scope. Right-click to shoot.");

            player.getInventory().addItem(item);
        } else if ("bipodsniper".equalsIgnoreCase(weaponName)) {
            ItemStack item = new ItemStack(Material.STONE_HOE, 1);

            applyWeapon(item, "Sniper", true);
            applyWeapon(item, "NightScope", false);
            applyWeapon(item, "Bipod", false);

            applyDescription(item, ChatColor.GREEN + "Left-click to activate scope and bipod. Right-click to shoot.");
            applyDescription(item, ChatColor.RED + "Requires client 1.14+ to function. Use normal Sniper with older client.");

            player.getInventory().addItem(item);
        } else if ("knife".equalsIgnoreCase(weaponName)) {
            ItemStack item = new ItemStack(Material.IRON_SWORD, 1);

            applyWeapon(item, "Knife", true);

            applyDescription(item, ChatColor.GREEN + "Provides speed while wielding.");
            applyDescription(item, ChatColor.GREEN + "Headshot and backstab attacks instantly kill.");

            player.getInventory().addItem(item);
        } else if ("molotov".equalsIgnoreCase(weaponName)) {
            ItemStack item = new ItemStack(Material.MAGMA_CREAM, 1);

            applyWeapon(item, "Molotov", true);

            applyDescription(item, ChatColor.GREEN + "Left-click to throw.");
            applyDescription(item, ChatColor.GREEN + "Optionally hold sneak before throwing to set throw strength.");
            applyDescription(item, ChatColor.GREEN + "Sets the ground ablaze on impact.");

            player.getInventory().addItem(item);
        }

        return true;
    }

    private void applyWeapon(ItemStack item, String weaponName, boolean applyName) {
        ItemMeta meta = item.getItemMeta();

        if (meta == null) return;

        List<String> lore;

        if (meta.hasLore()) {
            lore = meta.getLore();
        } else {
            lore = new ArrayList<>();
        }

        lore.add("gg:" + weaponName.toLowerCase());

        if (applyName) {
            meta.setDisplayName(ChatColor.GOLD + weaponName);
        }

        meta.setLore(lore);

        item.setItemMeta(meta);
    }

    private void applyDescription(ItemStack item, String description) {
        ItemMeta meta = item.getItemMeta();

        if (meta == null) return;

        List<String> lore;

        if (meta.hasLore()) {
            lore = meta.getLore();
        } else {
            lore = new ArrayList<>();
        }

        lore.add(description);

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    private void sendUsage(CommandSender sender) {
        ChatUtil.sendMessage(sender, "Usage: /gg create <weaponName> [player]");
        ChatUtil.sendMessage(sender, "Creates a gun that matches the provided weapon name and gives it to the specified player.");
    }

    @Override
    public String getDescription() {
        return "Creates a gun for a player.";
    }

    @Override
    public String getPermission() {
        return "gungame.create";
    }

    @Override
    public String[] getAliases() {
        return ALIASES;
    }
}
