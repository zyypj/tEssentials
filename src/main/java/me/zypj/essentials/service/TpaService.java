package me.zypj.essentials.service;

import me.zypj.essentials.EssentialsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TpaService {
    private final EssentialsPlugin plugin;
    private final Map<UUID, UUID> requests = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> tasks    = new ConcurrentHashMap<>();

    public TpaService(EssentialsPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean send(Player sender, Player target) {
        var tId = target.getUniqueId();
        if (requests.containsKey(tId)) return false;

        requests.put(tId, sender.getUniqueId());
        int timeout = plugin.getConfig().getInt("tpa.timeout-seconds", 30);
        var task = Bukkit.getScheduler()
                .runTaskLater(plugin, () -> expire(target), timeout * 20L);
        tasks.put(tId, task);
        return true;
    }

    private void expire(Player target) {
        var tId = target.getUniqueId();
        var sId = requests.remove(tId);
        var task = tasks.remove(tId);

        if (task != null) task.cancel();
        if (sId == null) return;

        var messagesAdapter = plugin.getBootstrap().getMessagesAdapter();
        var sender = Bukkit.getPlayer(sId);
        if (sender != null) {
            sender.sendMessage(
                    messagesAdapter.getMessage("tpa.expired")
                            .replace("{TARGET}", target.getName())
            );
        }
        target.sendMessage(
                messagesAdapter.getMessage("tpa.expire-notify")
                        .replace("{PLAYER}",
                                Objects.requireNonNull(Bukkit.getOfflinePlayer(sId).getName()))
        );
    }

    public boolean accept(Player target, String requesterName) {
        var tId = target.getUniqueId();
        var sId = requests.get(tId);
        if (sId == null) return false;

        var requester = Bukkit.getPlayer(sId);
        if (requester == null || !requester.getName().equalsIgnoreCase(requesterName))
            return false;

        requests.remove(tId);
        var task = tasks.remove(tId);
        if (task != null) task.cancel();

        requester.teleport(target.getLocation());
        var messagesAdapter = plugin.getBootstrap().getMessagesAdapter();
        requester.sendMessage(
                messagesAdapter.getMessage("tpa.accepted.sender")
                        .replace("{TARGET}", target.getName())
        );
        target.sendMessage(
                messagesAdapter.getMessage("tpa.accepted.target")
                        .replace("{PLAYER}", requester.getName())
        );
        return true;
    }

    public boolean deny(Player target, String requesterName) {
        var tId = target.getUniqueId();
        var sId = requests.get(tId);
        if (sId == null) return false;
        var requester = Bukkit.getPlayer(sId);
        if (requester == null || !requester.getName().equalsIgnoreCase(requesterName))
            return false;

        requests.remove(tId);
        var task = tasks.remove(tId);
        if (task != null) task.cancel();

        var messagesAdapter = plugin.getBootstrap().getMessagesAdapter();
        requester.sendMessage(
                messagesAdapter.getMessage("tpa.denied.sender")
                        .replace("{TARGET}", target.getName())
        );
        target.sendMessage(
                messagesAdapter.getMessage("tpa.denied.target")
                        .replace("{PLAYER}", requester.getName())
        );
        return true;
    }
}
