package me.zypj.essentials.command;

import lombok.RequiredArgsConstructor;
import me.zypj.essentials.EssentialsPlugin;
import me.zypj.essentials.common.command.annotation.Command;
import me.zypj.essentials.common.command.enums.SenderType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.Damageable;

@RequiredArgsConstructor
public class FixCommand {

    private final EssentialsPlugin plugin;

    @Command(
            name = "fix",
            permission = "fix",
            sender = { SenderType.PLAYER }
    )
    public void onFix(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        var item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            player.sendMessage(plugin.getBootstrap().getMessagesAdapter()
                    .getMessage("fix.error-no-item"));
            return;
        }

        var meta = item.getItemMeta();
        if (meta instanceof Damageable dm) {
            dm.setDamage(0);
            item.setItemMeta(dm);
            player.sendMessage(plugin.getBootstrap().getMessagesAdapter()
                    .getMessage("fix.success"));
        }
    }
}
