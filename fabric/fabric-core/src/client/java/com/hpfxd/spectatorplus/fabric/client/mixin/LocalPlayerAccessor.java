package com.hpfxd.spectatorplus.fabric.client.mixin;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LocalPlayer.class)
public interface LocalPlayerAccessor {
    @Invoker
    static HitResult invokePick(Entity cameraEntity, double blockRange, double entityRange, float partialTick) {
        throw new AssertionError();
    }
}