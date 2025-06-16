package me.zypj.essentials.api.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class YAML extends YamlConfiguration {

    private final File configFile;
    private final Plugin plugin;

    public YAML(String name, Plugin plugin, File folder)
            throws IOException, InvalidConfigurationException {
        this.plugin = plugin;
        File dir = folder != null ? folder : plugin.getDataFolder();
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Could not create folder: " + dir.getAbsolutePath());
        }

        String fileName = name.endsWith(".yml") ? name : name + ".yml";
        this.configFile = new File(dir, fileName);
        loadConfig();
    }

    public YAML(String name, Plugin plugin) throws IOException, InvalidConfigurationException {
        this(name, plugin, null);
    }

    private void loadConfig() throws IOException, InvalidConfigurationException {
        if (!configFile.exists()) {
            String folderName = configFile.getParentFile().getName();
            String resourcePath = folderName + "/" + configFile.getName();
            InputStream resource = plugin.getResource(resourcePath);

            if (resource == null) {
                resource = plugin.getResource(configFile.getName());
            }

            if (resource != null) {
                copyResource(resource, configFile);
            } else if (!configFile.createNewFile()) {
                throw new IOException(
                        "Could not create config file: " + configFile.getAbsolutePath());
            }
        }
        load(configFile);
    }

    private void copyResource(InputStream in, File dest) throws IOException {
        Files.copy(in, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        in.close();
    }

    public void saveDefaultConfig() {
        if (!configFile.exists()) {
            String folderName = configFile.getParentFile().getName();
            String resourcePath = folderName + "/" + configFile.getName();
            InputStream resource = plugin.getResource(resourcePath);

            if (resource == null) {
                resource = plugin.getResource(configFile.getName());
            }

            if (resource != null) {
                try {
                    copyResource(resource, configFile);
                } catch (IOException e) {
                    logError(
                            "Erro ao salvar recurso padrão para o arquivo " + configFile.getName(),
                            e);
                }
            } else {
                try {
                    File parent = configFile.getParentFile();
                    if (!parent.exists() && !parent.mkdirs()) {
                        logError(
                                "Não foi possível criar diretório para o arquivo "
                                        + configFile.getName(),
                                new IOException("mkdirs retornou false"));
                    }
                    if (!configFile.createNewFile()) {
                        logError(
                                "Não foi possível criar o arquivo " + configFile.getName(),
                                new IOException("createNewFile retornou false"));
                    }
                } catch (IOException e) {
                    logError("Erro ao criar o arquivo " + configFile.getName(), e);
                }
            }
        }
    }

    public void createDefaults() {
        saveDefaultConfig();
    }

    public boolean exists() {
        return configFile.exists();
    }

    public boolean delete() {
        return configFile.delete();
    }

    public void backup(String suffix) {
        File backup =
                new File(configFile.getParent(), configFile.getName() + "." + suffix + ".backup");
        try {
            Files.copy(configFile.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            plugin.getLogger()
                    .warning("Não foi possível criar o backup de: " + configFile.getName());
        }
    }

    @Override
    public void save(File file) throws IOException {
        super.save(configFile);
    }

    public void save() {
        try {
            super.save(configFile);
        } catch (IOException e) {
            logError("Erro ao salvar o arquivo " + configFile.getName(), e);
        }
    }

    public void reload() {
        try {
            loadConfig();
        } catch (Exception e) {
            logError("Erro ao recarregar o arquivo " + configFile.getName(), e);
        }
    }

    public void set(String path, Object value, boolean save) {
        super.set(path, value);
        if (save) {
            save();
        }
    }

    public void setDefault(String path, Object value) {
        if (!contains(path)) {
            set(path, value);
            save();
        }
    }

    public String getString(String path, boolean translateColors) {
        String raw = super.getString(path);
        if (raw == null) return null;
        return translateColors ? ChatColor.translateAlternateColorCodes('&', raw) : raw;
    }

    public List<String> getStringList(String path, boolean translateColors) {
        List<String> list = super.getStringList(path);
        if (!translateColors) return list;
        return list.stream()
                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public <T> T getOrSet(String path, T defaultValue) {
        if (!contains(path)) {
            set(path, defaultValue);
            save();
        }
        return (T) get(path);
    }

    public String getFormatted(String path, Object... args) {
        String raw = getString(path, true);
        return raw != null ? String.format(raw, args) : null;
    }

    public Set<String> getKeysRecursive(String path) {
        if (getConfigurationSection(path) == null) {
            return Collections.emptySet();
        }
        return getConfigurationSection(path).getKeys(true);
    }

    private void logError(String msg, Throwable t) {
        plugin.getLogger().severe("§c[YAML] " + msg);
        t.printStackTrace();
    }
}
