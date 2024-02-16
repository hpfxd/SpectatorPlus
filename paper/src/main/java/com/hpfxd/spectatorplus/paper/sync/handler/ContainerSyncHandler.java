package com.hpfxd.spectatorplus.paper.sync.handler;

import com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent;
import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent;
import com.hpfxd.spectatorplus.paper.SpectatorPlugin;
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
    private final Map<UUID, SyncedInventory> inventories = new HashMap<>();

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
                    if (!this.inventories.containsKey(spectator.getUniqueId())) {
                        continue;
                    }
                }

                this.handleNewInventory(spectator, event.getView());
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

            for (final Iterator<SyncedInventory> it = this.inventories.values().iterator(); it.hasNext(); ) {
                final SyncedInventory entry = it.next();

                if (entry.spectator.equals(event.getPlayer())) {
                    // event is being called for a spectator closing a synced inventory themself. just remove the entry
                    it.remove();
                } else if (entry.targetView.equals(event.getView())) {
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

        final SyncedInventory entry = this.inventories.remove(spectator.getUniqueId());
        if (entry != null) {
            entry.close();
        }

        if (event.getNewSpectatorTarget() instanceof final Player target && spectator.hasPermission(PERMISSION)) {
            this.handleNewInventory(spectator, target.getOpenInventory());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStopSpectating(PlayerStopSpectatingEntityEvent event) {
        final Player spectator = event.getPlayer();

        final SyncedInventory entry = this.inventories.remove(spectator.getUniqueId());
        if (entry != null) {
            entry.close();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClick(InventoryClickEvent event) {
        if (this.inventories.get(event.getWhoClicked().getUniqueId()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClick(InventoryDragEvent event) {
        if (this.inventories.get(event.getWhoClicked().getUniqueId()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.inventories.remove(event.getPlayer().getUniqueId());
    }

    private void handleNewInventory(Player spectator, InventoryView targetView) {
        // TODO inventories: player, creative, enchanting table
        // (need to make sure player/creative is actually open, since this is called on spectate)

        switch (targetView.getType()) {
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
                this.handleNewDirectInventory(spectator, targetView);
                break;
            case WORKBENCH:
            case LOOM:
            case STONECUTTER:
            case GRINDSTONE:
            case SMITHING:
            case ANVIL:
            case ENDER_CHEST:
                this.handleNewReplicaInventory(spectator, targetView);
                break;
        }
    }

    private void handleNewDirectInventory(Player spectator, InventoryView targetView) {
        final InventoryView view = spectator.openInventory(targetView.getTopInventory());
        final SyncedInventory entry = new SyncedInventory(spectator, view, targetView);

        this.inventories.put(spectator.getUniqueId(), entry);
    }

    private void handleNewReplicaInventory(Player spectator, InventoryView targetView) {
        final Inventory inventory;
        if (targetView.getType() == InventoryType.CHEST || targetView.getType() == InventoryType.ENDER_CHEST) {
            inventory = Bukkit.createInventory(spectator, targetView.getTopInventory().getSize(), targetView.title());
        } else {
            inventory = Bukkit.createInventory(spectator, targetView.getType(), targetView.title());
        }

        final InventoryView view = spectator.openInventory(inventory);
        final SyncedInventory entry = new ReplicaSyncedInventory(spectator, view, targetView);

        this.inventories.put(spectator.getUniqueId(), entry);
    }

    private void tick() {
        for (final SyncedInventory entry : this.inventories.values()) {
            if (entry instanceof ReplicaSyncedInventory) {
                entry.spectatorView.getTopInventory().setContents(entry.targetView.getTopInventory().getContents());
            }
        }
    }

    private static class SyncedInventory {
        public final Player spectator;
        public final InventoryView spectatorView;
        public final InventoryView targetView;

        private SyncedInventory(Player spectator, InventoryView spectatorView, InventoryView targetView) {
            this.spectator = spectator;
            this.spectatorView = spectatorView;
            this.targetView = targetView;
        }

        private void close() {
            this.spectator.closeInventory();
        }
    }

    private static class ReplicaSyncedInventory extends SyncedInventory {
        private ReplicaSyncedInventory(Player spectator, InventoryView spectatorView, InventoryView targetView) {
            super(spectator, spectatorView, targetView);
        }
    }
}
