package com.hpfxd.spectatorplus.paper.sync.packet;

import com.google.common.io.ByteArrayDataOutput;
import com.hpfxd.spectatorplus.paper.sync.ClientboundSyncPacket;
import com.hpfxd.spectatorplus.paper.util.SerializationUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public record ClientboundHotbarSyncPacket(
        UUID playerId,
        ItemStack[] items
) implements ClientboundSyncPacket {
    public static final NamespacedKey ID = new NamespacedKey("spectatorplus", "hotbar_sync");

    @Override
    public void write(ByteArrayDataOutput buf) {
        SerializationUtil.writeUuid(buf, this.playerId);

        buf.writeInt(this.items.length);

        for (final ItemStack item : this.items) {
            buf.writeBoolean(item != null);

            if (item != null) {
                if (item.isEmpty()) {
                    buf.writeInt(0);
                } else {
                    final byte[] itemData = item.serializeAsBytes();
                    buf.writeInt(itemData.length);
                    buf.write(itemData);
                }
            }
        }
    }

    @Override
    public NamespacedKey channel() {
        return ID;
    }
}
