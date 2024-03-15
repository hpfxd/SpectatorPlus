package com.hpfxd.spectatorplus.fabric.sync;

import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundExperienceSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundFoodSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundHotbarSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundInventorySyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundScreenCursorSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundScreenSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundSelectedSlotSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ServerboundOpenedInventorySyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ServerboundRequestInventoryOpenPacket;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public final class SyncPackets {
    public static void registerAll() {
        PayloadTypeRegistry.playC2S().register(ServerboundOpenedInventorySyncPacket.TYPE, ServerboundOpenedInventorySyncPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(ServerboundRequestInventoryOpenPacket.TYPE, ServerboundRequestInventoryOpenPacket.STREAM_CODEC);

        PayloadTypeRegistry.playS2C().register(ClientboundExperienceSyncPacket.TYPE, ClientboundExperienceSyncPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ClientboundFoodSyncPacket.TYPE, ClientboundFoodSyncPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ClientboundHotbarSyncPacket.TYPE, ClientboundHotbarSyncPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ClientboundInventorySyncPacket.TYPE, ClientboundInventorySyncPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ClientboundScreenCursorSyncPacket.TYPE, ClientboundScreenCursorSyncPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ClientboundScreenSyncPacket.TYPE, ClientboundScreenSyncPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ClientboundSelectedSlotSyncPacket.TYPE, ClientboundSelectedSlotSyncPacket.STREAM_CODEC);
    }

    private SyncPackets() {
    }
}
