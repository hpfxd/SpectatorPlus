package com.hpfxd.spectatorplus.fabric.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChunkMap.TrackedEntity.class)
public abstract class TrackedEntityMixin {
    @Shadow @Final Entity entity;

    @ModifyVariable(method = "updatePlayer(Lnet/minecraft/server/level/ServerPlayer;)V", at = @At("STORE"), index = 10)
    private boolean spectatorplus$fixCameraTeleport(boolean b, @Local(ordinal = 0, argsOnly = true) ServerPlayer player) {
        // Make entities always visible for spectators, even when teleported far away (MC-107113)

        return b || player.getCamera() == this.entity;
    }
}
