package com.hpfxd.spectatorplus.fabric.client.mixin;

import com.hpfxd.spectatorplus.fabric.client.util.SpecUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @Shadow @Final private Minecraft minecraft;
    @Shadow private float mainHandHeight;
    @Shadow private float oMainHandHeight;
    @Shadow private float oOffHandHeight;
    @Shadow private float offHandHeight;
    @Shadow private ItemStack mainHandItem;
    @Shadow private ItemStack offHandItem;

    @Unique private AbstractClientPlayer spectated;

    @Inject(at = @At("HEAD"), method = "tick", cancellable = true)
    public void spectatorplus$fixSpectatorHandHeight(CallbackInfo ci) {
        final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
        if (spectated != null) {
            ci.cancel();

            this.oMainHandHeight = this.mainHandHeight;
            this.oOffHandHeight = this.offHandHeight;

            final ItemStack mainHandItem = spectated.getMainHandItem();
            final ItemStack offHandItem = spectated.getOffhandItem();

            if (ItemStack.matches(this.mainHandItem, mainHandItem)) {
                this.mainHandItem = mainHandItem;
            }

            if (ItemStack.matches(this.offHandItem, offHandItem)) {
                this.offHandItem = offHandItem;
            }

            if (this.spectated == spectated) {
                float f = spectated.getAttackStrengthScale(1.0F);
                float g = this.mainHandItem != mainHandItem ? 0.0F : f * f * f;
                float h = this.offHandItem != offHandItem ? 0.0F : 1.0F;
                this.mainHandHeight = this.mainHandHeight + Mth.clamp(g - this.mainHandHeight, -0.4F, 0.4F);
                this.offHandHeight = this.offHandHeight + Mth.clamp(h - this.offHandHeight, -0.4F, 0.4F);

                if (this.mainHandHeight < 0.1F) {
                    this.mainHandItem = mainHandItem;
                }

                if (this.offHandHeight < 0.1F) {
                    this.offHandItem = offHandItem;
                }
            } else {
                // this is the first tick of spectating a new player

                this.spectated = spectated;
                this.mainHandHeight = 1F;
                this.offHandHeight = 1F;
                this.mainHandItem = mainHandItem;
                this.offHandItem = offHandItem;
            }
        } else {
            this.spectated = null;
        }
    }

    @ModifyVariable(method = "renderPlayerArm(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;IFFLnet/minecraft/world/entity/HumanoidArm;)V", at = @At("STORE"))
    private AbstractClientPlayer setArmPlayer(AbstractClientPlayer in) {
        // render the arm as the camera entity, instead of always as the client player

        final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
        if (spectated != null) {
            return spectated;
        }

        return in;
    }

    @Redirect(
            method = "renderMapHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/world/entity/HumanoidArm;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;getPlayerRenderer(Lnet/minecraft/client/player/AbstractClientPlayer;)Lnet/minecraft/client/renderer/entity/player/AvatarRenderer;")
    )
    private AvatarRenderer<AbstractClientPlayer> spectatorplus$mapHandUseCameraAvatarRenderer(net.minecraft.client.renderer.entity.EntityRenderDispatcher dispatcher, AbstractClientPlayer ignoredPlayer) {
        Entity cameraEntity = this.minecraft.getCameraEntity();
        if (cameraEntity instanceof AbstractClientPlayer cameraPlayer) {
            return dispatcher.getPlayerRenderer(cameraPlayer);
        }
        return dispatcher.getPlayerRenderer(this.minecraft.player);
    }

        @Redirect(
            method = "renderMapHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/world/entity/HumanoidArm;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isModelPartShown(Lnet/minecraft/world/entity/player/PlayerModelPart;)Z")
        )
        private boolean spectatorplus$mapHandFixPartVisibility(LocalPlayer instance, PlayerModelPart playerModelPart) {
        Entity cameraEntity = this.minecraft.getCameraEntity();
        if (cameraEntity instanceof Player cameraPlayer) {
            return cameraPlayer.isModelPartShown(playerModelPart);
        }
        return instance.isModelPartShown(playerModelPart);
    }

        @Redirect(method = {
            "renderOneHandedMap(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;IFLnet/minecraft/world/entity/HumanoidArm;FLnet/minecraft/world/item/ItemStack;)V",
            "renderTwoHandedMap(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;IFFF)V",
        }, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isInvisible()Z"))
        private boolean spectatorplus$spectatedInvisibility(LocalPlayer instance) {
        Entity cameraEntity = this.minecraft.getCameraEntity();
        if (cameraEntity instanceof net.minecraft.world.entity.Entity entity) {
            return entity.isInvisible();
        }
        return instance.isInvisible();
    }
}
