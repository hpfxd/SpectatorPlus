package com.hpfxd.spectatorplus.paper.sync.handler.screen;

import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;

public final class DirectSyncedContainer extends SyncedContainer {
    public DirectSyncedContainer(Player spectator, InventoryView targetView) {
        super(spectator, targetView);
    }

    @Override
    protected InventoryView openToSpectator() {
        return this.spectator.openInventory(this.targetView.getTopInventory());
    }
}
