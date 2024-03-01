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

        // Note: The Bukkit InventoryView.Property API is flawed. Each property can only apply to one InventoryType,
        // but they should apply to multiple. Example: The COOK_TIME property should be able to be set on
        // inventories with the types: FURNACE, BLAST_FURNACE, and SMOKER, but it only works on FURNACE.
        // TODO could use NMS to set the properties instead of Bukkit's API, but it doesn't really matter since I think
        //  the only type this really effects is FURNACE, which is handled as a DirectSyncedContainer anyway.

        if (!this.getPropertiesFailed) {
            try {
                // Sync all data slots (called a "Property" in Bukkit)
                ReflectionUtil.getContainerProperties(this.targetView).forEach(this.spectatorView::setProperty);
            } catch (ReflectiveOperationException e) {
                this.getPropertiesFailed = true;
                JavaPlugin.getPlugin(SpectatorPlugin.class).getSLF4JLogger().warn("Failed to retrieve container properties for a replica container", e);
            }
        }
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
