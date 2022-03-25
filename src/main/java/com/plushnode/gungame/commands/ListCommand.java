package com.plushnode.gungame.commands;

import com.plushnode.gungame.GunGamePlugin;
import com.plushnode.gungame.util.ChatUtil;
import org.bukkit.command.CommandSender;

public class ListCommand implements MultiplexableCommand {
    private static final String[] ALIASES = { "list", "l" };
    private GunGamePlugin plugin;

    public ListCommand(GunGamePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        // TODO: Pull from registry
        String[] weapons = {"Shotgun, AK47, Grenade", "Flamethrower"};

        StringBuilder sb = new StringBuilder();

        sb.append("Weapons: ");

        for (int i = 0; i < weapons.length; ++i) {
            if (i > 0) {
                sb.append(", ");
            }

            sb.append(weapons[i]);
        }


        ChatUtil.sendMessage(sender, sb.toString());

        return true;
    }

    @Override
    public String getDescription() {
        return "Lists available weapons.";
    }

    @Override
    public String getPermission() {
        return "gungame.list";
    }

    @Override
    public String[] getAliases() {
        return ALIASES;
    }
}
