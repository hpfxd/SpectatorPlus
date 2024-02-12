package com.hpfxd.spectatorplus.fabric.sync.packet;

import com.hpfxd.spectatorplus.fabric.sync.ClientboundSyncPacket;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public record ClientboundFoodSyncPacket(
        UUID playerId,
        int food,
        float saturation
) implements ClientboundSyncPacket {
    public static final PacketType<ClientboundFoodSyncPacket> TYPE = PacketType.create(new ResourceLocation("spectatorplus", "food_sync"), ClientboundFoodSyncPacket::new);
    private static final String PERMISSION = "spectatorplus.sync.food";

    public static ClientboundFoodSyncPacket initializing(ServerPlayer target) {
        return new ClientboundFoodSyncPacket(target.getUUID(), target.getFoodData().getFoodLevel(), target.getFoodData().getSaturationLevel());
    }

    public ClientboundFoodSyncPacket(FriendlyByteBuf buf) {
        this(buf.readUUID(), buf.readInt(), buf.readFloat());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(this.playerId);
        buf.writeInt(this.food);
        buf.writeFloat(this.saturation);
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
