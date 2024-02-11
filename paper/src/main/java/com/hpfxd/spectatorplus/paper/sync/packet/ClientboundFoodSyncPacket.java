package com.hpfxd.spectatorplus.paper.sync.packet;

import com.google.common.io.ByteArrayDataOutput;
import com.hpfxd.spectatorplus.paper.sync.ClientboundSyncPacket;
import com.hpfxd.spectatorplus.paper.util.SerializationUtil;
import org.bukkit.NamespacedKey;

import java.util.UUID;

public record ClientboundFoodSyncPacket(
        UUID playerId,
        int food,
        float saturation
) implements ClientboundSyncPacket {
    public static final NamespacedKey ID = new NamespacedKey("spectatorplus", "food_sync");

    @Override
    public void write(ByteArrayDataOutput buf) {
        SerializationUtil.writeUuid(buf, this.playerId);
        buf.writeInt(this.food);
        buf.writeFloat(this.saturation);
    }

    @Override
    public NamespacedKey channel() {
        return ID;
    }
}
