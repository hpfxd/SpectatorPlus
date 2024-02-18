package com.hpfxd.spectatorplus.fabric.sync.packet;

import com.hpfxd.spectatorplus.fabric.sync.ServerboundSyncPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public final class ServerboundOpenedInventorySyncPacket implements ServerboundSyncPacket {
    public static final PacketType<ServerboundOpenedInventorySyncPacket> TYPE = PacketType.create(new ResourceLocation("spectatorplus", "opened_inventory_sync"), ServerboundOpenedInventorySyncPacket::new);

    public ServerboundOpenedInventorySyncPacket() {
    }

    public ServerboundOpenedInventorySyncPacket(FriendlyByteBuf buf) {
    }

    @Override
    public void write(FriendlyByteBuf buf) {
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
