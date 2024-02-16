package com.hpfxd.spectatorplus.fabric.client.mixin;

import com.hpfxd.spectatorplus.fabric.client.util.SpecUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Inject(method = "destroyBlockProgress(ILnet/minecraft/core/BlockPos;I)V", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;remove(I)Ljava/lang/Object;", remap = false))
    private void spectatorplus$resetAttackCooldownOnDigFinish(int breakerId, BlockPos pos, int progress, CallbackInfo ci) {
        if (progress == -1) {
            final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer(Minecraft.getInstance());
            if (spectated != null && spectated.getId() == breakerId) {
                if (((LevelRendererAccessor) this).getDestroyingBlocks().containsKey(breakerId)) {
                    spectated.resetAttackStrengthTicker();
                }
            }
        }
    }
}
