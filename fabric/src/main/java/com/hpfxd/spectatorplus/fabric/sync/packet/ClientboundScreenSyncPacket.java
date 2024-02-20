package com.hpfxd.spectatorplus.fabric.sync.packet;

import com.hpfxd.spectatorplus.fabric.sync.ClientboundSyncPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public record ClientboundScreenSyncPacket(
        UUID playerId,
        int flags
) implements ClientboundSyncPacket {
    public static final PacketType<ClientboundScreenSyncPacket> TYPE = PacketType.create(new ResourceLocation("spectatorplus", "screen_sync"), ClientboundScreenSyncPacket::new);

    public ClientboundScreenSyncPacket(FriendlyByteBuf buf) {
        this(buf.readUUID(), buf.readByte());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(this.playerId);
        buf.writeByte(this.flags);
    }

    public boolean isSurvivalInventory() {
        return (this.flags & 1) == 1;
    }

    public boolean isClientRequested() {
        return (this.flags >> 1 & 1) == 1;
    }

    public boolean hasDummySlots() {
        return (this.flags >> 2 & 1) == 1;
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
