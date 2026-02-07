package com.hpfxd.spectatorplus.fabric.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemInHandRenderer.class)
public interface ItemInHandRendererAccessor {
    @Invoker("renderArmWithItem")
    void invokeRenderArmWithItem(AbstractClientPlayer player, float partialTick, float pitch, InteractionHand hand,
                                  float swingProgress, ItemStack item, float equippedProgress, PoseStack poseStack,
                                  SubmitNodeCollector nodeCollector, int packedLight);

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

    @Accessor
    ItemModelResolver getItemModelResolver();

    @Invoker("isChargedCrossbow")
    static boolean invokeIsChargedCrossbow(ItemStack stack) {
        throw new AssertionError();
    }
}
