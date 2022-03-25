package com.plushnode.gungame.commands;

import org.bukkit.command.CommandSender;

public interface MultiplexableCommand {
    boolean execute(CommandSender sender, String[] args);

    // A short description used when the main command is sent without args.
    String getDescription();
    String getPermission();
    String[] getAliases();
}

