package com.hpfxd.spectatorplus.fabric.client.gui.screens;

import com.hpfxd.spectatorplus.fabric.client.mixin.ImageButtonAccessor;
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

        if (this.getRecipeBookComponent().isVisible()) {
            this.getRecipeBookComponent().toggleVisibility();
        }
    }

    private void syncOtherItems() {
        final SyncedInventoryMenu menu = (SyncedInventoryMenu) this.menu;

        final Inventory playerInventory = menu.getOwner().getInventory();
        final Inventory fakeInventory = menu.getInventory();

        for (int slot = 0; slot < playerInventory.armor.size(); slot++) {
            fakeInventory.armor.set(slot, playerInventory.armor.get(slot));
        }

        fakeInventory.offhand.set(0, playerInventory.offhand.get(0));
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
