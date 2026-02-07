package com.hpfxd.spectatorplus.fabric.client.sync.screen;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class ScreenSyncData {
    /**
     * inventoryItems layout:
     *   0-35: main inventory (including hotbar)
     *   36-39: armor (helmet, chestplate, leggings, boots)
     */
    public NonNullList<ItemStack> inventoryItems;

    /**
     * Container items for non-inventory screens
     */
    public NonNullList<ItemStack> containerItems;

    /**
     * Container type for determining proper GUI rendering
     */
    public MenuType<?> containerType;

    /**
     * Container size for dynamic containers like chests
     */
    public int containerSize;

    public boolean isSurvivalInventory;
    public boolean isClientRequested;
    public boolean hasDummySlots;

    public ItemStack cursorItem = ItemStack.EMPTY;
    public int cursorItemSlot = -1;

    /**
     * Initialize or resize container items to the specified size
     * @param size the desired size of the container
     */
    public void initializeContainerItems(int size) {
        if (containerItems == null || containerItems.size() != size) {
            containerItems = NonNullList.withSize(size, ItemStack.EMPTY);
        }
    }

    /**
     * Update container item at the specified slot
     * @param slot the slot index
     * @param item the item to set (null means skip this slot)
     */
    public void updateContainerItem(int slot, ItemStack item) {
        if (containerItems != null && slot >= 0 && slot < containerItems.size() && item != null) {
            containerItems.set(slot, item.isEmpty() ? ItemStack.EMPTY : item);
        }
    }

    /**
     * Update multiple container items from an array
     * @param items array of items to update (null items are skipped)
     */
    public void updateContainerItems(ItemStack[] items) {
        if (containerItems == null || items == null) {
            return;
        }
        for (int i = 0; i < items.length && i < containerItems.size(); i++) {
            updateContainerItem(i, items[i]);
        }
    }

    /**
     * Populate a Container with the synced container items
     * @param container the container to populate
     */
    public void populateContainer(Container container) {
        if (containerItems == null || container == null) {
            return;
        }
        for (int i = 0; i < containerItems.size() && i < container.getContainerSize(); i++) {
            container.setItem(i, containerItems.get(i));
        }
    }

    /**
     * Check if container items are initialized
     * @return true if containerItems is not null
     */
    public boolean hasContainerItems() {
        return containerItems != null;
    }

    /**
     * Initialize or resize inventory items to the specified size
     * @param size the desired size of the inventory
     */
    public void initializeInventoryItems(int size) {
        if (inventoryItems == null || inventoryItems.size() != size) {
            inventoryItems = NonNullList.withSize(size, ItemStack.EMPTY);
        }
    }

    /**
     * Update inventory item at the specified slot
     * @param slot the slot index
     * @param item the item to set
     */
    public void updateInventoryItem(int slot, ItemStack item) {
        if (inventoryItems != null && slot >= 0 && slot < inventoryItems.size()) {
            inventoryItems.set(slot, item);
        }
    }

    /**
     * Populate an Inventory with the synced inventory items
     * @param inventory the inventory to populate
     */
    public void populateInventory(Inventory inventory) {
        if (inventoryItems == null || inventory == null) {
            return;
        }
        for (int i = 0; i < inventoryItems.size(); i++) {
            inventory.setItem(i, inventoryItems.get(i));
        }
    }

    /**
     * Check if inventory items are initialized
     * @return true if inventoryItems is not null
     */
    public boolean hasInventoryItems() {
        return inventoryItems != null;
    }
}
