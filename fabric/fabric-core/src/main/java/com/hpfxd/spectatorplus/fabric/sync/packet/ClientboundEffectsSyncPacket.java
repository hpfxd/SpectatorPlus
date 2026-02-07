
package com.hpfxd.spectatorplus.fabric.sync.packet;

import com.hpfxd.spectatorplus.fabric.sync.ClientboundSyncPacket;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import com.hpfxd.spectatorplus.fabric.sync.SyncedEffect;

public record ClientboundEffectsSyncPacket(
    UUID playerId,
    List<SyncedEffect> effects
) implements ClientboundSyncPacket {
    public static final StreamCodec<FriendlyByteBuf, ClientboundEffectsSyncPacket> STREAM_CODEC = CustomPacketPayload.codec(ClientboundEffectsSyncPacket::write, ClientboundEffectsSyncPacket::new);
    public static final CustomPacketPayload.Type<ClientboundEffectsSyncPacket> TYPE = new CustomPacketPayload.Type<>(Identifier.parse("spectatorplus:effects_sync"));
    private static final String PERMISSION = "spectatorplus.sync.effects";

    public ClientboundEffectsSyncPacket(FriendlyByteBuf buf) {
        this(buf.readUUID(), SyncedEffect.readList(buf));
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(this.playerId);
        buf.writeVarInt(this.effects.size());
        for (SyncedEffect effect : this.effects) {
            buf.writeUtf(effect.effectKey);
            buf.writeVarInt(effect.amplifier);
            buf.writeVarInt(effect.duration);
        }
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
