package com.hpfxd.spectatorplus.paper.sync.packet;

import com.google.common.io.ByteArrayDataOutput;
import com.hpfxd.spectatorplus.paper.sync.ClientboundSyncPacket;
import com.hpfxd.spectatorplus.paper.util.SerializationUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public record ClientboundScreenCursorSyncPacket(
        UUID playerId,
        ItemStack cursor,
        int originSlot
) implements ClientboundSyncPacket {
    public static final NamespacedKey ID = new NamespacedKey("spectatorplus", "screen_cursor_sync");

    @Override
    public void write(ByteArrayDataOutput buf) {
        SerializationUtil.writeUuid(buf, this.playerId);
        SerializationUtil.writeItem(buf, this.cursor);
        buf.writeByte(this.originSlot);
    }

    @Override
    public NamespacedKey channel() {
        return ID;
    }
}
