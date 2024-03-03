package com.hpfxd.spectatorplus.fabric.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemInHandRenderer.class)
public interface ItemInHandRendererAccessor {
    @Invoker("renderArmWithItem")
    void invokeRenderArmWithItem(AbstractClientPlayer player, float partialTicks, float pitch, InteractionHand hand,
                                  float swingProgress, ItemStack stack, float equippedProgress, PoseStack poseStack,
                                  MultiBufferSource buffer, int combinedLight);

    @Accessor
    float getMainHandHeight();

    @Accessor
    float getOffHandHeight();

    @Accessor
    float getOMainHandHeight();

    @Accessor
    float getOOffHandHeight();

    @Accessor
    ItemStack getMainHandItem();

    @Accessor
    ItemStack getOffHandItem();

    @Invoker("isChargedCrossbow")
    static boolean invokeIsChargedCrossbow(ItemStack stack) {
        throw new AssertionError();
    }
}
