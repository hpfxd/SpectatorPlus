package com.hpfxd.spectatorplus.paper;

import com.hpfxd.spectatorplus.paper.sync.ServerSyncController;
import org.bukkit.plugin.java.JavaPlugin;

public class SpectatorPlugin extends JavaPlugin {
    private ServerSyncController syncController;

    @Override
    public void onEnable() {
        this.syncController = new ServerSyncController(this);
        new SpectatorWorkarounds(this);
    }

    public ServerSyncController getSyncController() {
        return this.syncController;
    }
}
