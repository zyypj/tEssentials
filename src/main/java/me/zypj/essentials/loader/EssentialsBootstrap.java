package me.zypj.essentials.loader;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.zypj.essentials.EssentialsPlugin;
import me.zypj.essentials.adapter.MessagesAdapter;
import me.zypj.essentials.service.GodService;
import me.zypj.essentials.service.TpaService;

@Getter
@RequiredArgsConstructor
public class EssentialsBootstrap {

    private final EssentialsPlugin plugin;

    private MessagesAdapter messagesAdapter;

    private GodService godService;
    private TpaService tpaService;

    public void init() {
        setupFiles();
        setupServices();

    }

    private void setupFiles() {
        messagesAdapter = new MessagesAdapter(plugin);
    }

    private void setupServices() {
        godService = new GodService();
        tpaService = new TpaService(plugin);
    }
}
