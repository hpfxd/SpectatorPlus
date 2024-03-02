package com.hpfxd.spectatorplus.fabric.client.mixin;

import com.hpfxd.spectatorplus.fabric.client.util.SpecUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.InteractionHand;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

        return ((GameRendererAccessor) Minecraft.getInstance().gameRenderer).invokePick(this, player.blockInteractionRange(), player.entityInteractionRange(), 1F).getType() == HitResult.Type.BLOCK;
    }
}
