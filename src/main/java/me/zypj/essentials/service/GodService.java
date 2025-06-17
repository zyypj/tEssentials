package me.zypj.essentials.service;

import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GodService {

    private final Set<UUID> gods = ConcurrentHashMap.newKeySet();

    public boolean changeMode(Player player) {
        var id = player.getUniqueId();
        if (gods.remove(id)) {
            return false;
        }

        gods.add(id);
        return true;
    }

    public boolean isGod(Player player) {
        return gods.contains(player.getUniqueId());
    }
}
