package com.hpfxd.spectatorplus.paper.sync.handler.screen;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.Nullable;

public abstract sealed class SyncedScreen permits SyncedContainer, SyncedPlayerInventory {
    protected final Player spectator;
    protected InventoryView spectatorView;

    public SyncedScreen(Player spectator) {
        this.spectator = spectator;
    }

    public void open() {
        this.spectatorView = this.openToSpectator();
    }

    public void close() {
        this.spectator.closeInventory();
        this.spectatorView = null;
    }

    public void update() {
    }

    public abstract @Nullable Inventory getBottomInventory();

    protected abstract InventoryView openToSpectator();

    /**
     * Whether this screen is requested by the client to be opened (typically by pressing the inventory keybind).
     *
     * @return {@code true} if this screen is requested by the client to be opened.
     */
    public abstract boolean isRequestedByClient();

    /**
     * Whether this screen requires the SpectatorPlus client mod installed to open.
     * <p>
     * This is usually required for special inventory types that would look silly or just not work otherwise. An example
     * of this would be for player inventories, which the mod handles because servers cannot open this screen.
     *
     * @return {@code true} if this screen requires the SpectatorPlus client mod installed to open.
     */
    public boolean requiresClientMod() {
        return false;
    }

    public boolean isSurvivalInventory() {
        return false;
    }

    public boolean hasCraftingSlots() {
        return false;
    }

    public Player getSpectator() {
        return this.spectator;
    }

    public InventoryView getSpectatorView() {
        return this.spectatorView;
    }
}
