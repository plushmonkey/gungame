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
