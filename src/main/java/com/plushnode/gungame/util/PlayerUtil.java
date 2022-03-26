package com.plushnode.gungame.util;

import com.plushnode.gungame.GunGamePlugin;
import com.plushnode.gungame.attachments.BipodAttachment;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class PlayerUtil {
    private PlayerUtil() {

    }

    public static Location getEye(Player player) {
        Location location = player.getEyeLocation().clone();

        if (player.isSneaking()) {
            // getEyeLocation seems to not align correctly with the game camera's view position.
            // Sneak player height becomes 1.5 blocks tall, so game rendered eye becomes 1.5 - 0.2 = 1.3.
            location = player.getLocation().clone().add(0, 1.3, 0);
        }

        if (GunGamePlugin.plugin.getInstanceManager().getFirstInstance(player, BipodAttachment.class) != null) {
            // Swimming hitbox is 0.6m, so eye should be 0.2 below that.
            location = player.getLocation().clone().add(0, 0.4, 0);
        }

        return location;
    }
}
