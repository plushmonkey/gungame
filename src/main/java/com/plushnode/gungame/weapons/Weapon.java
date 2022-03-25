package com.plushnode.gungame.weapons;

import com.plushnode.gungame.Trigger;
import com.plushnode.gungame.UpdateResult;
import org.bukkit.entity.Player;

public interface Weapon {
    boolean activate(Player player, Trigger trigger);
    UpdateResult update();
    void destroy();

    Player getPlayer();
    String getName();
}
