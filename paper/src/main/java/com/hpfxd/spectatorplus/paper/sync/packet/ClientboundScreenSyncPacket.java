package com.hpfxd.spectatorplus.paper.sync.packet;

import com.google.common.io.ByteArrayDataOutput;
import com.hpfxd.spectatorplus.paper.sync.ClientboundSyncPacket;
import com.hpfxd.spectatorplus.paper.util.SerializationUtil;
import org.bukkit.NamespacedKey;

import java.util.UUID;

public record ClientboundScreenSyncPacket(
        UUID playerId,
        int flags
) implements ClientboundSyncPacket {
    public static final NamespacedKey ID = new NamespacedKey("spectatorplus", "screen_sync");

    public static ClientboundScreenSyncPacket of(UUID playerId, boolean isSurvivalInventory, boolean isClientRequested, boolean hasDummySlots) {
        int flags = 0;

        if (isSurvivalInventory) {
            flags |= 0x01;
        }

        if (isClientRequested) {
            flags |= 0x02;
        }

        if (hasDummySlots) {
            flags |= 0x04;
        }

        return new ClientboundScreenSyncPacket(playerId, flags);
    }

    @Override
    public void write(ByteArrayDataOutput buf) {
        SerializationUtil.writeUuid(buf, this.playerId);
        buf.writeByte(this.flags);
    }

    @Override
    public NamespacedKey channel() {
        return ID;
    }
}
