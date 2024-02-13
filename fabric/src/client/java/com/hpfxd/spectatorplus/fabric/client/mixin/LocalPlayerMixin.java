package com.hpfxd.spectatorplus.fabric.client.mixin;

import com.hpfxd.spectatorplus.fabric.client.sync.ClientPositionSyncTransmitter;
import com.hpfxd.spectatorplus.fabric.sync.PositionEntry;
import com.hpfxd.spectatorplus.fabric.sync.packet.ServerboundPositionsSyncPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.player.LocalPlayer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @Inject(method = "sendPosition()V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;yRotLast:F", opcode = Opcodes.PUTFIELD))
    private void spectatorplus$transmitPositionSync(CallbackInfo ci) {
        if (ClientPositionSyncTransmitter.transmitPositions) {
            final PositionEntry[] positions = ClientPositionSyncTransmitter.popPositions();

            ClientPlayNetworking.send(new ServerboundPositionsSyncPacket(positions));
        }
    }
}
