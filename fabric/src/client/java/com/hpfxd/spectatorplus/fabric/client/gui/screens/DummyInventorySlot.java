package com.hpfxd.spectatorplus.fabric.client.gui.screens;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class DummyInventorySlot extends Slot {
    public DummyInventorySlot(Container container, int slot) {
        super(container, slot, 0, 0);
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }
}
