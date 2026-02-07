package com.hpfxd.spectatorplus.fabric.client.mixin.screen;

import com.hpfxd.spectatorplus.fabric.sync.packet.ServerboundOpenedInventorySyncPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {

    @Inject(method = "onClose()V", at = @At("HEAD"))
    private void spectatorplus$onScreenClose(CallbackInfo ci) {
        // Check if this is an inventory or container screen being closed
        if ((Object) this instanceof InventoryScreen || (Object) this instanceof AbstractContainerScreen) {
            // Notify server that we closed our inventory/container
            if (ClientPlayNetworking.canSend(ServerboundOpenedInventorySyncPacket.TYPE)) {
                ClientPlayNetworking.send(new ServerboundOpenedInventorySyncPacket(false));
            }
        }
    }
}
