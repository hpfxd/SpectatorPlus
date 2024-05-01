package com.hpfxd.spectatorplus.fabric.client.mixin.screen;

import com.hpfxd.spectatorplus.fabric.client.sync.screen.ScreenSyncController;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {
    @Shadow @Final public int containerId;
    @Shadow public abstract Slot getSlot(int slotId);

    @Inject(
            method = "setItem(IILnet/minecraft/world/item/ItemStack;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void spectatorplus$cancelSetItem(int slotId, int stateId, ItemStack stack, CallbackInfo ci) {
        if (ScreenSyncController.syncedWindowId == this.containerId && ScreenSyncController.syncedInventory != null) {
            final Slot slot = this.getSlot(slotId);

            if (slot.container == ScreenSyncController.syncedInventory) {
                // Cancel if the slot is in our fake inventory
                ci.cancel();
            }
        }
    }

    @WrapWithCondition(
            method = "initializeContents(ILjava/util/List;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/Slot;set(Lnet/minecraft/world/item/ItemStack;)V")
    )
    private boolean spectatorplus$cancelInitializeContents(Slot instance, ItemStack stack) {
        // Only set slot if the container is not our fake inventory
        return instance.container != ScreenSyncController.syncedInventory;
    }
}
