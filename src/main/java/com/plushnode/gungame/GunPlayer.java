package com.plushnode.gungame;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class GunPlayer {
    private Player player;
    private Map<String, Long> cooldowns = new HashMap<>();

    public GunPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return this.player;
    }

    public void addCooldown(String weaponType, long duration) {
        cooldowns.put(weaponType, System.currentTimeMillis() + duration);
    }

    public boolean isOnCooldown(String weaponType) {
        long time = System.currentTimeMillis();
        long endTime = cooldowns.getOrDefault(weaponType, 0L);

        return endTime > time;
    }
}
