package com.hpfxd.spectatorplus.fabric.sync.packet;

import com.hpfxd.spectatorplus.fabric.sync.ClientboundSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.PositionEntry;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public record ClientboundPositionsSyncPacket(
        UUID playerId,
        PositionEntry[] entries
) implements ClientboundSyncPacket {
    public static final PacketType<ClientboundPositionsSyncPacket> TYPE = PacketType.create(new ResourceLocation("spectatorplus", "positions_sync"), ClientboundPositionsSyncPacket::new);
    private static final String PERMISSION = "spectatorplus.sync.positions";

    public ClientboundPositionsSyncPacket(FriendlyByteBuf buf) {
        this(buf.readUUID(), PositionEntry.readEntries(buf));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(this.playerId);
        PositionEntry.writeEntries(buf, this.entries);
    }

    @Override
    public PacketType<ClientboundPositionsSyncPacket> getType() {
        return TYPE;
    }

    @Override
    public boolean canSend(ServerPlayer receiver) {
        return Permissions.check(receiver, PERMISSION, true);
    }
}
