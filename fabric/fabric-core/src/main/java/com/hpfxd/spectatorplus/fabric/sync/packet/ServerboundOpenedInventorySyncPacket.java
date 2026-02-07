package com.hpfxd.spectatorplus.fabric.sync.packet;

import com.hpfxd.spectatorplus.fabric.sync.ServerboundSyncPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public final class ServerboundOpenedInventorySyncPacket implements ServerboundSyncPacket {
    public static final StreamCodec<FriendlyByteBuf, ServerboundOpenedInventorySyncPacket> STREAM_CODEC = CustomPacketPayload.codec(ServerboundOpenedInventorySyncPacket::write, ServerboundOpenedInventorySyncPacket::new);
    public static final CustomPacketPayload.Type<ServerboundOpenedInventorySyncPacket> TYPE = new CustomPacketPayload.Type<>(Identifier.parse("spectatorplus:opened_inventory_sync"));

    private final boolean isOpened;

    public ServerboundOpenedInventorySyncPacket(boolean isOpened) {
        this.isOpened = isOpened;
    }

    public ServerboundOpenedInventorySyncPacket(FriendlyByteBuf buf) {
        this.isOpened = buf.readBoolean();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(this.isOpened);
    }

    public boolean isOpened() {
        return this.isOpened;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
