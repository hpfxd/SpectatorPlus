package com.hpfxd.spectatorplus.fabric.client;

import com.hpfxd.spectatorplus.fabric.client.sync.ClientSyncController;
import net.fabricmc.api.ClientModInitializer;

public class SpectatorClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientSyncController.init();
    }
}
