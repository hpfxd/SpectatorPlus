package com.hpfxd.spectatorplus.fabric.client.sync;

import com.hpfxd.spectatorplus.fabric.sync.PositionEntry;
import com.hpfxd.spectatorplus.fabric.sync.packet.ServerboundPositionsSyncPacket;
import net.fabricmc.fabric.api.client.networking.v1.C2SPlayChannelEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

import java.util.ArrayList;
import java.util.List;

public class ClientPositionSyncTransmitter {
    public static boolean transmitPositions;
    private static List<PositionEntry> entries;

    static void init() {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            transmitPositions = false;
            entries = null;
        });

        C2SPlayChannelEvents.REGISTER.register((handler, sender, client, channels) -> {
            if (channels.contains(ServerboundPositionsSyncPacket.TYPE.getId())) {
                transmitPositions = true;
                entries = new ArrayList<>();
            }
        });

        C2SPlayChannelEvents.UNREGISTER.register((handler, sender, client, channels) -> {
            if (channels.contains(ServerboundPositionsSyncPacket.TYPE.getId())) {
                transmitPositions = false;
                entries = null;
            }
        });

        //ClientTickEvents.END_CLIENT_TICK.register(client -> ClientSyncController.syncData.positionRecord = null);
    }

    public static void pushPosition(PositionEntry entry) {
        entries.add(entry);
    }

    public static void clearPositions() {
        entries.clear();
    }

    public static PositionEntry[] popPositions() {
        final PositionEntry[] values = entries.toArray(new PositionEntry[0]);
        clearPositions();

        return values;
    }
}
