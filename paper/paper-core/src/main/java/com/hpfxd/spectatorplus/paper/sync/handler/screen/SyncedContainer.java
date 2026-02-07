package com.hpfxd.spectatorplus.paper.sync.handler.screen;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;

public abstract sealed class SyncedContainer extends SyncedScreen permits DirectSyncedContainer, ReplicaSyncedContainer {
    protected final InventoryView targetView;

    protected SyncedContainer(Player spectator, InventoryView targetView) {
        super(spectator);
        this.targetView = targetView;
    }

    @Override
    public boolean isRequestedByClient() {
        return false;
    }

    @Override
    public @NotNull Inventory getBottomInventory() {
        return this.targetView.getBottomInventory();
    }
}
