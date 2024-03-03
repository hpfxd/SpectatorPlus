package com.hpfxd.spectatorplus.fabric.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
    @Shadow @Final private Minecraft minecraft;

    @Redirect(method = "getMarkerParticleTarget()Lnet/minecraft/world/level/block/Block;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;getPlayerMode()Lnet/minecraft/world/level/GameType;"))
    private GameType spectatorplus$useCameraGameModeForMarkerParticleCheck(MultiPlayerGameMode instance) {
        if (this.minecraft.cameraEntity instanceof Player player && player.isCreative()) {
            return GameType.CREATIVE;
        }

        return instance.getPlayerMode();
    }

    @Redirect(method = "getMarkerParticleTarget()Lnet/minecraft/world/level/block/Block;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack spectatorplus$useCameraForMarkerParticleCheck(LocalPlayer instance) {
        if (this.minecraft.cameraEntity instanceof LivingEntity livingEntity) {
            return livingEntity.getMainHandItem();
        }
        return ItemStack.EMPTY;
    }
}
