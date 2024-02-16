package com.hpfxd.spectatorplus.fabric;

import com.hpfxd.spectatorplus.fabric.config.ConfigLoader;
import com.hpfxd.spectatorplus.fabric.config.ServerConfig;
import com.hpfxd.spectatorplus.fabric.sync.ServerSyncController;
import net.fabricmc.api.ModInitializer;

import java.io.IOException;
import java.nio.file.Files;

public class SpectatorMod implements ModInitializer {
    public static ConfigLoader<ServerConfig> configLoader;
    public static ServerConfig config;

    @Override
    public void onInitialize() {
        try {
            this.loadConfig();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load server config", e);
        }

        ServerSyncController.init();
    }

    private void loadConfig() throws IOException {
        configLoader = ConfigLoader.create(ServerConfig.class, "server");
        if (Files.exists(configLoader.getFile())) {
            config = configLoader.load();
        } else {
            config = new ServerConfig();
        }

        configLoader.save(config);
    }
}
