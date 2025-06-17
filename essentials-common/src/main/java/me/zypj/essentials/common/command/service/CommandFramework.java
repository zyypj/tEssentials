package me.zypj.essentials.common.command.service;

import me.zypj.essentials.common.command.annotation.Command;
import me.zypj.essentials.common.command.annotation.SubCommand;
import me.zypj.essentials.common.command.annotation.TabComplete;
import me.zypj.essentials.common.command.enums.SenderType;
import me.zypj.essentials.common.command.models.CommandMeta;
import me.zypj.essentials.common.command.models.SubCommandMeta;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public final class CommandFramework implements CommandExecutor, TabCompleter {
    private static final String NO_PERMISSION_MESSAGE = "§4§lERROR! §cYou don't have permission to execute this command. ";

    private final JavaPlugin plugin;
    private final CooldownService cooldown;
    private final CommandMap commandMap;
    private final Map<String, CommandMeta> commands = new HashMap<>();
    private final Map<String, Method> tabCompleteMethods = new HashMap<>();

    public CommandFramework(JavaPlugin plugin) {
        this.plugin = plugin;
        this.cooldown = new CooldownService();
        this.commandMap = extractCommandMap();
    }

    private CommandMap extractCommandMap() {
        try {
            var method = plugin.getServer().getClass().getMethod("getCommandMap");
            return (CommandMap) method.invoke(plugin.getServer());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot access CommandMap", e);
        }
    }

    public void registerHandlers(Object... handlers) {
        for (var handler : handlers) {
            scanHandler(handler);
        }
        commands.values().forEach(this::registerCommand);
    }

    private void scanHandler(Object handler) {
        for (var method : handler.getClass().getDeclaredMethods()) {
            method.setAccessible(true);

            if (method.isAnnotationPresent(Command.class)) {
                var ann = method.getAnnotation(Command.class);
                commands.put(ann.name().toLowerCase(),
                        new CommandMeta(
                                ann.name().toLowerCase(),
                                handler,
                                method,
                                ann.permission(),
                                ann.usage(),
                                ann.description(),
                                ann.cooldown(),
                                Set.of(ann.sender())
                        )
                );
            }

            if (method.isAnnotationPresent(SubCommand.class)) {
                var ann = method.getAnnotation(SubCommand.class);
                var parent = commands.get(ann.parent().toLowerCase());
                if (parent != null) {
                    parent.getSubCommands().put(ann.name().toLowerCase(),
                            new SubCommandMeta(
                                    ann.name().toLowerCase(),
                                    handler,
                                    method,
                                    ann.permission(),
                                    ann.usage(),
                                    ann.description(),
                                    ann.cooldown(),
                                    Set.of(ann.sender())
                            )
                    );
                } else {
                    plugin.getLogger().warning("""
                            SubCommand '%s' not registered: parent '%s' not found.
                            """.formatted(ann.name(), ann.parent()));
                }
            }

            if (method.isAnnotationPresent(TabComplete.class)) {
                var ann = method.getAnnotation(TabComplete.class);
                tabCompleteMethods.put(ann.command().toLowerCase(), method);
            }
        }
    }

    private void registerCommand(CommandMeta meta) {
        try {
            var name = meta.getName().toLowerCase();
            var ctor = PluginCommand.class
                    .getDeclaredConstructor(String.class, Plugin.class);
            ctor.setAccessible(true);

            var cmd = ctor.newInstance(name, plugin);
            cmd.setExecutor(this);
            cmd.setTabCompleter(this);

            commandMap.register("", cmd);
        } catch (ReflectiveOperationException e) {
            plugin.getLogger().severe("Error registering command '"
                    + meta.getName() + "': " + e.getMessage());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd,
                             String label, String[] args) {
        var key = label.toLowerCase();
        var meta = commands.get(key);
        if (meta == null || !isAllowed(meta.getAllowedSenders(), sender)) {
            return false;
        }

        if (args.length > 0 && meta.getSubCommands().containsKey(args[0].toLowerCase())) {
            var sub = meta.getSubCommands().get(args[0].toLowerCase());
            if (!isAllowed(sub.getAllowedSenders(), sender)
                    || !hasPermission(sender, sub.getPermission())
                    || !cooldown.tryUse(sender, key + "." + sub.getName(), sub.getCooldown())) {
                return false;
            }
            invoke(sender, sub.getMethod(), Arrays.copyOfRange(args, 1, args.length));
        } else {
            if (!hasPermission(sender, meta.getPermission())
                    || !cooldown.tryUse(sender, key, meta.getCooldown())) {
                return false;
            }
            invoke(sender, meta.getMethod(), args);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender,
                                      org.bukkit.command.Command cmd,
                                      String alias, String[] args) {
        var method = tabCompleteMethods.get(alias.toLowerCase());
        if (method != null) {
            try {
                var handler = findHandler(method);
                @SuppressWarnings("unchecked")
                var suggestions = (List<String>) method.invoke(handler, sender, args);
                return suggestions != null ? suggestions : List.of();
            } catch (Exception e) {
                plugin.getLogger().severe("Error in tabComplete: " + e.getMessage());
                return List.of();
            }
        }

        var meta = commands.get(alias.toLowerCase());
        if (meta == null || args.length != 1) {
            return List.of();
        }

        return meta.getSubCommands().keySet().stream()
                .filter(sub -> sub.startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
    }

    private boolean isAllowed(Set<SenderType> allowed, CommandSender sender) {
        if (allowed.contains(SenderType.ALL)) {
            return true;
        }
        if (sender instanceof Player) {
            return allowed.contains(SenderType.PLAYER);
        }
        return allowed.contains(SenderType.CONSOLE);
    }


    private boolean hasPermission(CommandSender sender, String permKey) {
        if (permKey == null || permKey.isBlank()) {
            return true;
        }
        var node = "permissions." + permKey;
        var perm = plugin.getConfig().getString(node, permKey);
        if (sender.hasPermission(perm)) {
            return true;
        }
        sender.sendMessage(NO_PERMISSION_MESSAGE);
        return false;
    }

    private void invoke(CommandSender sender, Method method, String[] args) {
        var handler = findHandler(method);
        if (handler == null) {
            plugin.getLogger().severe("Handler not found for " + method.getName());
            return;
        }
        try {
            method.invoke(handler, sender, args);
        } catch (Exception e) {
            plugin.getLogger().severe("Error executing " + method.getName() + ": " + e.getMessage());
        }
    }

    private Object findHandler(Method method) {
        for (var cmdMeta : commands.values()) {
            if (cmdMeta.getMethod().equals(method)) {
                return cmdMeta.getHandler();
            }
            for (var sub : cmdMeta.getSubCommands().values()) {
                if (sub.getMethod().equals(method)) {
                    return sub.getHandler();
                }
            }
        }
        return null;
    }
}
