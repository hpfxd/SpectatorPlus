package com.hpfxd.spectatorplus.fabric.client.mixin;

import com.hpfxd.spectatorplus.fabric.client.SpectatorClientMod;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public class EntityMixin {
    @ModifyExpressionValue(method = "isInvisibleTo(Lnet/minecraft/world/entity/player/Player;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isSpectator()Z"))
    private boolean spectatorplus$configShowInvisibleEntities(boolean original, @Local(argsOnly = true) Player player) {
        if (player instanceof LocalPlayer && !SpectatorClientMod.config.showInvisibleEntities) {
            return false;
        }

        return original;
    }
}
