package me.zypj.essentials.listener;

import me.zypj.essentials.EssentialsPlugin;
import me.zypj.essentials.service.GodService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class GodListener implements Listener {

    private final GodService godService;

    public GodListener(EssentialsPlugin plugin) {
        this.godService = plugin.getBootstrap().getGodService();
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player p && godService.isGod(p)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent e) {
        if (e.getEntity() instanceof Player p && godService.isGod(p)) {
            e.setCancelled(true);
        }
    }
}
