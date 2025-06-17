package me.zypj.essentials.common.command.service;

import org.bukkit.command.CommandSender;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownService {
    private final Map<String, Long> map = new ConcurrentHashMap<>();

    public boolean tryUse(CommandSender sender, String cmdKey, long cooldownSecs) {
        if (cooldownSecs <= 0) return true;

        String key = sender.getName() + "#" + cmdKey;

        long now = System.currentTimeMillis()/1000;
        Long expires = map.get(key);
        if (expires != null && expires > now) return false;

        map.put(key, now + cooldownSecs);
        return true;
    }
}
