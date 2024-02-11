package com.hpfxd.spectatorplus.fabric;

import com.hpfxd.spectatorplus.fabric.sync.ServerSyncController;
import net.fabricmc.api.ModInitializer;

public class SpectatorMod implements ModInitializer {
    @Override
    public void onInitialize() {
        ServerSyncController.init();
    }
}
