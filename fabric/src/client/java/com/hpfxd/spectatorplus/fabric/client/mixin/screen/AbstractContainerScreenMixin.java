package com.hpfxd.spectatorplus.fabric.client.mixin.screen;

import com.hpfxd.spectatorplus.fabric.client.SpectatorClientMod;
import com.hpfxd.spectatorplus.fabric.client.gui.screens.ItemMoveAnimation;
import com.hpfxd.spectatorplus.fabric.client.sync.ClientSyncController;
import com.hpfxd.spectatorplus.fabric.client.sync.screen.ScreenSyncController;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {
    @Unique
    private static final int MOVE_ANIMATION_TICKS = 4;

    @Unique
    private final List<ItemMoveAnimation> animations = new ArrayList<>();

    @Unique
    private ItemStack cursorItem = ItemStack.EMPTY;
    @Unique
    private int cursorSlot = -1;

    @Unique
    private int originalMouseX = -1;
    @Unique
    private int originalMouseY = -1;
    @Unique
    private boolean mouseMoved;

    @Shadow protected abstract void renderFloatingItem(GuiGraphics guiGraphics, ItemStack stack, int x, int y, String text);
    @Shadow @Final protected AbstractContainerMenu menu;
    @Shadow @Nullable protected abstract Slot findSlot(double mouseX, double mouseY);
    @Shadow protected int leftPos;
    @Shadow protected int topPos;

    @Inject(
            method = "slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ClickType;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void spectatorplus$noClickingOnSyncedScreens(Slot slot, int slotId, int mouseButton, ClickType type, CallbackInfo ci) {
        if (this.spectatorplus$isSyncedScreen()) {
            ci.cancel();
        }
    }

    @Inject(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderLabels(Lnet/minecraft/client/gui/GuiGraphics;II)V",
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    private void spectatorplus$renderSyncedCursorItem(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!this.spectatorplus$isSyncedScreen()) {
            return;
        }

        if (this.originalMouseX == -1 && this.originalMouseY == -1) {
            this.originalMouseX = mouseX;
            this.originalMouseY = mouseY;
        }

        if (mouseX != this.originalMouseX && mouseY != this.originalMouseY) {
            this.mouseMoved = true;
        }

        final ItemStack cursorItem = ClientSyncController.syncData.screen.cursorItem;
        int cursorSlot = ClientSyncController.syncData.screen.cursorItemSlot;

        if (this.cursorItem != cursorItem) {
            if (cursorSlot != -1 && this.cursorSlot != -1 && this.cursorSlot != cursorSlot) {
                final ItemStack animationItem = this.cursorItem.isEmpty() ? cursorItem : this.cursorItem;

                if (!animationItem.isEmpty() && this.menu.isValidSlotIndex(this.cursorSlot) && this.menu.isValidSlotIndex(cursorSlot)) {
                    this.animations.add(new ItemMoveAnimation(this.cursorSlot, cursorSlot, animationItem));
                }
            }
        }

        this.cursorItem = cursorItem;
        this.cursorSlot = cursorItem.isEmpty() ? -1 : cursorSlot;

        for (final ItemMoveAnimation animation : this.animations) {
            final Slot fromSlot = this.menu.getSlot(animation.fromSlot);
            final Slot toSlot = this.menu.getSlot(animation.toSlot);

            final float delta = (animation.tick + partialTick) / (float) MOVE_ANIMATION_TICKS;
            final int cursorX = Math.round(Mth.lerp(delta, fromSlot.x, toSlot.x));
            final int cursorY = Math.round(Mth.lerp(delta, fromSlot.y, toSlot.y));

            this.spectatorplus$renderCursorItem(guiGraphics, animation.itemStack, cursorX, cursorY);
        }

        if (!this.cursorItem.isEmpty() && this.cursorSlot > 0 && this.menu.isValidSlotIndex(this.cursorSlot)) {
            final Slot slot = this.menu.getSlot(this.cursorSlot);
            this.spectatorplus$renderCursorItem(guiGraphics, this.cursorItem, slot.x, slot.y);
        }
    }

    @Unique
    private void spectatorplus$renderCursorItem(GuiGraphics guiGraphics, ItemStack stack, int cursorX, int cursorY) {
        final Slot hoverSlot = this.findSlot(cursorX + this.leftPos + 8, cursorY + this.topPos + 8);
        if (hoverSlot != null && hoverSlot.isHighlightable()) {
            AbstractContainerScreen.renderSlotHighlight(guiGraphics, hoverSlot.x, hoverSlot.y, 0);
        }
        this.renderFloatingItem(guiGraphics, stack, cursorX, cursorY, null);
    }

    @Inject(
            method = "tick()V",
            at = @At("TAIL")
    )
    private void spectatorplus$tickCursorItem(CallbackInfo ci) {
        if (!this.spectatorplus$isSyncedScreen()) {
            return;
        }

        this.animations.removeIf(animation -> ++animation.tick >= MOVE_ANIMATION_TICKS);
    }

    @ModifyExpressionValue(
            method = "renderTooltip(Lnet/minecraft/client/gui/GuiGraphics;II)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/Slot;hasItem()Z")
    )
    private boolean spectatorplus$hideTooltipUntilMouseMove(boolean original) {
        return original && (!this.spectatorplus$isSyncedScreen() || !SpectatorClientMod.config.hideTooltipUntilMouseMove || this.mouseMoved);
    }

    @ModifyExpressionValue(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/Slot;isHighlightable()Z", ordinal = 0)
    )
    private boolean spectatorplus$hideHoverUntilMoveMouse(boolean original) {
        return original && (!this.spectatorplus$isSyncedScreen() || !SpectatorClientMod.config.hideTooltipUntilMouseMove || this.mouseMoved);
    }

    @Unique
    private boolean spectatorplus$isSyncedScreen() {
        return (Object) this == ScreenSyncController.syncedScreen;
    }
}
