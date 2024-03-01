package com.hpfxd.spectatorplus.paper.sync.handler.screen;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

public final class SyncedPlayerInventory extends SyncedScreen {
    private static Inventory DUMMY_INVENTORY;

    private final PlayerInventory targetInventory;

    public SyncedPlayerInventory(Player spectator, PlayerInventory targetInventory) {
        super(spectator);
        this.targetInventory = targetInventory;
    }

    @Override
    public @NotNull PlayerInventory getBottomInventory() {
        return this.targetInventory;
    }

    @Override
    protected InventoryView openToSpectator() {
        return this.spectator.openInventory(getDummyInventory());
    }

    @Override
    public boolean isRequestedByClient() {
        return true;
    }

    @Override
    public boolean requiresClientMod() {
        // If a client doesn't have the mod installed, all would will see is an empty inventory.
        return true;
    }

    @Override
    public boolean isSurvivalInventory() {
        return true;
    }

    private static Inventory getDummyInventory() {
        if (DUMMY_INVENTORY == null) {
            DUMMY_INVENTORY = Bukkit.createInventory(null, 9, Component.translatable("container.inventory"));
        }

        return DUMMY_INVENTORY;
    }
}
