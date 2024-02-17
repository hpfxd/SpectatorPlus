package com.hpfxd.spectatorplus.paper.sync.packet;

import com.google.common.io.ByteArrayDataOutput;
import com.hpfxd.spectatorplus.paper.sync.ClientboundSyncPacket;
import com.hpfxd.spectatorplus.paper.util.SerializationUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public record ClientboundInventorySyncPacket(
        UUID playerId,
        int containerId,
        ItemStack[] inventoryItems
) implements ClientboundSyncPacket {
    public static final int ITEMS_LENGTH = 4 * 9;
    public static final NamespacedKey ID = new NamespacedKey("spectatorplus", "inventory_sync");

    @Override
    public void write(ByteArrayDataOutput buf) {
        SerializationUtil.writeUuid(buf, this.playerId);

        buf.writeInt(this.inventoryItems.length);

        for (final ItemStack item : this.inventoryItems) {
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
