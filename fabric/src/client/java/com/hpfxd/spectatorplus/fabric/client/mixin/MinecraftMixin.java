package com.hpfxd.spectatorplus.fabric.client.mixin;

import com.hpfxd.spectatorplus.fabric.client.SpectatorClientMod;
import com.hpfxd.spectatorplus.fabric.client.sync.ClientSyncController;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.hpfxd.spectatorplus.fabric.client.util.SpecUtil;
import com.hpfxd.spectatorplus.fabric.sync.packet.ServerboundRequestInventoryOpenPacket;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "setCameraEntity(Lnet/minecraft/world/entity/Entity;)V", at = @At(value = "TAIL"))
    private void spectatorplus$resetSyncDataOnCameraSwitch(Entity viewingEntity, CallbackInfo ci) {
        if (ClientSyncController.syncData != null && !ClientSyncController.syncData.playerId.equals(viewingEntity.getUUID())) {
            ClientSyncController.setSyncData(null);
        }
    }

    @ModifyExpressionValue(
            method = "shouldEntityAppearGlowing(Lnet/minecraft/world/entity/Entity;)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z")
    )
    private boolean spectatorplus$hideHighlightOtherSpectators(boolean original, @Local(argsOnly = true) Entity entity) {
        if (!SpectatorClientMod.config.highlightSpectators && entity instanceof final Player player && player.isSpectator()) {
            return false;
        }
        return original;
    }

    /**
     * If the server has indicated the ability to accept the {@link ServerboundRequestInventoryOpenPacket}, we want to
     * send it instead of opening the client's inventory when pressing the inventory keybind and let the server handle
     * opening the inventory for us.
     */
    @WrapWithCondition(
            method = "handleKeybinds()V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", ordinal = 1)
    )
    private boolean spectatorplus$requestSpectatorInventoryOpen(Minecraft instance, Screen guiScreen) {
        // are we currently spectating a player?
        final AbstractClientPlayer spectated = SpecUtil.getCameraPlayer((Minecraft) (Object) this);
        if (spectated != null) {
            if (ClientPlayNetworking.canSend(ServerboundRequestInventoryOpenPacket.TYPE)) {
                // server has registered the ability to accept the packet, we want to cancel the original setScreen call
                // and send the packet to the server. if the server has not registered this packet, the mod will not
                // interfere with this key's normal operation.

                ClientPlayNetworking.send(new ServerboundRequestInventoryOpenPacket(spectated.getUUID()));
                return false;
            }
        }
        return true;
    }
}
