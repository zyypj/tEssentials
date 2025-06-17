package me.zypj.essentials.command;

import me.zypj.essentials.common.command.annotation.Command;
import me.zypj.essentials.common.command.enums.SenderType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TrashCommand {

    @Command(
            name = "trash",
            sender = { SenderType.PLAYER }
    )
    public void onTrash(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        player.openInventory(Bukkit.createInventory(null, 9 * 4));
    }
}
