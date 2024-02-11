package com.hpfxd.spectatorplus.fabric.client.mixin;

import com.hpfxd.spectatorplus.fabric.client.sync.ClientSyncController;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "setCameraEntity(Lnet/minecraft/world/entity/Entity;)V", at = @At(value = "TAIL"))
    private void spectatorplus$resetSyncDataOnCameraSwitch(Entity viewingEntity, CallbackInfo ci) {
        if (ClientSyncController.syncData != null && !ClientSyncController.syncData.playerId.equals(viewingEntity.getUUID())) {
            ClientSyncController.syncData = null;
        }
    }
}
