package com.hpfxd.spectatorplus.fabric.sync.packet;

import com.hpfxd.spectatorplus.fabric.sync.ClientboundSyncPacket;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record ClientboundFoodSyncPacket(
        UUID playerId,
        int food,
        float saturation
) implements ClientboundSyncPacket {
    public static final StreamCodec<FriendlyByteBuf, ClientboundFoodSyncPacket> STREAM_CODEC = CustomPacketPayload.codec(ClientboundFoodSyncPacket::write, ClientboundFoodSyncPacket::new);
    public static final CustomPacketPayload.Type<ClientboundFoodSyncPacket> TYPE = CustomPacketPayload.createType("spectatorplus:food_sync");
    private static final String PERMISSION = "spectatorplus.sync.food";

    public static ClientboundFoodSyncPacket initializing(ServerPlayer target) {
        return new ClientboundFoodSyncPacket(target.getUUID(), target.getFoodData().getFoodLevel(), target.getFoodData().getSaturationLevel());
    }

    public ClientboundFoodSyncPacket(FriendlyByteBuf buf) {
        this(buf.readUUID(), buf.readInt(), buf.readFloat());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(this.playerId);
        buf.writeInt(this.food);
        buf.writeFloat(this.saturation);
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
