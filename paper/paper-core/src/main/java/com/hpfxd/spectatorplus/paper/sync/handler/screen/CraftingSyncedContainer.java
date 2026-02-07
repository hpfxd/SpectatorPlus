package com.hpfxd.spectatorplus.paper.sync.handler.screen;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;

public final class CraftingSyncedContainer extends ReplicaSyncedContainer {
    public CraftingSyncedContainer(Player spectator, InventoryView targetView) {
        super(spectator, targetView);
    }

    @Override
    protected InventoryView openToSpectator() {
        final Inventory inventory = createReplicaInventory(this.spectator, this.targetView);
        return this.spectator.openInventory(inventory);
    }

    @Override
    public boolean requiresClientMod() {
        // Not necessarily "required"... but... if a client without the mod opens this container,
        // it will just look dumb. (9 slots, and all that shows is the inventory crafting slots).
        return true;
    }

    private static Inventory createReplicaInventory(InventoryHolder owner, InventoryView targetView) {
        return Bukkit.createInventory(owner, 9, targetView.title());
    }

    @Override
    public boolean isSurvivalInventory() {
        return true;
    }
}
