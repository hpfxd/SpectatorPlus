package com.hpfxd.spectatorplus.fabric.client.mixin;

import com.google.common.base.MoreObjects;
import com.hpfxd.spectatorplus.fabric.client.util.SpecUtil;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"), method = "renderItemInHand")
    public void spectatorplus$renderItemInHand(PoseStack poseStackIn, Camera activeRenderInfoIn, float partialTicks, CallbackInfo ci) {
        if (this.minecraft.player != null && this.minecraft.options.getCameraType().isFirstPerson() && !this.minecraft.options.hideGui) {
            final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
            if (spectated != null) {
                this.lightTexture.turnOnLightLayer();

                float attackAnim = spectated.getAttackAnim(partialTicks);
                final InteractionHand interactionHand = MoreObjects.firstNonNull(spectated.swingingArm, InteractionHand.MAIN_HAND);
                float pitch = Mth.lerp(partialTicks, spectated.xRotO, spectated.getXRot());

                final ItemInHandRenderer.HandRenderSelection handRenderSelection = evaluateWhichHandsToRender(spectated);
                final int packedLightCoords = this.minecraft.getEntityRenderDispatcher().getPackedLightCoords(this.minecraft.player, partialTicks);

                if (handRenderSelection.renderMainHand) {
                    final float swingProgress = interactionHand == InteractionHand.MAIN_HAND ? attackAnim : 0.0F;
                    final float equippedProgress = 1F - Mth.lerp(partialTicks,
                            ((ItemInHandRendererAccessor) this.itemInHandRenderer).getOMainHandHeight(),
                            ((ItemInHandRendererAccessor) this.itemInHandRenderer).getMainHandHeight());

                    ((ItemInHandRendererAccessor) this.itemInHandRenderer).invokeRenderArmWithItem(spectated, partialTicks,
                            pitch, InteractionHand.MAIN_HAND, swingProgress, spectated.getMainHandItem(), equippedProgress,
                            poseStackIn, this.renderBuffers.bufferSource(), packedLightCoords);
                }

                if (handRenderSelection.renderOffHand) {
                    final float swingProgress = interactionHand == InteractionHand.OFF_HAND ? attackAnim : 0.0F;
                    final float equippedProgress = 1F - Mth.lerp(partialTicks,
                            ((ItemInHandRendererAccessor) this.itemInHandRenderer).getOOffHandHeight(),
                            ((ItemInHandRendererAccessor) this.itemInHandRenderer).getOffHandHeight());

                    ((ItemInHandRendererAccessor) this.itemInHandRenderer).invokeRenderArmWithItem(spectated, partialTicks,
                            pitch, InteractionHand.OFF_HAND, swingProgress, spectated.getOffhandItem(), equippedProgress,
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

    @ModifyExpressionValue(method = "pick(F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;getPickRange()F"))
    private float spectatorplus$modifyPickRange(float original) {
        final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
        if (spectated != null) {
            return Player.getPickRange(spectated.isCreative());
        }

        return original;
    }

    @ModifyExpressionValue(method = "pick(F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;hasFarPickRange()Z"))
    private boolean spectatorplus$modifyHasFarPickRange(boolean original) {
        final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
        if (spectated != null) {
            return spectated.isCreative();
        }

        return original;
    }
}
