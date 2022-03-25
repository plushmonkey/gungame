package com.plushnode.gungame.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.*;

public class CommandMultiplexer implements CommandExecutor {
    private String mainCommand;
    private Map<String, MultiplexableCommand> commands = new HashMap<>();

    public CommandMultiplexer(String command) {
        this.mainCommand = command;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        String targetCommand = args[0].toLowerCase();
        MultiplexableCommand selectedCommand = commands.get(targetCommand);

        if (selectedCommand == null) {
            sendUsage(sender);
            return true;
        }

        String permission = selectedCommand.getPermission();

        if (permission != null && !sender.hasPermission(permission)) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        selectedCommand.execute(sender, args);

        return true;
    }

    private void sendUsage(CommandSender commandSender) {
        Set<MultiplexableCommand> commandSet = new HashSet<>();
        commandSet.addAll(commands.values());

        for (MultiplexableCommand command : commandSet) {
            String[] aliases = command.getAliases();
            if (aliases == null || aliases.length == 0)
                continue;

            // Hide any commands that the sender doesn't have permission for.
            if (!commandSender.hasPermission(command.getPermission())) continue;

            String name = aliases[0];
            String usage = ChatColor.GRAY + "/" + mainCommand + " " + ChatColor.DARK_AQUA + name + ChatColor.GRAY + " - " + ChatColor.GOLD + command.getDescription();

            commandSender.sendMessage(usage);
        }
    }

    public void registerCommand(MultiplexableCommand command) {
        String[] aliases = command.getAliases();

        for (String alias : aliases) {
            commands.put(alias.toLowerCase(), command);
        }
    }

    public void unregisterCommand(String commandName) {
        MultiplexableCommand command = commands.get(commandName.toLowerCase());

        if (command == null) return;

        Iterator<Map.Entry<String, MultiplexableCommand>> iterator = commands.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, MultiplexableCommand> entry = iterator.next();
            if (entry.getValue() == command) {
                iterator.remove();
            }
        }
    }
}

