package com.hpfxd.spectatorplus.fabric.client.mixin;

import com.hpfxd.spectatorplus.fabric.client.sync.screen.ScreenSyncController;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin {
    @ModifyConstant(
            method = "<init>(Lnet/minecraft/world/entity/player/Inventory;ZLnet/minecraft/world/entity/player/Player;)V",
            constant = @Constant(intValue = 0, ordinal = 0)
    )
    private static int spectatorplus$modifySyncedWindowId(int constant) {
        if (ScreenSyncController.isPendingOpen) {
            return ScreenSyncController.syncedWindowId;
        }
        return constant;
    }
}
