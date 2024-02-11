package com.hpfxd.spectatorplus.fabric.sync.packet;

import com.hpfxd.spectatorplus.fabric.sync.ClientboundSyncPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public record ClientboundSelectedSlotSyncPacket(
        UUID playerId,
        int selectedSlot
) implements ClientboundSyncPacket {
    public static final PacketType<ClientboundSelectedSlotSyncPacket> TYPE = PacketType.create(new ResourceLocation("spectatorplus", "selected_slot_sync"), ClientboundSelectedSlotSyncPacket::new);

    public static ClientboundSelectedSlotSyncPacket initializing(ServerPlayer target) {
        return new ClientboundSelectedSlotSyncPacket(target.getUUID(), target.getInventory().selected);
    }

    public ClientboundSelectedSlotSyncPacket(FriendlyByteBuf buf) {
        this(buf.readUUID(), buf.readByte());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(this.playerId);
        buf.writeByte(this.selectedSlot);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
