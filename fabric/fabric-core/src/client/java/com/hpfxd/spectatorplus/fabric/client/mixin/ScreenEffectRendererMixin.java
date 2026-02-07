package com.hpfxd.spectatorplus.fabric.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// Redirect calls from mc.player to the current camera entity

@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererMixin {
    @Shadow @Final private Minecraft minecraft;

    @Redirect(method = "renderScreenEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSpectator()Z"))
    private boolean spectatorplus$modifyIsSpectator(LocalPlayer instance) {
        return this.minecraft.getCameraEntity().isSpectator();
    }

    @Redirect(method = "renderScreenEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean spectatorplus$modifyIsEyeInFluid(LocalPlayer instance, TagKey<Fluid> tagKey) {
        return this.minecraft.getCameraEntity().isEyeInFluid(tagKey);
    }

    @Redirect(method = "renderScreenEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isOnFire()Z"))
    private boolean spectatorplus$modifyIsOnFire(LocalPlayer instance) {
        return this.minecraft.getCameraEntity().isOnFire();
    }
}
