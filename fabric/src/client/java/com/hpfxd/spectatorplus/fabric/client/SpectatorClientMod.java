package com.hpfxd.spectatorplus.fabric.client;

import com.hpfxd.spectatorplus.fabric.client.config.ClientConfig;
import com.hpfxd.spectatorplus.fabric.client.sync.ClientSyncController;
import com.hpfxd.spectatorplus.fabric.config.ConfigLoader;
import net.fabricmc.api.ClientModInitializer;

import java.io.IOException;
import java.nio.file.Files;

public class SpectatorClientMod implements ClientModInitializer {
    public static ConfigLoader<ClientConfig> configLoader;
    public static ClientConfig config;

    @Override
    public void onInitializeClient() {
        try {
            this.loadConfig();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load server config", e);
        }

        ClientSyncController.init();
        ClientTargetController.init();
    }

    private void loadConfig() throws IOException {
        configLoader = ConfigLoader.create(ClientConfig.class, "client");
        if (Files.exists(configLoader.getFile())) {
            config = configLoader.load();
        } else {
            config = new ClientConfig();
        }

        configLoader.save(config);
    }
}
