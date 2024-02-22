package com.hpfxd.spectatorplus.fabric.client.mixin;

import com.hpfxd.spectatorplus.fabric.client.SpectatorClientMod;
import com.hpfxd.spectatorplus.fabric.client.sync.ClientSyncController;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
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

    @ModifyExpressionValue(
            method = "shouldEntityAppearGlowing(Lnet/minecraft/world/entity/Entity;)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z")
    )
    private boolean spectatorplus$hideHighlightOtherSpectators(boolean original, @Local(argsOnly = true) Entity entity) {
        if (!SpectatorClientMod.config.highlightSpectators && entity instanceof final Player player && player.isSpectator()) {
            return false;
        }
        return original;
    }
}
