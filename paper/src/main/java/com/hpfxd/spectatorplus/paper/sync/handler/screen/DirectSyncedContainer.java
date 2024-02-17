package com.hpfxd.spectatorplus.paper.sync.handler.screen;

import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;

public final class DirectSyncedContainer extends SyncedContainer {
    public DirectSyncedContainer(Player spectator, InventoryView spectatorView, InventoryView targetView) {
        super(spectator, spectatorView, targetView);
    }
}
