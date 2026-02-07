package com.hpfxd.spectatorplus.fabric.client.mixin;

import com.google.common.base.MoreObjects;
import com.hpfxd.spectatorplus.fabric.client.SpectatorClientMod;
import com.hpfxd.spectatorplus.fabric.client.util.SpecUtil;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.ClientAvatarState;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private LightTexture lightTexture;
    @Shadow @Final private RenderBuffers renderBuffers;
    @Shadow @Final public ItemInHandRenderer itemInHandRenderer;

    @Unique private float bob;
    @Unique private float bobO;
    @Unique private float walkDist;
    @Unique private float walkDistO;

    @Unique private float xBob;
    @Unique private float yBob;
    @Unique private float xBobO;
    @Unique private float yBobO;

//    @Redirect(method = "renderItemInHand", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Matrix4fc;)V", ordinal = 0))
//    private void redirectMulPose(PoseStack poseStack, Matrix4fc matrix) {
//        // In spectator mode, the projection matrix contains rotation data from the spectated player,
//        // while arm rendering calculations are based on the localPlayer's viewpoint.
//        // We must skip this mulPose operation when spectating to avoid rendering arms with incorrect orientation.
//        if (this.minecraft.player == null
//                || this.minecraft.player.gameMode() != GameType.SPECTATOR
//                || !this.minecraft.options.getCameraType().isFirstPerson()) {
//            poseStack.mulPose(matrix);
//        }
//    }

    @Inject(method = "renderItemInHand", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4fStack;popMatrix()Lorg/joml/Matrix4fStack;", remap = false))
    public void spectatorplus$renderItemInHand(float partialTicks, boolean sleeping, Matrix4f projectionMatrix, CallbackInfo ci, @Local PoseStack poseStackIn) {
        if (SpectatorClientMod.config.renderArms && this.minecraft.player != null && this.minecraft.options.getCameraType().isFirstPerson() && !this.minecraft.options.hideGui) {
            final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
            if (spectated != null && !spectated.isSpectator()) {
                //this.lightTexture.turnOnLightLayer();

                float attackAnim = spectated.getAttackAnim(partialTicks);
                final InteractionHand interactionHand = MoreObjects.firstNonNull(spectated.swingingArm, InteractionHand.MAIN_HAND);
                float pitch = Mth.lerp(partialTicks, spectated.xRotO, spectated.getXRot());

                poseStackIn.mulPose(Axis.XP.rotationDegrees((spectated.getViewXRot(partialTicks) - Mth.lerp(partialTicks, this.xBobO, this.xBob)) * 0.1F));
                poseStackIn.mulPose(Axis.YP.rotationDegrees(Mth.degreesDifference(Mth.lerp(partialTicks, this.yBobO, this.yBob), Mth.rotLerp(partialTicks, spectated.yRotO, spectated.getYRot())) * 0.1F));

                final ItemInHandRenderer.HandRenderSelection handRenderSelection = evaluateWhichHandsToRender(spectated);
                final int packedLightCoords = this.minecraft.getEntityRenderDispatcher().getPackedLightCoords(spectated, partialTicks);

                final ItemInHandRendererAccessor accessor = ((ItemInHandRendererAccessor) this.itemInHandRenderer);
                var submitNodeCollector = minecraft.gameRenderer.getSubmitNodeStorage();

                if (handRenderSelection.renderMainHand) {
                    final float swingProgress = interactionHand == InteractionHand.MAIN_HAND ? attackAnim : 0.0F;
                    final float equippedProgress = accessor.getItemModelResolver().swapAnimationScale(accessor.getMainHandItem()) * (1F - Mth.lerp(partialTicks, accessor.getOMainHandHeight(), accessor.getMainHandHeight()));

                        accessor.invokeRenderArmWithItem(spectated, partialTicks,
                            pitch, InteractionHand.MAIN_HAND, swingProgress, accessor.getMainHandItem(), equippedProgress,
                            poseStackIn, submitNodeCollector, packedLightCoords);
                }

                if (handRenderSelection.renderOffHand) {
                    final float swingProgress = interactionHand == InteractionHand.OFF_HAND ? attackAnim : 0.0F;
                    final float equippedProgress = accessor.getItemModelResolver().swapAnimationScale(accessor.getOffHandItem()) * (1F - Mth.lerp(partialTicks, accessor.getOOffHandHeight(), accessor.getOffHandHeight()));

                        accessor.invokeRenderArmWithItem(spectated, partialTicks,
                            pitch, InteractionHand.OFF_HAND, swingProgress, accessor.getOffHandItem(), equippedProgress,
                            poseStackIn, submitNodeCollector, packedLightCoords);
                }

                //this.lightTexture.turnOffLightLayer();
                this.minecraft.gameRenderer.getFeatureRenderDispatcher().renderAllFeatures();
                this.renderBuffers.bufferSource().endBatch();
            }
        }
    }

    @Unique
    private static ItemInHandRenderer.HandRenderSelection evaluateWhichHandsToRender(AbstractClientPlayer player) {
        // see ItemInHandRenderer#evaluateWhichHandsToRender
        // could not just call into the original method due to the argument being LocalPlayer

        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack offHandItem = player.getOffhandItem();
        boolean bl = mainHandItem.is(Items.BOW) || offHandItem.is(Items.BOW);
        boolean bl2 = mainHandItem.is(Items.CROSSBOW) || offHandItem.is(Items.CROSSBOW);
        if (!bl && !bl2) {
            return ItemInHandRenderer.HandRenderSelection.RENDER_BOTH_HANDS;
        } else if (player.isUsingItem()) {
            ItemStack itemStack = player.getUseItem();
            InteractionHand interactionHand = player.getUsedItemHand();
            if (!itemStack.is(Items.BOW) && !itemStack.is(Items.CROSSBOW)) {
                return interactionHand == InteractionHand.MAIN_HAND && ItemInHandRendererAccessor.invokeIsChargedCrossbow(player.getOffhandItem())
                        ? ItemInHandRenderer.HandRenderSelection.RENDER_MAIN_HAND_ONLY
                        : ItemInHandRenderer.HandRenderSelection.RENDER_BOTH_HANDS;
            } else {
                return ItemInHandRenderer.HandRenderSelection.onlyForHand(interactionHand);
            }
        } else {
            return ItemInHandRendererAccessor.invokeIsChargedCrossbow(mainHandItem)
                    ? ItemInHandRenderer.HandRenderSelection.RENDER_MAIN_HAND_ONLY
                    : ItemInHandRenderer.HandRenderSelection.RENDER_BOTH_HANDS;
        }
    }

    @Inject(method = "tick()V", at = @At("HEAD"))
    private void spectatorplus$tick(CallbackInfo ci) {
        final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
        if (spectated != null) {
            // View bobbing

            // get horizontal distance between current and last pos
            final Vec3 pos = spectated.position().with(Direction.Axis.Y, 0);
            final Vec3 posO = spectated.getPosition(0F).with(Direction.Axis.Y, 0);
            final float deltaMovement = (float) posO.distanceTo(pos);

            this.walkDistO = this.walkDist;
            this.walkDist += deltaMovement * 0.6F;

            this.bobO = this.bob;
            if (spectated.isPassenger() || !spectated.onGround() || spectated.isDeadOrDying() || spectated.isSwimming()) {
                this.bob = 0.0F;
            } else {
                this.bob += (Math.min(0.1F, deltaMovement) - this.bob) * 0.4F;
            }

            // Hand swaying

            this.yBobO = this.yBob;
            this.xBobO = this.xBob;
            this.xBob += Mth.degreesDifference(this.xBob, spectated.getXRot()) * 0.5F;
            this.yBob += Mth.degreesDifference(this.yBob, spectated.getYRot()) * 0.5F;
        } else {
            this.bob = this.bobO = this.walkDist = this.walkDistO = this.xBob = this.yBob = this.xBobO = this.yBobO = 0;
        }
    }

    @Redirect(method = "bobView", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/ClientAvatarState;getBackwardsInterpolatedWalkDistance(F)F"))
    float spectatorplus$modifyBobWalkDist(ClientAvatarState instance, float partialTick) {
        if (minecraft.getCameraEntity() == this.minecraft.player) return instance.getBackwardsInterpolatedWalkDistance(partialTick);
        float f = this.walkDist - this.walkDistO;
        return -(this.walkDist + f * partialTick);
    }

    @Redirect(method = "bobView", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/ClientAvatarState;getInterpolatedBob(F)F"))
    float spectatorplus$modifyBobValue(ClientAvatarState instance, float partialTick) {
        if (minecraft.getCameraEntity() == this.minecraft.player) return instance.getInterpolatedBob(partialTick);
        return Mth.lerp(partialTick, this.bobO, this.bob);
    }
}
