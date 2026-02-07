package com.hpfxd.spectatorplus.fabric.sync.packet;

import com.hpfxd.spectatorplus.fabric.sync.ClientboundSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.CustomPacketCodecs;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * items layout:
 *   0-35: main inventory (including hotbar)
 *   36-39: armor (helmet, chestplate, leggings, boots)
 */
public record ClientboundInventorySyncPacket(
        UUID playerId,
        ItemStack[] items
) implements ClientboundSyncPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundInventorySyncPacket> STREAM_CODEC = CustomPacketPayload.codec(ClientboundInventorySyncPacket::write, ClientboundInventorySyncPacket::new);
    public static final CustomPacketPayload.Type<ClientboundInventorySyncPacket> TYPE = new CustomPacketPayload.Type<>(Identifier.parse("spectatorplus:inventory_sync"));
    public static final int ITEMS_LENGTH = 4 * 9 + 4 + 1; // 36 main + 4 armor + 1 offhand = 41
    private static final String PERMISSION = "spectatorplus.sync.inventory";

    public ClientboundInventorySyncPacket(RegistryFriendlyByteBuf buf) {
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
