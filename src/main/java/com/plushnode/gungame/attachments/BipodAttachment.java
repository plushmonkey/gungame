package com.plushnode.gungame.attachments;

import com.plushnode.gungame.GunGamePlugin;
import com.plushnode.gungame.Trigger;
import com.plushnode.gungame.UpdateResult;
import com.plushnode.gungame.weapons.Weapon;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

public class BipodAttachment implements Weapon {
    private static final BlockData BLOCKER_DATA = Material.BARRIER.createBlockData();

    private Player player;
    private Block headBlock;
    private Block currentBlock;
    private int slot;
    private World world;

    @Override
    public boolean activate(Player player, Trigger trigger) {
        if (trigger != Trigger.LeftClick) return false;

        BipodAttachment instance = GunGamePlugin.plugin.getInstanceManager().getFirstInstance(player, BipodAttachment.class);

        if (instance != null) {
            GunGamePlugin.plugin.getInstanceManager().destroyInstance(player, instance);
            return false;
        }

        this.player = player;
        this.world = player.getWorld();
        this.slot = player.getInventory().getHeldItemSlot();
        this.headBlock = this.player.getEyeLocation().getBlock();

        if (headBlock.getType() != Material.AIR) return false;

        Bukkit.getScheduler().runTaskLater(GunGamePlugin.plugin, () -> {
            player.sendBlockChange(headBlock.getLocation(), BLOCKER_DATA);
        }, 5);

        currentBlock = player.getLocation().getBlock();

        return true;
    }

    @Override
    public UpdateResult update() {
        if (!player.isOnline() || player.isDead() || player.getInventory().getHeldItemSlot() != this.slot) {
            return UpdateResult.Remove;
        }

        if (player.getWorld() != this.world) {
            return UpdateResult.Remove;
        }

        if (player.getGameMode() == GameMode.SPECTATOR) {
            return UpdateResult.Remove;
        }

        if (!player.getLocation().getBlock().equals(currentBlock)) {
            player.sendBlockChange(headBlock.getLocation(), Material.AIR.createBlockData());
            headBlock = player.getLocation().getBlock().getRelative(BlockFace.UP);
            player.sendBlockChange(headBlock.getLocation(), BLOCKER_DATA);
            currentBlock = player.getLocation().getBlock();
        }

        return UpdateResult.Continue;
    }

    public void sendBlocker() {
        player.sendBlockChange(headBlock.getLocation(), BLOCKER_DATA);
    }

    @Override
    public void destroy() {
        player.sendBlockChange(headBlock.getLocation(), Material.AIR.createBlockData());
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

    @Override
    public String getName() {
        return "Bipod";
    }
}
