package com.hpfxd.spectatorplus.fabric.client.sync.screen;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public class ScreenSyncData {
    public NonNullList<ItemStack> inventoryItems;

    public boolean isSurvivalInventory;
    public boolean isClientRequested;
    public boolean hasDummySlots;

    public ItemStack cursorItem = ItemStack.EMPTY;
    public int cursorItemSlot = -1;
}
