package com.hpfxd.spectatorplus.fabric.sync.packet;

import com.hpfxd.spectatorplus.fabric.sync.ClientboundSyncPacket;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public record ClientboundExperienceSyncPacket(
        UUID playerId,
        float progress,
        int neededForNextLevel,
        int level
) implements ClientboundSyncPacket {
    public static final PacketType<ClientboundExperienceSyncPacket> TYPE = PacketType.create(new ResourceLocation("spectatorplus", "experience_sync"), ClientboundExperienceSyncPacket::new);
    private static final String PERMISSION = "spectatorplus.sync.experience";

    public static ClientboundExperienceSyncPacket initializing(ServerPlayer target) {
        return new ClientboundExperienceSyncPacket(target.getUUID(), target.experienceProgress, target.getXpNeededForNextLevel(), target.experienceLevel);
    }

    public ClientboundExperienceSyncPacket(FriendlyByteBuf buf) {
        this(buf.readUUID(), buf.readFloat(), buf.readInt(), buf.readInt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(this.playerId);
        buf.writeFloat(this.progress);
        buf.writeInt(this.neededForNextLevel);
        buf.writeInt(this.level);
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
