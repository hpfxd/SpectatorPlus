package com.hpfxd.spectatorplus.paper.sync.packet;

import com.google.common.io.ByteArrayDataOutput;
import com.hpfxd.spectatorplus.paper.sync.ClientboundSyncPacket;
import com.hpfxd.spectatorplus.paper.util.SerializationUtil;
import org.bukkit.NamespacedKey;

import java.util.UUID;

public record ClientboundExperienceSyncPacket(
        UUID playerId,
        float progress,
        int neededForNextLevel,
        int level
) implements ClientboundSyncPacket {
    public static final NamespacedKey ID = new NamespacedKey("spectatorplus", "experience_sync");

    @Override
    public void write(ByteArrayDataOutput buf) {
        SerializationUtil.writeUuid(buf, this.playerId);
        buf.writeFloat(this.progress);
        buf.writeInt(this.neededForNextLevel);
        buf.writeInt(this.level);
    }

    @Override
    public NamespacedKey channel() {
        return ID;
    }
}
