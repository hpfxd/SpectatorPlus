package com.hpfxd.spectatorplus.fabric.mixin;

import com.hpfxd.spectatorplus.fabric.sync.ServerSyncController;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundSelectedSlotSyncPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
    @Shadow public ServerPlayer player;

    @Inject(method = "handleSetCarriedItem(Lnet/minecraft/network/protocol/game/ServerboundSetCarriedItemPacket;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Inventory;selected:I", opcode = Opcodes.PUTFIELD))
    private void spectatorplus$syncSelectedSlot(ServerboundSetCarriedItemPacket packet, CallbackInfo ci) {
        ServerSyncController.broadcastPacketToSpectators(this.player, new ClientboundSelectedSlotSyncPacket(this.player.getUUID(), packet.getSlot()));
    }
}
