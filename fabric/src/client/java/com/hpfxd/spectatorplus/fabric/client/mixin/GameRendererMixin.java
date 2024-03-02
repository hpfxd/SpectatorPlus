package com.hpfxd.spectatorplus.fabric.client.mixin;

import com.google.common.base.MoreObjects;
import com.hpfxd.spectatorplus.fabric.client.SpectatorClientMod;
import com.hpfxd.spectatorplus.fabric.client.util.SpecUtil;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow @Final Minecraft minecraft;
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

    @Inject(method = "renderItemInHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/Camera;F)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"))
    public void spectatorplus$renderItemInHand(PoseStack poseStackIn, Camera activeRenderInfoIn, float partialTicks, CallbackInfo ci) {
        if (SpectatorClientMod.config.renderArms && this.minecraft.player != null && this.minecraft.options.getCameraType().isFirstPerson() && !this.minecraft.options.hideGui) {
            final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
            if (spectated != null && !spectated.isSpectator()) {
                this.lightTexture.turnOnLightLayer();

                float attackAnim = spectated.getAttackAnim(partialTicks);
                final InteractionHand interactionHand = MoreObjects.firstNonNull(spectated.swingingArm, InteractionHand.MAIN_HAND);
                float pitch = Mth.lerp(partialTicks, spectated.xRotO, spectated.getXRot());

                poseStackIn.mulPose(Axis.XP.rotationDegrees((spectated.getViewXRot(partialTicks) - Mth.lerp(partialTicks, this.xBobO, this.xBob)) * 0.1F));
                poseStackIn.mulPose(Axis.YP.rotationDegrees(Mth.degreesDifference(Mth.lerp(partialTicks, this.yBobO, this.yBob), Mth.rotLerp(partialTicks, spectated.yRotO, spectated.getYRot())) * 0.1F));

                final ItemInHandRenderer.HandRenderSelection handRenderSelection = evaluateWhichHandsToRender(spectated);
                final int packedLightCoords = this.minecraft.getEntityRenderDispatcher().getPackedLightCoords(spectated, partialTicks);

                final ItemInHandRendererAccessor accessor = ((ItemInHandRendererAccessor) this.itemInHandRenderer);

                if (handRenderSelection.renderMainHand) {
                    final float swingProgress = interactionHand == InteractionHand.MAIN_HAND ? attackAnim : 0.0F;
                    final float equippedProgress = 1F - Mth.lerp(partialTicks, accessor.getOMainHandHeight(), accessor.getMainHandHeight());

                    accessor.invokeRenderArmWithItem(spectated, partialTicks,
                            pitch, InteractionHand.MAIN_HAND, swingProgress, accessor.getMainHandItem(), equippedProgress,
                            poseStackIn, this.renderBuffers.bufferSource(), packedLightCoords);
                }

                if (handRenderSelection.renderOffHand) {
                    final float swingProgress = interactionHand == InteractionHand.OFF_HAND ? attackAnim : 0.0F;
                    final float equippedProgress = 1F - Mth.lerp(partialTicks, accessor.getOOffHandHeight(), accessor.getOffHandHeight());

                    accessor.invokeRenderArmWithItem(spectated, partialTicks,
                            pitch, InteractionHand.OFF_HAND, swingProgress, accessor.getOffHandItem(), equippedProgress,
                            poseStackIn, this.renderBuffers.bufferSource(), packedLightCoords);
                }

                this.lightTexture.turnOffLightLayer();
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

    @ModifyExpressionValue(method = "bobView(Lcom/mojang/blaze3d/vertex/PoseStack;F)V", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Player;walkDist:F"))
    private float spectatorplus$modifyBobWalkDist(float original, @Local Player cameraPlayer) {
        return cameraPlayer == this.minecraft.player ? original : this.walkDist;
    }

    @ModifyExpressionValue(method = "bobView(Lcom/mojang/blaze3d/vertex/PoseStack;F)V", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Player;walkDistO:F"))
    private float spectatorplus$modifyBobWalkDistO(float original, @Local Player cameraPlayer) {
        return cameraPlayer == this.minecraft.player ? original : this.walkDistO;
    }

    @ModifyExpressionValue(method = "bobView(Lcom/mojang/blaze3d/vertex/PoseStack;F)V", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Player;bob:F"))
    private float spectatorplus$modifyBobValue(float original, @Local Player cameraPlayer) {
        return cameraPlayer == this.minecraft.player ? original : this.bob;
    }

    @ModifyExpressionValue(method = "bobView(Lcom/mojang/blaze3d/vertex/PoseStack;F)V", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Player;oBob:F"))
    private float spectatorplus$modifyBobValueO(float original, @Local Player cameraPlayer) {
        return cameraPlayer == this.minecraft.player ? original : this.bobO;
    }

    @ModifyExpressionValue(method = "pick(F)V", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/player/LocalPlayer;blockInteractionRange()D"))
    private double spectatorplus$modifyBlockInteractionRange(double original) {
        final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
        if (spectated != null) {
            return spectated.blockInteractionRange();
        }

        return original;
    }

    @ModifyExpressionValue(method = "pick(F)V", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/player/LocalPlayer;entityInteractionRange()D"))
    private double spectatorplus$modifyEntityInteractionRange(double original) {
        final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
        if (spectated != null) {
            return spectated.entityInteractionRange();
        }

        return original;
    }
}
