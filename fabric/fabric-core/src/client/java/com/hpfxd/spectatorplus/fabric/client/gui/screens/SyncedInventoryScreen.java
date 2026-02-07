package com.hpfxd.spectatorplus.fabric.client.gui.screens;

import com.hpfxd.spectatorplus.fabric.client.mixin.screen.AbstractRecipeBookScreenAccessor;
import com.hpfxd.spectatorplus.fabric.client.mixin.screen.ImageButtonAccessor;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class SyncedInventoryScreen extends InventoryScreen {
    public SyncedInventoryScreen(Player player) {
        super(player);
        this.syncOtherItems();
    }

    @Override
    public void containerTick() {
        super.containerTick();

        this.syncOtherItems();

        final RecipeBookComponent<?> recipeBookComponent = ((AbstractRecipeBookScreenAccessor) this).getRecipeBookComponent();
        if (recipeBookComponent.isVisible()) {
            recipeBookComponent.toggleVisibility();
        }
    }

    private void syncOtherItems() {
        // If the mixin worked correctly, this.menu should be a SyncedInventoryMenu
        if (this.menu instanceof SyncedInventoryMenu syncedMenu) {
            final Inventory inventory = syncedMenu.getInventory();

            // Use synced inventory data for all slots (main, armor, offhand)
            var syncData = com.hpfxd.spectatorplus.fabric.client.sync.ClientSyncController.syncData;
            if (syncData != null && syncData.screen != null && syncData.screen.inventoryItems != null) {
                var items = syncData.screen.inventoryItems;
                for (int i = 0; i < items.size() && i < 41; i++) {
                    inventory.setItem(i, items.get(i));
                }
            }
        }
    }

    @Override
    protected <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget) {
        if (widget instanceof final ImageButton button) {
            final WidgetSprites sprites = ((ImageButtonAccessor) button).getSprites();

            if (sprites == RecipeBookComponent.RECIPE_BUTTON_SPRITES) {
                // skip adding recipe book button
                return widget;
            }
        }

        return super.addRenderableWidget(widget);
    }
}
