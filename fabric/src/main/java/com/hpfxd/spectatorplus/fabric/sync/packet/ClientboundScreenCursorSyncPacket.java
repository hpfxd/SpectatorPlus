package com.hpfxd.spectatorplus.fabric.sync.packet;

import com.hpfxd.spectatorplus.fabric.sync.ClientboundSyncPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public record ClientboundScreenCursorSyncPacket(
        UUID playerId,
        ItemStack cursor,
        int originSlot
) implements ClientboundSyncPacket {
    public static final PacketType<ClientboundScreenCursorSyncPacket> TYPE = PacketType.create(new ResourceLocation("spectatorplus", "screen_cursor_sync"), ClientboundScreenCursorSyncPacket::new);

    public ClientboundScreenCursorSyncPacket(FriendlyByteBuf buf) {
        this(buf.readUUID(), ClientboundHotbarSyncPacket.readItem(buf), buf.readByte());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(this.playerId);
        ClientboundHotbarSyncPacket.writeItem(buf, this.cursor);
        buf.writeByte(this.originSlot);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    @Override
    public boolean canSend(ServerPlayer receiver) {
        return true;
    }
}
