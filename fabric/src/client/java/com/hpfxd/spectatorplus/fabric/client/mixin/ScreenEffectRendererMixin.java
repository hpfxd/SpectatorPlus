package com.hpfxd.spectatorplus.fabric.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererMixin {
    // Redirect calls from mc.player to the current camera entity

    @Redirect(
            method = "renderScreenEffect(Lnet/minecraft/client/Minecraft;Lcom/mojang/blaze3d/vertex/PoseStack;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSpectator()Z")
    )
    private static boolean spectatorplus$modifyIsSpectator(LocalPlayer instance, @Local(argsOnly = true) Minecraft mc) {
        return mc.getCameraEntity().isSpectator();
    }

    @Redirect(
            method = "renderScreenEffect(Lnet/minecraft/client/Minecraft;Lcom/mojang/blaze3d/vertex/PoseStack;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z")
    )
    private static boolean spectatorplus$modifyIsEyeInFluid(LocalPlayer instance, TagKey<Fluid> tagKey, @Local(argsOnly = true) Minecraft mc) {
        return mc.getCameraEntity().isEyeInFluid(tagKey);
    }

    @Redirect(
            method = "renderScreenEffect(Lnet/minecraft/client/Minecraft;Lcom/mojang/blaze3d/vertex/PoseStack;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isOnFire()Z")
    )
    private static boolean spectatorplus$modifyIsOnFire(LocalPlayer instance, @Local(argsOnly = true) Minecraft mc) {
        return mc.getCameraEntity().isOnFire();
    }
}
