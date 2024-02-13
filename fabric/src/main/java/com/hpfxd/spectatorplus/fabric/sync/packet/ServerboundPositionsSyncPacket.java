package com.hpfxd.spectatorplus.fabric.sync.packet;

import com.hpfxd.spectatorplus.fabric.sync.PositionEntry;
import com.hpfxd.spectatorplus.fabric.sync.ServerboundSyncPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record ServerboundPositionsSyncPacket(
        PositionEntry[] entries
) implements ServerboundSyncPacket {
    public static final PacketType<ServerboundPositionsSyncPacket> TYPE = PacketType.create(new ResourceLocation("spectatorplus", "positions_sync"), ServerboundPositionsSyncPacket::new);

    public ServerboundPositionsSyncPacket(FriendlyByteBuf buf) {
        this(PositionEntry.readEntries(buf));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        PositionEntry.writeEntries(buf, this.entries);
    }

    @Override
    public PacketType<ServerboundPositionsSyncPacket> getType() {
        return TYPE;
    }
}
