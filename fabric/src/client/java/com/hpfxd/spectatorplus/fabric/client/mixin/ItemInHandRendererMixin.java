package com.hpfxd.spectatorplus.fabric.client.mixin;

import com.hpfxd.spectatorplus.fabric.client.util.SpecUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
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

            float f = spectated.getAttackStrengthScale(1.0F);
            this.mainHandHeight += Mth.clamp((this.mainHandItem == mainHandItem ? f * f * f : 0.0F) - this.mainHandHeight, -0.4F, 0.4F);
            this.offHandHeight += Mth.clamp((float) (this.offHandItem == offHandItem ? 1 : 0) - this.offHandHeight, -0.4F, 0.4F);

            if (this.mainHandHeight < 0.1F) {
                this.mainHandItem = mainHandItem;
            }

            if (this.offHandHeight < 0.1F) {
                this.offHandItem = offHandItem;
            }
        }
    }

    @ModifyVariable(method = "renderPlayerArm", at = @At("STORE"))
    private AbstractClientPlayer setArmPlayer(AbstractClientPlayer in) {
        // render the arm as the camera entity, instead of always as the client player

        final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(this.minecraft);
        if (spectated != null) {
            return spectated;
        }

        return in;
    }
}
