package com.plushnode.gungame.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public final class ChatUtil {
    private ChatUtil() {

    }

    public static void sendError(CommandSender target, String message) {
        target.sendMessage(getChatPrefix() + ChatColor.RED + message);
    }

    public static void sendMessage(CommandSender target, String message) {
        target.sendMessage(getChatPrefix() + message);
    }

    public static String getChatPrefix() {
        return ChatColor.DARK_GRAY + "[" + ChatColor.DARK_AQUA + "GunGame" + ChatColor.DARK_GRAY + "] " + ChatColor.GOLD;
    }
}
