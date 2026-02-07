package com.hpfxd.spectatorplus.fabric.sync.packet;

import com.hpfxd.spectatorplus.fabric.sync.ClientboundSyncPacket;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record ClientboundExperienceSyncPacket(
        UUID playerId,
        float progress,
        int neededForNextLevel,
        int level
) implements ClientboundSyncPacket {
    public static final StreamCodec<FriendlyByteBuf, ClientboundExperienceSyncPacket> STREAM_CODEC = CustomPacketPayload.codec(ClientboundExperienceSyncPacket::write, ClientboundExperienceSyncPacket::new);
    public static final CustomPacketPayload.Type<ClientboundExperienceSyncPacket> TYPE = new CustomPacketPayload.Type<>(Identifier.parse("spectatorplus:experience_sync"));
    private static final String PERMISSION = "spectatorplus.sync.experience";

    public static ClientboundExperienceSyncPacket initializing(ServerPlayer target) {
        return new ClientboundExperienceSyncPacket(target.getUUID(), target.experienceProgress, target.getXpNeededForNextLevel(), target.experienceLevel);
    }

    public ClientboundExperienceSyncPacket(FriendlyByteBuf buf) {
        this(buf.readUUID(), buf.readFloat(), buf.readInt(), buf.readInt());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(this.playerId);
        buf.writeFloat(this.progress);
        buf.writeInt(this.neededForNextLevel);
        buf.writeInt(this.level);
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
