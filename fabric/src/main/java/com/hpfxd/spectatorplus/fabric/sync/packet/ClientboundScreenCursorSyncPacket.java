package com.hpfxd.spectatorplus.fabric.sync.packet;

import com.hpfxd.spectatorplus.fabric.sync.ClientboundSyncPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record ClientboundScreenCursorSyncPacket(
        UUID playerId,
        ItemStack cursor,
        int originSlot
) implements ClientboundSyncPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundScreenCursorSyncPacket> STREAM_CODEC = CustomPacketPayload.codec(ClientboundScreenCursorSyncPacket::write, ClientboundScreenCursorSyncPacket::new);
    public static final CustomPacketPayload.Type<ClientboundScreenCursorSyncPacket> TYPE = CustomPacketPayload.createType("spectatorplus:screen_cursor_sync");

    public ClientboundScreenCursorSyncPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readUUID(), ItemStack.OPTIONAL_STREAM_CODEC.decode(buf), buf.readByte());
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(this.playerId);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, this.cursor);
        buf.writeByte(this.originSlot);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean canSend(ServerPlayer receiver) {
        return true;
    }
}
