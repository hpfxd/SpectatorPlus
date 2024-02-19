package com.hpfxd.spectatorplus.paper.sync.packet;

import com.google.common.io.ByteArrayDataOutput;
import com.hpfxd.spectatorplus.paper.sync.ClientboundSyncPacket;
import com.hpfxd.spectatorplus.paper.util.SerializationUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public record ClientboundInventorySyncPacket(
        UUID playerId,
        ItemStack[] inventoryItems
) implements ClientboundSyncPacket {
    public static final int ITEMS_LENGTH = 4 * 9;
    public static final NamespacedKey ID = new NamespacedKey("spectatorplus", "inventory_sync");

    @Override
    public void write(ByteArrayDataOutput buf) {
        SerializationUtil.writeUuid(buf, this.playerId);
        SerializationUtil.writeItems(buf, this.inventoryItems);
    }

    @Override
    public NamespacedKey channel() {
        return ID;
    }
}
