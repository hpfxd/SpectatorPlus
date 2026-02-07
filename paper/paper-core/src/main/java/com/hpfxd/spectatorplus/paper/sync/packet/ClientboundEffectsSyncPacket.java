package com.hpfxd.spectatorplus.paper.sync.packet;

import com.google.common.io.ByteArrayDataOutput;
import com.hpfxd.spectatorplus.paper.sync.ClientboundSyncPacket;
import com.hpfxd.spectatorplus.paper.util.SerializationUtil;
import org.bukkit.NamespacedKey;

import java.util.List;
import java.util.UUID;
import com.hpfxd.spectatorplus.paper.effect.SyncedEffect;

public record ClientboundEffectsSyncPacket(
        UUID playerId,
        List<SyncedEffect> effects // List of effect data
) implements ClientboundSyncPacket {
    public static final NamespacedKey ID = new NamespacedKey("spectatorplus", "effects_sync");

    @Override
    public void write(ByteArrayDataOutput buf) {
        SerializationUtil.writeUuid(buf, this.playerId);
        SerializationUtil.writeVarInt(buf, effects.size());
        for (SyncedEffect effect : effects) {
            effect.write(buf);
        }
    }

    @Override
    public NamespacedKey channel() {
        return ID;
    }
}
