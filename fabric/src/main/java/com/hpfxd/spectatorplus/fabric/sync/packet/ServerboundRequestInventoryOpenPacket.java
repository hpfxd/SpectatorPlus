package com.hpfxd.spectatorplus.fabric.sync.packet;

import com.hpfxd.spectatorplus.fabric.sync.ServerboundSyncPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record ServerboundRequestInventoryOpenPacket(
        UUID playerId
) implements ServerboundSyncPacket {
    public static final StreamCodec<FriendlyByteBuf, ServerboundRequestInventoryOpenPacket> STREAM_CODEC = CustomPacketPayload.codec(ServerboundRequestInventoryOpenPacket::write, ServerboundRequestInventoryOpenPacket::new);
    public static final CustomPacketPayload.Type<ServerboundRequestInventoryOpenPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.parse("spectatorplus:request_inventory_open"));

    public ServerboundRequestInventoryOpenPacket(FriendlyByteBuf buf) {
        this(buf.readUUID());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(this.playerId);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
