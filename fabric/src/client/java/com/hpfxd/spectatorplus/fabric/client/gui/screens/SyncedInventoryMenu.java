package com.hpfxd.spectatorplus.fabric.client.gui.screens;

import com.hpfxd.spectatorplus.fabric.client.sync.ClientSyncController;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;

public class SyncedInventoryMenu extends InventoryMenu {
    private static final Container DUMMY_CONTAINER_NO_CRAFTING = new DummyContainer(9);
    private static final Container DUMMY_CONTAINER_CRAFTING = new DummyContainer(4);

    private final Player owner;
    private final Inventory inventory;

    public SyncedInventoryMenu(Inventory playerInventory, boolean active, Player owner) {
        super(playerInventory, active, owner);
        this.owner = owner;
        this.inventory = playerInventory;
    }

    public Player getOwner() {
        return this.owner;
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    @Override
    protected @NotNull Slot addSlot(Slot slot) {
        if (slot.container == this.getCraftSlots() && ClientSyncController.syncData.screen.hasDummySlots) {
            if (!ClientSyncController.syncData.screen.isClientRequested) {
                final Slot s = super.addSlot(slot);
                if (slot.index == 4) {
                    // Add dummy slots after craft slots, since this inventory doesn't exist on the server side and is actually
                    // just a normal 9-slot generic menu. These slots pad the menu so the slot numbers sync up.
                    for (int i = 0; i < DUMMY_CONTAINER_CRAFTING.getContainerSize(); i++) {
                        super.addSlot(new DummyInventorySlot(DUMMY_CONTAINER_CRAFTING, i));
                    }
                }
                return s;
            } else {
                if (slot.getContainerSlot() == 3) {
                    // Add dummy slots. Since the crafting slots were not added, we need more slots to compensate.
                    for (int i = 0; i < DUMMY_CONTAINER_NO_CRAFTING.getContainerSize(); i++) {
                        super.addSlot(new DummyInventorySlot(DUMMY_CONTAINER_NO_CRAFTING, i));
                    }
                }

                // return here without adding the slot
                return slot;
            }
        }

        return super.addSlot(slot);
    }
}
