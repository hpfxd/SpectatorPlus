package com.hpfxd.spectatorplus.paper.sync.packet;

import com.google.common.io.ByteArrayDataInput;
import com.hpfxd.spectatorplus.paper.sync.ServerboundSyncPacket;
import org.bukkit.NamespacedKey;

public class ServerboundOpenedInventorySyncPacket implements ServerboundSyncPacket {
    public static final NamespacedKey ID = new NamespacedKey("spectatorplus", "opened_inventory_sync");

    public ServerboundOpenedInventorySyncPacket(ByteArrayDataInput buf) {
    }

    @Override
    public NamespacedKey channel() {
        return ID;
    }
}
