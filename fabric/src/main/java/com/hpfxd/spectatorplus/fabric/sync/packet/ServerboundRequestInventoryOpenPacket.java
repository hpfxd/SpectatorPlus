package com.hpfxd.spectatorplus.fabric.sync.packet;

import com.hpfxd.spectatorplus.fabric.sync.ServerboundSyncPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record ServerboundRequestInventoryOpenPacket(
        UUID playerId
) implements ServerboundSyncPacket {
    public static final PacketType<ServerboundRequestInventoryOpenPacket> TYPE = PacketType.create(new ResourceLocation("spectatorplus", "request_inventory_open"), ServerboundRequestInventoryOpenPacket::new);

    public ServerboundRequestInventoryOpenPacket(FriendlyByteBuf buf) {
        this(buf.readUUID());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(this.playerId);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
