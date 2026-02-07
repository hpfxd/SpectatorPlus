package com.hpfxd.spectatorplus.fabric.sync.packet;

import com.hpfxd.spectatorplus.fabric.sync.ClientboundSyncPacket;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundHotbarSyncPacket.PERMISSION;

public record ClientboundSelectedSlotSyncPacket(
        UUID playerId,
        int selectedSlot
) implements ClientboundSyncPacket {
    public static final StreamCodec<FriendlyByteBuf, ClientboundSelectedSlotSyncPacket> STREAM_CODEC = CustomPacketPayload.codec(ClientboundSelectedSlotSyncPacket::write, ClientboundSelectedSlotSyncPacket::new);
    public static final CustomPacketPayload.Type<ClientboundSelectedSlotSyncPacket> TYPE = new CustomPacketPayload.Type<>(Identifier.parse("spectatorplus:selected_slot_sync"));

    public static ClientboundSelectedSlotSyncPacket initializing(ServerPlayer target) {
        return new ClientboundSelectedSlotSyncPacket(target.getUUID(), target.getInventory().getSelectedSlot());
    }

    public ClientboundSelectedSlotSyncPacket(FriendlyByteBuf buf) {
        this(buf.readUUID(), buf.readByte());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(this.playerId);
        buf.writeByte(this.selectedSlot);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean canSend(ServerPlayer receiver) {
        return Permissions.check(receiver, PERMISSION, true);
    }
}
