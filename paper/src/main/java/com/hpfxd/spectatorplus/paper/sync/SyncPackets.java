package com.hpfxd.spectatorplus.paper.sync;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteArrayDataInput;
import com.hpfxd.spectatorplus.paper.sync.packet.ClientboundExperienceSyncPacket;
import com.hpfxd.spectatorplus.paper.sync.packet.ClientboundFoodSyncPacket;
import com.hpfxd.spectatorplus.paper.sync.packet.ClientboundHotbarSyncPacket;
import com.hpfxd.spectatorplus.paper.sync.packet.ClientboundSelectedSlotSyncPacket;
import org.bukkit.NamespacedKey;

import java.util.Map;
import java.util.function.Function;

public final class SyncPackets {
    public static final Map<NamespacedKey, Class<? extends ClientboundSyncPacket>> CLIENTBOUND = ImmutableMap.<NamespacedKey, Class<? extends ClientboundSyncPacket>>builder()
            .put(ClientboundExperienceSyncPacket.ID, ClientboundExperienceSyncPacket.class)
            .put(ClientboundFoodSyncPacket.ID, ClientboundFoodSyncPacket.class)
            .put(ClientboundHotbarSyncPacket.ID, ClientboundHotbarSyncPacket.class)
            .put(ClientboundSelectedSlotSyncPacket.ID, ClientboundSelectedSlotSyncPacket.class)
            .build();

    public static final Map<NamespacedKey, Function<ByteArrayDataInput, ? extends ServerboundSyncPacket>> SERVERBOUND = ImmutableMap.of();

    private SyncPackets() {
    }
}
