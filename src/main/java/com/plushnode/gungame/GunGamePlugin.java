package com.plushnode.gungame;

import com.plushnode.gungame.commands.CommandMultiplexer;
import com.plushnode.gungame.commands.CreateCommand;
import com.plushnode.gungame.commands.ListCommand;
import com.plushnode.gungame.listeners.PlayerListener;
import com.plushnode.gungame.physics.PhysicsSystem;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class GunGamePlugin extends JavaPlugin {
    public static GunGamePlugin plugin;

    private CommandMultiplexer multiplexer;
    private PlayerManager playerManager;
    private InstanceManager instanceManager;
    private PhysicsSystem physicsSystem;
    private DamageTracker damageTracker;

    @Override
    public void onEnable() {
        plugin = this;

        this.playerManager = new PlayerManager();
        this.instanceManager = new InstanceManager();
        this.physicsSystem = new PhysicsSystem();
        this.damageTracker = new DamageTracker();

        this.multiplexer = new CommandMultiplexer("gg");

        this.getCommand("gungame").setExecutor(this.multiplexer);
        this.getCommand("gg").setExecutor(this.multiplexer);

        this.multiplexer.registerCommand(new CreateCommand(this));
        this.multiplexer.registerCommand(new ListCommand(this));

        this.getServer().getPluginManager().registerEvents(new PlayerListener(), plugin);

        Bukkit.getScheduler().runTaskTimer(this, this::update, 1, 1);
    }

    @Override
    public void onDisable() {

    }

    public void update() {
        this.instanceManager.update();
        this.physicsSystem.update();
    }

    public PlayerManager getPlayerManager() {
        return this.playerManager;
    }

    public DamageTracker getDamageTracker() {
        return this.damageTracker;
    }

    public InstanceManager getInstanceManager() {
        return instanceManager;
    }

    public PhysicsSystem getPhysicsSystem() {
        return physicsSystem;
    }
}
