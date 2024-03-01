package com.hpfxd.spectatorplus.fabric.sync.packet;

import com.hpfxd.spectatorplus.fabric.sync.ClientboundSyncPacket;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public record ClientboundInventorySyncPacket(
        UUID playerId,
        ItemStack[] items
) implements ClientboundSyncPacket {
    public static final int ITEMS_LENGTH = 4 * 9;
    public static final PacketType<ClientboundInventorySyncPacket> TYPE = PacketType.create(new ResourceLocation("spectatorplus", "inventory_sync"), ClientboundInventorySyncPacket::new);
    private static final String PERMISSION = "spectatorplus.sync.inventory";

    public ClientboundInventorySyncPacket(FriendlyByteBuf buf) {
        this(buf.readUUID(), ClientboundHotbarSyncPacket.readItems(buf));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(this.playerId);
        ClientboundHotbarSyncPacket.writeItems(buf, this.items);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    @Override
    public boolean canSend(ServerPlayer receiver) {
        return Permissions.check(receiver, PERMISSION, true);
    }
}
