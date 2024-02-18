package com.hpfxd.spectatorplus.paper.sync.handler.screen;

import com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent;
import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent;
import com.hpfxd.spectatorplus.paper.SpectatorPlugin;
import com.hpfxd.spectatorplus.paper.sync.packet.ClientboundInventorySyncPacket;
import com.hpfxd.spectatorplus.paper.sync.packet.ClientboundScreenSyncPacket;
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
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import static com.hpfxd.spectatorplus.paper.sync.handler.InventorySyncHandler.INVENTORY_PERMISSION;

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
        this.plugin.getSyncController().sendPacket(this.plugin.getSyncController().getSpectators(player, eligible -> {
            // eligible spectator must have permission and viewing a synced screen to receive packet
            return eligible.hasPermission(INVENTORY_PERMISSION) && this.isViewingSyncedScreen(eligible);
        }), new ClientboundInventorySyncPacket(player.getUniqueId(), inventorySendSlots));
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
            case CREATIVE:
                this.openSyncedCraftingContainer(spectator, targetView);
                break;
        }
    }

    public void onRequestOpen(Player spectator, Player target) {
        if (spectator.hasPermission(INVENTORY_PERMISSION)) {
            this.openPlayerInventory(spectator, target);
        }

        // TODO maybe send a message if the player doesn't have permission?
    }

    public void onPlayerOpenInventory(Player target) {
        try {
            this.ignoreInventoryEvents = true;

            final InventoryView view = target.getOpenInventory();

            if (view.getType() == InventoryType.CRAFTING || view.getType() == InventoryType.CREATIVE) {
                for (final Player spectator : this.plugin.getSyncController().getSpectators(target, PERMISSION)) {
                    if (!this.canOverrideSpectatorView(spectator, spectator.getOpenInventory())) {
                        continue;
                    }
                    this.openSyncedContainer(spectator, view);
                }
            }
        } finally {
            this.ignoreInventoryEvents = false;
        }
    }
    
    private void openPlayerInventory(Player spectator, Player target) {
        final SyncedScreen screen = new SyncedPlayerInventory(spectator, target.getInventory());
        
        this.setScreen(spectator, screen);
    }

    private void openSyncedDirectContainer(Player spectator, InventoryView targetView) {
        final SyncedScreen screen = new DirectSyncedContainer(spectator, targetView);

        this.setScreen(spectator, screen);
    }

    private void openSyncedReplicaContainer(Player spectator, InventoryView targetView) {
        final SyncedScreen screen = new ReplicaSyncedContainer(spectator, targetView);

        this.setScreen(spectator, screen);
    }

    private void openSyncedCraftingContainer(Player spectator, InventoryView targetView) {
        final SyncedScreen screen = new CraftingSyncedContainer(spectator, targetView);

        this.setScreen(spectator, screen);
    }
    
    private void setScreen(Player spectator, SyncedScreen screen) {
        final boolean hasClientMod = spectator.getListeningPluginChannels().contains(ClientboundScreenSyncPacket.ID.asString());

        if (!hasClientMod && (this.plugin.getServerConfig().screensRequireClientMod || screen.requiresClientMod())) {
            // The spectator doesn't have the client mod installed and isn't allowed to open this screen.
            return;
        }

        this.screens.put(spectator.getUniqueId(), screen);

        this.plugin.getSyncController().sendPacket(spectator, ClientboundScreenSyncPacket.of(spectator.getSpectatorTarget().getUniqueId(), screen.isSurvivalInventory(), screen.isRequestedByClient()));

        if (spectator.hasPermission(INVENTORY_PERMISSION)) {
            if (screen.getBottomInventory() instanceof final PlayerInventory inventory) {
                // Sync the bottom inventory with the spectator (the target's inventory).
                this.plugin.getSyncController().getInventorySyncHandler().sendInventory(spectator, inventory);
            }
        }

        screen.open();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onOpen(InventoryOpenEvent event) {
        if (this.ignoreInventoryEvents) {
            return;
        }

        try {
            this.ignoreInventoryEvents = true;

            for (final Player spectator : this.plugin.getSyncController().getSpectators((Player) event.getPlayer(), PERMISSION)) {
                // if the player currently has an inventory screen already open, we want to skip opening this new inventory
                if (!this.canOverrideSpectatorView(spectator, spectator.getOpenInventory())) {
                    continue;
                }

                this.openSyncedContainer(spectator, event.getView());
            }
        } finally {
            this.ignoreInventoryEvents = false;
        }
    }

    private boolean canOverrideSpectatorView(HumanEntity spectator, InventoryView view) {
        final SyncedScreen screen = this.screens.get(spectator.getUniqueId());
        if (screen != null && screen.isRequestedByClient()) {
            return false;
        }

        return view.getType() == InventoryType.CRAFTING || view.getType() == InventoryType.CREATIVE;
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
            final InventoryView view = target.getOpenInventory();

            // Only open if the current view is not CRAFTING or CREATIVE
            if (view.getType() != InventoryType.CRAFTING && view.getType() != InventoryType.CREATIVE) {
                this.openSyncedContainer(spectator, view);
            }
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
