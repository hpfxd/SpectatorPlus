package com.hpfxd.spectatorplus.paper.sync.handler.screen;

import com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent;
import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent;
import com.hpfxd.spectatorplus.paper.SpectatorPlugin;
import com.hpfxd.spectatorplus.paper.sync.handler.InventorySyncHandler;
import com.hpfxd.spectatorplus.paper.sync.packet.ClientboundInventorySyncPacket;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class ScreenSyncHandler implements Listener {
    private static final String PERMISSION = "spectatorplus.sync.screen";

    private final SpectatorPlugin plugin;
    private final Map<UUID, SyncedScreen> screens = new HashMap<>();

    private boolean ignoreInventoryEvents;

    public ScreenSyncHandler(SpectatorPlugin plugin) {
        this.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 0, 0);
    }

    private void tick() {
        for (final SyncedScreen screen : this.screens.values()) {
            try {
                screen.update();
            } catch (Exception e) {
                this.plugin.getSLF4JLogger().warn("An exception occurred while updating a synced screen for \"{}\"", screen.spectator.getName(), e);
            }
        }
    }

    public void updatePlayerInventory(Player player, ItemStack[] inventorySendSlots) {
        for (final Player spectator : this.plugin.getSyncController().getSpectators(player, InventorySyncHandler.INVENTORY_PERMISSION)) {
            final SyncedScreen screen = this.screens.get(spectator.getUniqueId());

            if (screen != null) {
                // need to create a packet per-spectator as the containerId is likely to be different
                this.plugin.getSyncController().sendPacket(spectator, new ClientboundInventorySyncPacket(player.getUniqueId(), screen.containerId, inventorySendSlots));
            }
        }
    }

    public boolean isViewingSyncedScreen(HumanEntity spectator) {
        return this.screens.containsKey(spectator.getUniqueId());
    }

    private void openSyncedContainer(Player spectator, InventoryView targetView) {
        // todo MERCHANT

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
                this.openSyncedDirectContainer(spectator, targetView);
                break;
            case WORKBENCH:
            case LOOM:
            case STONECUTTER:
            case GRINDSTONE:
            case SMITHING:
            case ANVIL:
            case ENDER_CHEST:
            case ENCHANTING:
            case CARTOGRAPHY:
                this.openSyncedReplicaContainer(spectator, targetView);
                break;
            case CRAFTING:
                this.openSyncedCraftingContainer(spectator, targetView);
                break;
        }
    }
    
    private void openPlayerInventory(Player spectator, Player target) {
        final InventoryView spectatorView = spectator.openInventory(SyncedPlayerInventory.getDummyInventory());
        final SyncedScreen screen = new SyncedPlayerInventory(spectator, spectatorView, target.getInventory());
        
        this.setScreen(spectator, screen);
    }

    private void openSyncedDirectContainer(Player spectator, InventoryView targetView) {
        final InventoryView spectatorView = spectator.openInventory(targetView.getTopInventory());
        final SyncedScreen screen = new DirectSyncedContainer(spectator, spectatorView, targetView);

        this.setScreen(spectator, screen);
    }

    private void openSyncedReplicaContainer(Player spectator, InventoryView targetView) {
        final Inventory replicaInventory = ReplicaSyncedContainer.createReplicaInventory(spectator, targetView);
        final InventoryView spectatorView = spectator.openInventory(replicaInventory);
        final SyncedScreen screen = new ReplicaSyncedContainer(spectator, spectatorView, targetView);

        this.setScreen(spectator, screen);
    }

    private void openSyncedCraftingContainer(Player spectator, InventoryView targetView) {
        final Inventory replicaInventory = CraftingSyncedContainer.createReplicaInventory(spectator, targetView);
        final InventoryView spectatorView = spectator.openInventory(replicaInventory);
        final SyncedScreen screen = new CraftingSyncedContainer(spectator, spectatorView, targetView);

        this.setScreen(spectator, screen);
    }
    
    private void setScreen(Player spectator, SyncedScreen screen) {
        this.screens.put(spectator.getUniqueId(), screen);

        // todo send init screen packet

        if (spectator.hasPermission(InventorySyncHandler.INVENTORY_PERMISSION)) {
            if (screen.getBottomInventory() instanceof final PlayerInventory inventory) {
                // Sync the bottom inventory with the spectator (the target's inventory).
                this.plugin.getSyncController().getInventorySyncHandler().sendInventory(spectator, inventory, screen.containerId);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onOpen(InventoryOpenEvent event) {
        if (this.ignoreInventoryEvents) {
            return;
        }

        try {
            this.ignoreInventoryEvents = true;

            for (final Player spectator : this.plugin.getSyncController().getSpectators((Player) event.getPlayer(), PERMISSION)) {
                final InventoryType currentInventoryType = spectator.getOpenInventory().getTopInventory().getType();

                // if the player currently has an inventory screen already open, we want to skip opening this new inventory
                if (currentInventoryType == InventoryType.CRAFTING || currentInventoryType == InventoryType.CREATIVE) {
                    // unless, their current inventory is already a synced one, which is okay to replace
                    if (!this.screens.containsKey(spectator.getUniqueId())) {
                        continue;
                    }
                }

                this.openSyncedContainer(spectator, event.getView());
            }
        } finally {
            this.ignoreInventoryEvents = false;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClose(InventoryCloseEvent event) {
        if (this.ignoreInventoryEvents) {
            return;
        }

        try {
            this.ignoreInventoryEvents = true;

            for (final Iterator<SyncedScreen> it = this.screens.values().iterator(); it.hasNext(); ) {
                final SyncedScreen screen = it.next();

                if (screen.spectator.equals(event.getPlayer())) {
                    // event is being called for a spectator closing a synced inventory themself, so just remove the entry
                    it.remove();
                } else if (screen instanceof SyncedContainer syncedContainer && syncedContainer.targetView.equals(event.getView())) {
                    // event is being called for a target closing an inventory, and this entry is for a spectator who is
                    // currently viewing it. so close the inventory for the spectator, and remove the entry
                    screen.close();
                    it.remove();
                }
            }
        } finally {
            this.ignoreInventoryEvents = false;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStartSpectating(PlayerStartSpectatingEntityEvent event) {
        final Player spectator = event.getPlayer();

        final SyncedScreen screen = this.screens.remove(spectator.getUniqueId());
        if (screen != null) {
            screen.close();
        }

        if (event.getNewSpectatorTarget() instanceof final Player target && spectator.hasPermission(PERMISSION)) {
            this.openSyncedContainer(spectator, target.getOpenInventory());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStopSpectating(PlayerStopSpectatingEntityEvent event) {
        final Player spectator = event.getPlayer();

        final SyncedScreen screen = this.screens.remove(spectator.getUniqueId());
        if (screen != null) {
            screen.close();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClick(InventoryClickEvent event) {
        if (this.isViewingSyncedScreen(event.getWhoClicked())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClick(InventoryDragEvent event) {
        if (this.isViewingSyncedScreen(event.getWhoClicked())) {
            event.setCancelled(true);
        }
    }
}
