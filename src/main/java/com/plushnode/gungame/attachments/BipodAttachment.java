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
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

public class BipodAttachment implements Weapon {
    private static final BlockData BLOCKER_DATA = Material.BARRIER.createBlockData();

    private Player player;
    private Block headBlock;
    private BlockData previousData;
    private Block currentBlock;
    private int slot;
    private World world;
    private ArmorStand armorStand;

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

        if (!headBlock.isPassable()) return false;

        Bukkit.getScheduler().runTaskLater(GunGamePlugin.plugin, this::setHeadBlock, 5);

        currentBlock = player.getLocation().getBlock();
        player.setSwimming(true);
        player.setSprinting(false);

        // Spawn an armor stand that makes the player's display name invisible.
        armorStand = world.spawn(player.getLocation(), ArmorStand.class, entity -> {
            entity.setBasePlate(false);
            entity.setVisible(false);
            entity.setGravity(true);
            entity.getLocation().setDirection(player.getLocation().getDirection());
            entity.setMarker(false);
            entity.setSmall(true);

            player.addPassenger(entity);
        });

        return true;
    }

    private void setHeadBlock() {
        previousData = headBlock.getBlockData().clone();
        player.sendBlockChange(headBlock.getLocation(), BLOCKER_DATA);
    }

    private void revertHeadBlock() {
        if (previousData != null) {
            player.sendBlockChange(headBlock.getLocation(), previousData);
        }
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
            revertHeadBlock();
            headBlock = player.getLocation().getBlock().getRelative(BlockFace.UP);
            setHeadBlock();
            currentBlock = player.getLocation().getBlock();
        }

        return UpdateResult.Continue;
    }

    public void sendBlocker() {
        player.sendBlockChange(headBlock.getLocation(), BLOCKER_DATA);
    }

    @Override
    public void destroy() {
        revertHeadBlock();

        player.setSwimming(false);

        player.removePassenger(armorStand);

        armorStand.remove();
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
