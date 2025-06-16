package me.zypj.essentials.adapter;

import lombok.Getter;
import me.zypj.essentials.EssentialsPlugin;
import me.zypj.essentials.api.file.YAML;
import org.bukkit.configuration.InvalidConfigurationException;

import java.io.IOException;
import java.util.List;

@Getter
public class MessagesAdapter {

    private final YAML yaml;

    public MessagesAdapter(EssentialsPlugin plugin) {
        try {
            this.yaml = new YAML("messages", plugin);
            yaml.saveDefaultConfig();
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().severe("Failed to load messages.yml");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public String getMessage(String path) {
        return yaml.getString("messages." + path, true);
    }

    public List<String> getMessages(String path) {
        return yaml.getStringList("messages." + path, true);
    }
}
