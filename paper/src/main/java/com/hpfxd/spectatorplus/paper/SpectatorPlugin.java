package com.hpfxd.spectatorplus.paper;

import com.hpfxd.spectatorplus.paper.config.ServerConfig;
import com.hpfxd.spectatorplus.paper.sync.ServerSyncController;
import org.bukkit.plugin.java.JavaPlugin;

public class SpectatorPlugin extends JavaPlugin {
    private ServerConfig serverConfig;
    private ServerSyncController syncController;

    @Override
    public void onEnable() {
        if(!this.getDataFolder().exists() && !this.getDataFolder().mkdirs()) {
            this.getLogger().warning("Could not create config folder. Check read/write permissions.");
        }
        this.saveDefaultConfig();
        this.serverConfig = new ServerConfig(this.getConfig());

        this.syncController = new ServerSyncController(this);
        new SpectatorWorkarounds(this);
    }

    public ServerSyncController getSyncController() {
        return this.syncController;
    }

    public ServerConfig getServerConfig() {
        return this.serverConfig;
    }
}
