package com.hpfxd.spectatorplus.paper.sync.handler;

import com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent;
import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent;
import com.hpfxd.spectatorplus.paper.SpectatorPlugin;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Container syncing works in one of two "modes" depending on the type of the inventory:
 * <ol>
 *     <li><i>Direct</i> mode which directly opens the container inventory to the spectator</li>
 *     <li><i>Replica</i> mode which creates a new inventory for the spectator, and copies items from the "target" inventory every tick.</li>
 * </ol>
 * <p>The <i>Direct</i> mode is used when the inventory has an underlying container that is viewed the same for all players (e.g. chests, droppers, furnaces)</p>
 * <p>The <i>Replica</i> mode is used when the inventory is usually not the same for all players that open it (e.g. crafting tables, anvils, ender chests)</p>
 */
public class ContainerSyncHandler implements Listener {
    private static final String PERMISSION = "spectatorplus.sync.container";

    private final SpectatorPlugin plugin;
    private final Map<UUID, SyncEntry> entries = new HashMap<>();

    private boolean opening;
    private boolean closing;

    public ContainerSyncHandler(SpectatorPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 0, 0);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onOpen(InventoryOpenEvent event) {
        if (this.opening) {
            // we don't want to listen here if this event is being called for opening a synced inventory to a spectator
            return;
        }

        try {
            this.opening = true;

            for (final Player spectator : this.plugin.getSyncController().getSpectators((Player) event.getPlayer(), PERMISSION)) {
                final InventoryType currentInventoryType = spectator.getOpenInventory().getTopInventory().getType();
                if (currentInventoryType == InventoryType.CRAFTING || currentInventoryType == InventoryType.CREATIVE) {
                    // if the player currently has an inventory screen already open, we want to skip opening this new inventory for them.
                    // UNLESS, their current inventory is already a synced one, which is okay to replace:
                    if (!this.entries.containsKey(spectator.getUniqueId())) {
                        continue;
                    }
                }

                this.handleNewInventory(spectator, event.getView(), false);
            }
        } finally {
            this.opening = false;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClose(InventoryCloseEvent event) {
        if (this.closing) {
            // we don't want to listen here if this event is being called for mass-closing synced spectator inventories
            return;
        }

        try {
            this.closing = true;

            for (final Iterator<SyncEntry> it = this.entries.values().iterator(); it.hasNext(); ) {
                final SyncEntry entry = it.next();

                if (entry.spectator.equals(event.getPlayer())) {
                    // event is being called for a spectator closing a synced inventory themself. just remove the entry
                    it.remove();
                } else if (entry instanceof SyncedContainer syncedContainer && syncedContainer.targetView.equals(event.getView())) {
                    // event is being called for a target closing an inventory, and this entry is for a spectator viewing it.
                    // close the inventory for the spectator, and remove the entry
                    entry.close();
                    it.remove();
                }
            }
        } finally {
            this.closing = false;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStartSpectating(PlayerStartSpectatingEntityEvent event) {
        final Player spectator = event.getPlayer();

        final SyncEntry entry = this.entries.remove(spectator.getUniqueId());
        if (entry != null) {
            entry.close();
        }

        if (event.getNewSpectatorTarget() instanceof final Player target && spectator.hasPermission(PERMISSION)) {
            this.handleNewInventory(spectator, target.getOpenInventory(), false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStopSpectating(PlayerStopSpectatingEntityEvent event) {
        final Player spectator = event.getPlayer();

        final SyncEntry entry = this.entries.remove(spectator.getUniqueId());
        if (entry != null) {
            entry.close();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClick(InventoryClickEvent event) {
        if (this.entries.get(event.getWhoClicked().getUniqueId()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClick(InventoryDragEvent event) {
        if (this.entries.get(event.getWhoClicked().getUniqueId()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.entries.remove(event.getPlayer().getUniqueId());
    }

    private void handleNewInventory(Player spectator, InventoryView targetView, boolean requestedOpen) {
        // TODO inventories: player, creative, enchanting table
        // (need to make sure player/creative is actually open, since this is called on spectate)

        switch (targetView.getType()) {
            case PLAYER:
                this.handleNewPlayerInventory(spectator, targetView.getPlayer().getInventory(), requestedOpen);
            case CHEST:
            case DISPENSER:
            case DROPPER:
            case FURNACE:
            case BREWING:
            case BEACON:
            case HOPPER:
            case SHULKER_BOX:
            case BARREL:
            case BLAST_FURNACE:
            case LECTERN:
            case SMOKER:
                this.handleNewDirectInventory(spectator, targetView, requestedOpen);
                break;
            case WORKBENCH:
            case LOOM:
            case STONECUTTER:
            case GRINDSTONE:
            case SMITHING:
            case ANVIL:
            case ENDER_CHEST:
                this.handleNewReplicaInventory(spectator, targetView, requestedOpen);
                break;
        }
    }

    private void handleNewDirectInventory(Player spectator, InventoryView targetView, boolean requestedOpen) {
        final InventoryView spectatorView = spectator.openInventory(targetView.getTopInventory());
        final SyncEntry entry = new SyncedContainer(spectator, spectatorView, targetView, requestedOpen);

        this.entries.put(spectator.getUniqueId(), entry);
    }

    private void handleNewReplicaInventory(Player spectator, InventoryView targetView, boolean requestedOpen) {
        final Inventory inventory;
        if (targetView.getType() == InventoryType.CHEST || targetView.getType() == InventoryType.ENDER_CHEST) {
            inventory = Bukkit.createInventory(spectator, targetView.getTopInventory().getSize(), targetView.title());
        } else {
            inventory = Bukkit.createInventory(spectator, targetView.getType(), targetView.title());
        }

        final InventoryView spectatorView = spectator.openInventory(inventory);
        final SyncEntry entry = new ReplicaSyncedInventory(spectator, spectatorView, targetView, requestedOpen);

        this.entries.put(spectator.getUniqueId(), entry);
    }

    private void handleNewPlayerInventory(Player spectator, PlayerInventory targetInventory, boolean requestedOpen) {
        final InventoryView spectatorView = spectator.openInventory(targetInventory);
        final SyncEntry entry = new SyncedPlayerInventory(spectator, spectatorView, targetInventory, requestedOpen);

        this.entries.put(spectator.getUniqueId(), entry);
    }

    private void handleNewPlayerCraftingInventory(Player spectator, InventoryView targetView, boolean requestedOpen) {
        final Inventory spectatorInventory = Bukkit.createInventory(spectator, targetView.getTopInventory().getSize() + targetView.getBottomInventory().getSize());
        final InventoryView spectatorView = spectator.openInventory(spectatorInventory);

        final SyncEntry entry = new SyncedPlayerCraftingInventory(spectator, spectatorView, targetView, requestedOpen);

        this.entries.put(spectator.getUniqueId(), entry);
    }

    private void tick() {
        for (final SyncEntry entry : this.entries.values()) {
            if (entry instanceof ReplicaSyncedInventory e) {
                e.update();
            }
        }
    }

    private abstract static class SyncEntry {
        public final Player spectator;
        public final InventoryView spectatorView;
        public final Inventory targetInventory;
        public final boolean requestedOpen;

        private SyncEntry(Player spectator, InventoryView spectatorView, Inventory targetInventory, boolean requestedOpen) {
            this.spectator = spectator;
            this.spectatorView = spectatorView;
            this.targetInventory = targetInventory;
            this.requestedOpen = requestedOpen;
        }

        public void close() {
            this.spectator.closeInventory();
        }
    }

    private static class SyncedPlayerInventory extends SyncEntry {
        private SyncedPlayerInventory(Player spectator, InventoryView spectatorView, PlayerInventory inventory, boolean requestedOpen) {
            super(spectator, spectatorView, inventory, requestedOpen);
        }
    }

    private static class SyncedContainer extends SyncEntry {
        public final InventoryView targetView;

        private SyncedContainer(Player spectator, InventoryView spectatorView, InventoryView targetView, boolean requestedOpen) {
            super(spectator, spectatorView, targetView.getTopInventory(), requestedOpen);
            this.targetView = targetView;
        }
    }

    private static class ReplicaSyncedInventory extends SyncedContainer {
        private ReplicaSyncedInventory(Player spectator, InventoryView spectatorView, InventoryView targetView, boolean requestedOpen) {
            super(spectator, spectatorView, targetView, requestedOpen);
        }

        public void update() {
            this.spectatorView.getTopInventory().setContents(this.targetView.getTopInventory().getContents());
        }
    }

    private static class SyncedPlayerCraftingInventory extends ReplicaSyncedInventory {
        private SyncedPlayerCraftingInventory(Player spectator, InventoryView spectatorView, InventoryView targetView, boolean requestedOpen) {
            super(spectator, spectatorView, targetView, requestedOpen);
        }

        @Override
        public void update() {
            final ItemStack[] contents = ArrayUtils.addAll(this.targetView.getTopInventory().getContents(), this.targetView.getBottomInventory().getContents());
            this.spectatorView.getTopInventory().setContents(contents);
        }
    }
}
