package com.hpfxd.spectatorplus.fabric.client.sync;

import net.minecraft.core.NonNullList;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class ClientSyncData {
    public final UUID playerId;
    public final NonNullList<ItemStack> hotbarItems = NonNullList.withSize(9, ItemStack.EMPTY);
    public int selectedHotbarSlot = -1;
    public FoodData foodData;

    public float experienceProgress;
    public int experienceNeededForNextLevel;
    public int experienceLevel = -1;

    public ClientSyncData(UUID playerId) {
        this.playerId = playerId;
    }
}
