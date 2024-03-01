package com.hpfxd.spectatorplus.fabric.client.gui.screens;

import net.minecraft.world.item.ItemStack;

public class ItemMoveAnimation {
    public final int fromSlot;
    public final int toSlot;
    public final ItemStack itemStack;
    public int tick;

    public ItemMoveAnimation(int fromSlot, int toSlot, ItemStack itemStack) {
        this.fromSlot = fromSlot;
        this.toSlot = toSlot;
        this.itemStack = itemStack;
    }
}
