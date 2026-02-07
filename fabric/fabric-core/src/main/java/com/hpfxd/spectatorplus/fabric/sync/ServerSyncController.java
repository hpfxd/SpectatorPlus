package com.hpfxd.spectatorplus.fabric.sync;

import com.hpfxd.spectatorplus.fabric.sync.handler.ContainerSyncHandler;
import com.hpfxd.spectatorplus.fabric.sync.handler.CursorSyncHandler;
import com.hpfxd.spectatorplus.fabric.sync.handler.EffectsSyncHandler;
import com.hpfxd.spectatorplus.fabric.sync.handler.HotbarSyncHandler;
import com.hpfxd.spectatorplus.fabric.sync.handler.InventorySyncHandler;
import com.hpfxd.spectatorplus.fabric.sync.handler.ScreenSyncHandler;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.Collection;

public class ServerSyncController {
    public static void init() {
        SyncPackets.registerAll();

        HotbarSyncHandler.init();
        ScreenSyncHandler.init();
        ContainerSyncHandler.init();
        EffectsSyncHandler.init();
        InventorySyncHandler.init();
        CursorSyncHandler.init();
    }

    public static void sendPacket(ServerPlayer serverPlayer, ClientboundSyncPacket packet) {
        if (packet.canSend(serverPlayer)) {
            ServerPlayNetworking.send(serverPlayer, packet);
        }
    }

    public static void broadcastPacketToSpectators(Entity target, ClientboundSyncPacket packet) {
        for (final ServerPlayer spectator : getSpectators(target)) {
            if (packet.canSend(spectator)) {
                ServerPlayNetworking.send(spectator, packet);
            }
        }
    }

    public static Collection<ServerPlayer> getSpectators(Entity target) {
        return ((ServerLevel) target.level()).getPlayers(spectator -> target != spectator && target.equals(spectator.getCamera()));
    }
}
