package com.plushnode.gungame;

import com.plushnode.gungame.weapons.Weapon;
import org.bukkit.entity.Player;

import java.util.*;

public class InstanceManager {
    private Map<Player, List<Weapon>> globalInstances = new HashMap<>();
    private List<QueuedWeapon> addQueue = new ArrayList<>();

    public void addWeapon(Player user, Weapon instance) {
        addQueue.add(new QueuedWeapon(user, instance));
    }

    public boolean hasWeapon(Player user, Class<? extends Weapon> abilityType) {
        List<Weapon> weapons = globalInstances.get(user);
        if (weapons == null) return false;

        for (Weapon weapon : weapons) {
            if (weapon.getClass().equals(abilityType)) {
                return true;
            }
        }

        for (QueuedWeapon userInstance : addQueue) {
            if (userInstance.player.equals(user) && userInstance.weapon.getClass().equals(abilityType)) {
                return true;
            }
        }

        return false;
    }

    public void destroyInstance(Player user, Weapon weapon) {
        List<Weapon> weapons = globalInstances.get(user);
        if (weapon == null || weapons == null) {
            return;
        }

        weapons.remove(weapon);
        weapon.destroy();
    }

    public boolean destroyInstanceType(Player user, Class<? extends Weapon> clazz) {
        List<Weapon> weapons = globalInstances.get(user);
        if (weapons == null) return false;

        boolean destroyed = false;
        for (Iterator<Weapon> iterator = weapons.iterator(); iterator.hasNext();) {
            Weapon weapon = iterator.next();

            if (weapon.getClass() == clazz) {
                iterator.remove();
                weapon.destroy();
                destroyed = true;
            }
        }

        return destroyed;
    }

    // Get the number of active abilities.
    public int getInstanceCount() {
        int size = 0;
        for (List<Weapon> instances : globalInstances.values()) {
            size += instances.size();
        }
        return size;
    }

    public List<Weapon> getPlayerInstances(Player user) {
        List<Weapon> weapons = globalInstances.get(user);
        if (weapons == null) return new ArrayList<>();
        return weapons;
    }

    public <T extends Weapon> List<T> getPlayerInstances(Player user, Class<T> type) {
        List<T> result = new ArrayList<>();
        List<Weapon> weapons = globalInstances.get(user);

        if (weapons == null) return new ArrayList<>();

        for (Weapon weapon : weapons) {
            if (weapon.getClass() == type) {
                result.add(type.cast(weapon));
            }
        }

        for (QueuedWeapon userInstance : addQueue) {
            if (!userInstance.player.equals(user)) continue;

            if (userInstance.weapon.getClass() == type) {
                result.add(type.cast(userInstance.weapon));
            }
        }

        return result;
    }

    public <T extends Weapon> T getFirstInstance(Player user, Class<T> type) {
        List<Weapon> weapons = globalInstances.get(user);

        if (weapons == null) return null;

        for (Weapon weapon : weapons) {
            if (weapon.getClass() == type) {
                return type.cast(weapon);
            }
        }

        for (QueuedWeapon userInstance : addQueue) {
            if (userInstance.player.equals(user) && userInstance.weapon.getClass() == type) {
                return type.cast(userInstance.weapon);
            }
        }

        return null;
    }

    public List<Weapon> getInstances() {
        List<Weapon> totalInstances = new ArrayList<>();

        for (List<Weapon> instances : globalInstances.values()) {
            totalInstances.addAll(instances);
        }

        for (QueuedWeapon userInstance : addQueue) {
            totalInstances.add(userInstance.weapon);
        }

        return totalInstances;
    }

    public <T extends Weapon> List<T> getInstances(Class<T> type) {
        List<T> totalInstances = new ArrayList<>();

        for (List<Weapon> instances : globalInstances.values()) {
            for (Weapon ability : instances) {
                if (ability.getClass().equals(type)) {
                    totalInstances.add(type.cast(ability));
                }
            }
        }

        for (QueuedWeapon userInstance : addQueue) {
            if (userInstance.weapon.getClass().equals(type)) {
                totalInstances.add(type.cast(userInstance.weapon));
            }
        }

        return totalInstances;
    }

    // Destroy every instance created by a player.
    // Calls destroy on the weapon before removing it.
    public void destroyPlayerInstances(Player player) {
        List<Weapon> instances = globalInstances.get(player);

        if (instances != null) {
            for (Weapon weapon : instances) {
                weapon.destroy();
            }

            instances.clear();
        }

        globalInstances.remove(player);
    }

    public void destroyAllInstances() {
        Iterator<Map.Entry<Player, List<Weapon>>> playerIterator = globalInstances.entrySet().iterator();

        while (playerIterator.hasNext()) {
            Map.Entry<Player, List<Weapon>> entry = playerIterator.next();
            List<Weapon> instances = entry.getValue();

            for (Weapon weapon : instances) {
                try {
                    weapon.destroy();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            instances.clear();
            playerIterator.remove();
        }
    }

    public void update() {
        for (QueuedWeapon queuedWeapon : addQueue) {
            globalInstances.computeIfAbsent(queuedWeapon.player, key -> new ArrayList<>())
                    .add(queuedWeapon.weapon);
        }
        addQueue.clear();

        Iterator<Map.Entry<Player, List<Weapon>>> playerIterator = globalInstances.entrySet().iterator();
        List<Weapon> removed = new ArrayList<>();

        // Loop through each player and get their active instances
        while (playerIterator.hasNext()) {
            Map.Entry<Player, List<Weapon>> entry = playerIterator.next();
            List<Weapon> instances = entry.getValue();
            Iterator<Weapon> iterator = instances.iterator();

            // Loop through the player's weapons and update each one.
            while (iterator.hasNext()) {
                Weapon weapon = iterator.next();
                UpdateResult result = UpdateResult.Remove;

                // Catch any exception to prevent disrupting the regular processing.
                // It will be removed if an exception occurs.
                try {
                    result = weapon.update();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (result == UpdateResult.Remove) {
                    removed.add(weapon);
                    iterator.remove();
                }
            }

            // Remove the player from the global instances list if they have no more instances.
            if (entry.getValue().isEmpty()) {
                playerIterator.remove();
            }
        }

        removed.forEach(Weapon::destroy);
    }

    private static class QueuedWeapon {
        Player player;
        Weapon weapon;

        QueuedWeapon(Player player, Weapon weapon) {
            this.player = player;
            this.weapon = weapon;
        }
    }
}
