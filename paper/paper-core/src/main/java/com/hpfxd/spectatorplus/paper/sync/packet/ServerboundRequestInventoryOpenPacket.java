package com.hpfxd.spectatorplus.paper.sync.packet;

import com.google.common.io.ByteArrayDataInput;
import com.hpfxd.spectatorplus.paper.sync.ServerboundSyncPacket;
import com.hpfxd.spectatorplus.paper.util.SerializationUtil;
import org.bukkit.NamespacedKey;

import java.util.UUID;

public record ServerboundRequestInventoryOpenPacket(
        UUID playerId
) implements ServerboundSyncPacket {
    public static final NamespacedKey ID = new NamespacedKey("spectatorplus", "request_inventory_open");

    public ServerboundRequestInventoryOpenPacket(ByteArrayDataInput buf) {
        this(SerializationUtil.readUuid(buf));
    }

    @Override
    public NamespacedKey channel() {
        return ID;
    }
}
