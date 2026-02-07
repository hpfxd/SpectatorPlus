package com.hpfxd.spectatorplus.fabric.mixin;

import com.hpfxd.spectatorplus.fabric.sync.handler.EffectsSyncHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "onEffectAdded", at = @At("TAIL"))
    private void onEffectAdded(MobEffectInstance effectInstance, @Nullable Entity entity, CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayer player) {
            EffectsSyncHandler.onEffectChanged(player);
        }
    }

    @Inject(method = "onEffectUpdated", at = @At("TAIL"))
    private void onEffectUpdated(MobEffectInstance effectInstance, boolean forced, @Nullable Entity entity, CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayer player) {
            EffectsSyncHandler.onEffectChanged(player);
        }
    }

    @Inject(method = "onEffectsRemoved", at = @At("TAIL"))
    private void onEffectsRemoved(Collection<MobEffectInstance> effects, CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayer player) {
            EffectsSyncHandler.onEffectChanged(player);
        }
    }
}