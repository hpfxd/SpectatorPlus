package com.hpfxd.spectatorplus.fabric.sync.packet;

import com.hpfxd.spectatorplus.fabric.sync.ServerboundSyncPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public final class ServerboundOpenedInventorySyncPacket implements ServerboundSyncPacket {
    public static final StreamCodec<FriendlyByteBuf, ServerboundOpenedInventorySyncPacket> STREAM_CODEC = CustomPacketPayload.codec(ServerboundOpenedInventorySyncPacket::write, ServerboundOpenedInventorySyncPacket::new);
    public static final CustomPacketPayload.Type<ServerboundOpenedInventorySyncPacket> TYPE = CustomPacketPayload.createType("spectatorplus:opened_inventory_sync");

    public ServerboundOpenedInventorySyncPacket() {
    }

    public ServerboundOpenedInventorySyncPacket(FriendlyByteBuf buf) {
    }

    public void write(FriendlyByteBuf buf) {
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
