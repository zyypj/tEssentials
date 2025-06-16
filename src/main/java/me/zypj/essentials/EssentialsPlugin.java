package me.zypj.essentials;

import com.google.common.base.Stopwatch;
import lombok.Getter;
import me.zypj.essentials.api.command.service.CommandFramework;
import me.zypj.essentials.command.GamemodeCommand;
import me.zypj.essentials.loader.EssentialsBootstrap;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class EssentialsPlugin extends JavaPlugin {

    private EssentialsBootstrap bootstrap;

    @Override
    public void onEnable() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        log("", false);
        log("&aStarting EssentialsPlugin...", false);

        bootstrap = new EssentialsBootstrap(this);
        bootstrap.init();

        registerCommands();

        log("&2EssentialsPlugin started in " + stopwatch.stop() + "!", false);
        log("", false);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void log(String message, boolean debug) {
        var formatted = ChatColor.translateAlternateColorCodes('&', message);

        if (debug && !getConfig().getBoolean("debug")) return;

        var prefix = debug ? "§f[RolleriteEssentials] §8§l[DEBUG] " : "§f[RolleriteEssentials] ";
        getServer()
                .getConsoleSender()
                .sendMessage(prefix + formatted);
    }

    private void registerCommands() {
        CommandFramework commandFramework = new CommandFramework(this);
        commandFramework.registerHandlers(
                new GamemodeCommand(this)
        );
    }
}
