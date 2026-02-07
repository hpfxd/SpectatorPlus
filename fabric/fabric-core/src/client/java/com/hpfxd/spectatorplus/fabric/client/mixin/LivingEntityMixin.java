package com.hpfxd.spectatorplus.fabric.client.mixin;

import com.hpfxd.spectatorplus.fabric.client.util.EffectUtil;
import com.hpfxd.spectatorplus.fabric.client.util.SpecUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.Holder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "swing(Lnet/minecraft/world/InteractionHand;)V", at = @At("TAIL"))
    private void spectatorplus$resetSpectatedAttackStrength(InteractionHand hand, CallbackInfo ci) {
        final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(Minecraft.getInstance());
        if ((Object) this == spectated) {
            if (!this.isBreakingBlock() && !this.isLookingAtBlock()) {
                spectated.resetAttackStrengthTicker();
            }
        }
    }

    @Unique
    private boolean isBreakingBlock() {
        return ((LevelRendererAccessor) Minecraft.getInstance().levelRenderer).getDestroyingBlocks().containsKey(this.getId());
    }

    @Unique
    private boolean isLookingAtBlock() {
        if (!(((Object) this) instanceof final Player player)) {
            return false;
        }

        var spectated = SpecUtil.getCameraPlayer(Minecraft.getInstance());
        var blockRange = spectated == null ? player.blockInteractionRange() : spectated.blockInteractionRange();
        var entityRange = spectated == null ? player.entityInteractionRange() : spectated.entityInteractionRange();
        return LocalPlayerAccessor.invokePick(this, blockRange, entityRange, 1F).getType() == HitResult.Type.BLOCK;
    }

    @Redirect(method = {"hasEffect", "getEffect", "getActiveEffects", "tickEffects"},
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;activeEffects:Ljava/util/Map;"))
    private Map<Holder<MobEffect>, MobEffectInstance> spectatorplus$redirectActiveEffects(LivingEntity instance) {
        // 只对玩家且满足条件时才重定向
        if (instance.level().isClientSide() && instance instanceof Player && EffectUtil.shouldUseSpectatorData()) {
            return EffectUtil.getActiveEffectsMap();
        }
        return ((LivingEntityAccessor) instance).spectatorplus$getActiveEffects();
    }

}
