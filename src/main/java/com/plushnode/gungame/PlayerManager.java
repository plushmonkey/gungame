package com.plushnode.gungame;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class PlayerManager {
    private Map<Player, GunPlayer> players = new HashMap<>();

    public void createPlayer(Player player) {
        players.put(player, new GunPlayer(player));
    }

    public void destroyPlayer(Player player) {
        players.remove(player);
    }

    public GunPlayer getPlayer(Player player) {
        return players.get(player);
    }
}
