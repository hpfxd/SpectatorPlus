package com.hpfxd.spectatorplus.paper.sync.handler.screen;

import com.hpfxd.spectatorplus.paper.SpectatorPlugin;
import com.hpfxd.spectatorplus.paper.util.ReflectionUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

public abstract sealed class SyncedScreen permits SyncedContainer, SyncedPlayerInventory {
    protected final Player spectator;
    protected final InventoryView spectatorView;
    /**
     * The internal container ID number Minecraft assigned to the {@link #spectatorView}.
     * <p>
     * This is used to ensure the client knows which screen the server is talking about when sending container sync
     * packets. If this ID is not able to be obtained (via reflection), it will be {@code -1}, and the client will
     * accept the packets anyway.
     */
    protected final int containerId;

    public SyncedScreen(Player spectator, InventoryView spectatorView) {
        this.spectator = spectator;
        this.spectatorView = spectatorView;

        int containerId = -1;
        try {
            containerId = ReflectionUtil.getContainerId(spectatorView);
        } catch (Exception e) {
            JavaPlugin.getPlugin(SpectatorPlugin.class).getSLF4JLogger().warn("Failed to retrieve Container ID for synced inventory", e);
        }

        this.containerId = containerId;
    }

    public void close() {
        this.spectator.closeInventory();
    }

    public void update() {
    }

    public abstract @Nullable Inventory getBottomInventory();

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

    public Player getSpectator() {
        return this.spectator;
    }

    public InventoryView getSpectatorView() {
        return this.spectatorView;
    }
}
