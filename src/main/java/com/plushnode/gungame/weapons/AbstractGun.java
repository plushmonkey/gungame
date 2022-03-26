package com.plushnode.gungame.weapons;

import com.plushnode.gungame.UpdateResult;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractGun implements Weapon {
    protected Player player;
    protected List<Bullet> bullets = new ArrayList<>();

    protected Location getEyeLocation(Player player) {
        Location location = player.getEyeLocation().clone();

        if (player.isSneaking()) {
            // getEyeLocation seems to not align correctly with the game camera's view position.
            // Sneak player height becomes 1.5 blocks tall, so game rendered eye becomes 1.5 - 0.2 = 1.3.
            location = player.getLocation().clone().add(0, 1.3, 0);
        }

        return location;
    }

    protected void applyRecoil(float amount) {
        Vector velocity = this.player.getVelocity().clone();

        velocity.add(this.player.getEyeLocation().getDirection().multiply(-amount));

        this.player.setVelocity(velocity);
    }

    @Override
    public UpdateResult update() {
        if (!player.isOnline()) return UpdateResult.Remove;
        if (player.getGameMode() == GameMode.SPECTATOR) return UpdateResult.Remove;

        bullets.removeIf(Bullet::update);

        return bullets.isEmpty() ? UpdateResult.Remove : UpdateResult.Continue;
    }

    @Override
    public void destroy() {

    }

    @Override
    public Player getPlayer() {
        return player;
    }
}
