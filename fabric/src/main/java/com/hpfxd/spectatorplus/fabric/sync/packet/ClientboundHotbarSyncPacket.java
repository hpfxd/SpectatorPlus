package com.hpfxd.spectatorplus.fabric.sync.packet;

import com.hpfxd.spectatorplus.fabric.sync.ClientboundSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.CustomPacketCodecs;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record ClientboundHotbarSyncPacket(
        UUID playerId,
        ItemStack[] items
) implements ClientboundSyncPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundHotbarSyncPacket> STREAM_CODEC = CustomPacketPayload.codec(ClientboundHotbarSyncPacket::write, ClientboundHotbarSyncPacket::new);
    public static final CustomPacketPayload.Type<ClientboundHotbarSyncPacket> TYPE = CustomPacketPayload.createType("spectatorplus:hotbar_sync");
    static final String PERMISSION = "spectatorplus.sync.hotbar";

    public static ClientboundHotbarSyncPacket initializing(ServerPlayer target) {
        final ItemStack[] items = new ItemStack[9];

        for (int slot = 0; slot < items.length; slot++) {
            items[slot] = target.getInventory().getItem(slot);
        }

        return new ClientboundHotbarSyncPacket(target.getUUID(), items);
    }

    public ClientboundHotbarSyncPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readUUID(), CustomPacketCodecs.readItems(buf));
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(this.playerId);
        CustomPacketCodecs.writeItems(buf, this.items);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean canSend(ServerPlayer receiver) {
        return Permissions.check(receiver, PERMISSION, true);
    }
}
