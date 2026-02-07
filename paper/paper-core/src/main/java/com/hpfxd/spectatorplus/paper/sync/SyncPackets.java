package com.hpfxd.spectatorplus.paper.sync;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteArrayDataInput;
import com.hpfxd.spectatorplus.paper.sync.packet.ClientboundEffectsSyncPacket;
import com.hpfxd.spectatorplus.paper.sync.packet.ClientboundExperienceSyncPacket;
import com.hpfxd.spectatorplus.paper.sync.packet.ClientboundFoodSyncPacket;
import com.hpfxd.spectatorplus.paper.sync.packet.ClientboundHotbarSyncPacket;
import com.hpfxd.spectatorplus.paper.sync.packet.ClientboundInventorySyncPacket;
import com.hpfxd.spectatorplus.paper.sync.packet.ClientboundScreenCursorSyncPacket;
import com.hpfxd.spectatorplus.paper.sync.packet.ClientboundScreenSyncPacket;
import com.hpfxd.spectatorplus.paper.sync.packet.ClientboundSelectedSlotSyncPacket;
import com.hpfxd.spectatorplus.paper.sync.packet.ServerboundOpenedInventorySyncPacket;
import com.hpfxd.spectatorplus.paper.sync.packet.ServerboundRequestInventoryOpenPacket;
import org.bukkit.NamespacedKey;

import java.util.Map;
import java.util.function.Function;

public final class SyncPackets {
    public static final Map<NamespacedKey, Class<? extends ClientboundSyncPacket>> CLIENTBOUND = ImmutableMap.<NamespacedKey, Class<? extends ClientboundSyncPacket>>builder()
            .put(ClientboundExperienceSyncPacket.ID, ClientboundExperienceSyncPacket.class)
            .put(ClientboundFoodSyncPacket.ID, ClientboundFoodSyncPacket.class)
            .put(ClientboundHotbarSyncPacket.ID, ClientboundHotbarSyncPacket.class)
            .put(ClientboundInventorySyncPacket.ID, ClientboundInventorySyncPacket.class)
            .put(ClientboundScreenCursorSyncPacket.ID, ClientboundScreenCursorSyncPacket.class)
            .put(ClientboundScreenSyncPacket.ID, ClientboundScreenSyncPacket.class)
            .put(ClientboundSelectedSlotSyncPacket.ID, ClientboundSelectedSlotSyncPacket.class)
            .put(ClientboundEffectsSyncPacket.ID, ClientboundEffectsSyncPacket.class)
            .build();

    public static final Map<NamespacedKey, Function<ByteArrayDataInput, ? extends ServerboundSyncPacket>> SERVERBOUND = ImmutableMap.<NamespacedKey, Function<ByteArrayDataInput, ? extends ServerboundSyncPacket>>builder()
            .put(ServerboundOpenedInventorySyncPacket.ID, ServerboundOpenedInventorySyncPacket::new)
            .put(ServerboundRequestInventoryOpenPacket.ID, ServerboundRequestInventoryOpenPacket::new)
            .build();

    private SyncPackets() {
    }
}
