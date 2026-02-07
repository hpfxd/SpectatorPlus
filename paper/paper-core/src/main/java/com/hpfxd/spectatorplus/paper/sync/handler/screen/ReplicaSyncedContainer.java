package com.hpfxd.spectatorplus.paper.sync.handler.screen;

import com.hpfxd.spectatorplus.paper.SpectatorPlugin;
import com.hpfxd.spectatorplus.paper.util.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.java.JavaPlugin;

public sealed class ReplicaSyncedContainer extends SyncedContainer permits CraftingSyncedContainer {
    /**
     * Whether calling {@link ReflectionUtil#getContainerProperties(InventoryView)} has failed for this instance.
     * <p>
     * If it failed once, it is likely to fail again, so we use this to avoid calling this method every tick if it for
     * any reason fails on this container, whether that's because of a weird container or unsupported server version.
     */
    private boolean getPropertiesFailed;

    public ReplicaSyncedContainer(Player spectator, InventoryView targetView) {
        super(spectator, targetView);
    }

    @Override
    public void update() {
        // Sync all item slots
        final Inventory targetInventory = this.targetView.getTopInventory();
        final Inventory spectatorInventory = this.spectatorView.getTopInventory();
        // Cannot simply do spectatorInventory.setContents(targetInventory.getContents()) as that will only get items
        // from the primary inventory in some menu types (e.g. stonecutter)
        for (int slot = 0; slot < targetInventory.getSize(); slot++) {
            spectatorInventory.setItem(slot, targetInventory.getItem(slot));
        }

        // Note: The Bukkit InventoryView.Property API is deprecated and removed in 1.21+. There is no way to get all properties for a container. This is a limitation in Bukkit.
        // If you need to sync properties, you must use NMS or another workaround.
    }

    @Override
    protected InventoryView openToSpectator() {
        final Inventory inventory = createReplicaInventory(this.spectator, this.targetView);
        return this.spectator.openInventory(inventory);
    }

    private static Inventory createReplicaInventory(InventoryHolder owner, InventoryView targetView) {
        if (targetView.getType() == InventoryType.CHEST || targetView.getType() == InventoryType.ENDER_CHEST) {
            return Bukkit.createInventory(owner, targetView.getTopInventory().getSize(), targetView.title());
        } else {
            return Bukkit.createInventory(owner, targetView.getType(), targetView.title());
        }
    }
}
