package me.zypj.essentials.loader;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.zypj.essentials.EssentialsPlugin;
import me.zypj.essentials.adapter.MessagesAdapter;

@Getter
@RequiredArgsConstructor
public class EssentialsBootstrap {

    private final EssentialsPlugin plugin;

    private MessagesAdapter messagesAdapter;

    public void init() {

        setupFiles();

    }

    private void setupFiles() {
        messagesAdapter = new MessagesAdapter(plugin);
    }
}
