package com.hpfxd.spectatorplus.fabric.sync.packet;

import com.hpfxd.spectatorplus.fabric.sync.ClientboundSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.CustomPacketCodecs;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record ClientboundContainerSyncPacket(
        UUID playerId,
        MenuType<?> containerType,
        int containerSize,
        ItemStack[] items
) implements ClientboundSyncPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundContainerSyncPacket> STREAM_CODEC =
        CustomPacketPayload.codec(ClientboundContainerSyncPacket::write, ClientboundContainerSyncPacket::new);
    public static final CustomPacketPayload.Type<ClientboundContainerSyncPacket> TYPE =
        new CustomPacketPayload.Type<>(Identifier.parse("spectatorplus:container_sync"));
    private static final String PERMISSION = "spectatorplus.sync.screen";

    public ClientboundContainerSyncPacket(RegistryFriendlyByteBuf buf) {
        this(
            buf.readUUID(),
                buf.readById(id -> {
                    MenuType<?> type = BuiltInRegistries.MENU.byId(id);
                    return type != null ? type : MenuType.GENERIC_9x3;
                }),
            buf.readVarInt(),
            CustomPacketCodecs.readItems(buf)
        );
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(this.playerId);
        buf.writeById(BuiltInRegistries.MENU::getId, this.containerType);
        buf.writeVarInt(this.containerSize);
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
