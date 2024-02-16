package com.hpfxd.spectatorplus.fabric.client.mixin;

import com.hpfxd.spectatorplus.fabric.client.util.SpecUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
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

        // see GameRenderer#pick

        final float pickRange = Player.getPickRange(player.isCreative());
        final HitResult result = this.pick(pickRange, 1F, false);

        final Vec3 vec3 = this.getEyePosition(1F);
        final Vec3 vec32 = this.getViewVector(1F);
        final Vec3 vec33 = vec3.add(vec32.x * pickRange, vec32.y * pickRange, vec32.z * pickRange);
        final AABB aABB = this.getBoundingBox().expandTowards(vec32.scale(pickRange)).inflate(1, 1, 1);
        final double e = result != null ? result.getLocation().distanceToSqr(vec3) : pickRange * pickRange;

        final EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(this, vec3, vec33, aABB, ent -> !ent.isSpectator() && ent.isPickable(), e);

        return entityHitResult == null && result != null && result.getType() != HitResult.Type.MISS;
    }
}
