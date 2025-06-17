package me.zypj.essentials.command;

import lombok.RequiredArgsConstructor;
import me.zypj.essentials.EssentialsPlugin;
import me.zypj.essentials.adapter.MessagesAdapter;
import me.zypj.essentials.common.command.annotation.Command;
import me.zypj.essentials.common.command.annotation.TabComplete;
import me.zypj.essentials.common.command.enums.SenderType;
import me.zypj.essentials.service.TpaService;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class TpaCommand {

    private final EssentialsPlugin plugin;
    private final TpaService service;

    @Command(
            name = "tpa",
            permission = "essentials.tpa",
            sender = {SenderType.PLAYER}
    )
    public void onTpa(CommandSender sender, String[] args) {
        var msgs = plugin.getBootstrap().getMessagesAdapter();
        if (args.length < 1) {
            sender.sendMessage(msgs.getMessage("tpa.error-usage"));
            return;
        }
        var sub = args[0].toLowerCase();
        if (sub.equals("accept") || sub.equals("deny")) {
            handleResponse(sender, sub, args, msgs);
        } else {
            handleRequest(sender, args[0], msgs);
        }
    }

    private void handleRequest(CommandSender sender,
                               String targetName,
                               MessagesAdapter messagesAdapter) {
        var req = (Player) sender;
        var target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            req.sendMessage(messagesAdapter.getMessage("player-not-found"));
            return;
        }
        if (!service.send(req, target)) {
            req.sendMessage(messagesAdapter.getMessage("tpa.already-pending"));
            return;
        }

        req.sendMessage(
                messagesAdapter.getMessage("tpa.request-sent")
                        .replace("{TARGET}", target.getName())
        );

        var template = messagesAdapter.getMessage("tpa.request-received")
                .replace("{PLAYER}", req.getName());
        var parts = template.split("\\{BUTTONS\\}", -1);
        var prefix = parts[0];
        var suffix = parts.length > 1 ? parts[1] : "";

        var order = messagesAdapter.getMessages("tpa.button-order");
        var sep = messagesAdapter.getMessage("tpa.button-separator");

        var components = new ArrayList<BaseComponent>();
        components.add(new TextComponent(prefix));

        boolean first = true;
        for (var key : order) {
            if (!first) components.add(new TextComponent(sep));
            first = false;

            var label = messagesAdapter.getMessage("tpa.buttons." + key);
            var btn = new TextComponent(label);
            btn.setClickEvent(new ClickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    "/tpa " + key + " " + req.getName()
            ));
            components.add(btn);
        }

        if (!suffix.isEmpty()) {
            components.add(new TextComponent(suffix));
        }

        target.spigot().sendMessage(
                components.toArray(new BaseComponent[0])
        );
    }

    private void handleResponse(CommandSender sender,
                                String action,
                                String[] args,
                                MessagesAdapter messagesAdapter) {
        var target = (Player) sender;
        if (args.length < 2) {
            target.sendMessage(messagesAdapter.getMessage("tpa.error-usage-response"));
            return;
        }
        var name = args[1];
        var ok = action.equals("accept")
                ? service.accept(target, name)
                : service.deny(target, name);

        var key = action.equals("accept")
                ? ok ? "tpa.accepted.target" : "tpa.accept-not-found"
                : ok ? "tpa.denied.target" : "tpa.accept-not-found";

        target.sendMessage(
                messagesAdapter.getMessage(key)
                        .replace("{PLAYER}", name)
        );
    }

    @TabComplete(command = "tpa")
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        var online = Bukkit.getOnlinePlayers().stream().map(Player::getName);
        if (args.length == 1) {
            return Stream.concat(
                            Stream.of("accept", "deny"),
                            online
                    )
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        if (args.length == 2 && (
                args[0].equalsIgnoreCase("accept")
                        || args[0].equalsIgnoreCase("deny")
        )) {
            return online
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
