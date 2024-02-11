package com.hpfxd.spectatorplus.fabric.sync;

import com.hpfxd.spectatorplus.fabric.sync.handler.HotbarSyncHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public class ServerSyncController {
    public static void init() {
        HotbarSyncHandler.init();
    }

    public static void sendPacket(ServerPlayer serverPlayer, ClientboundSyncPacket packet) {
        ServerPlayNetworking.send(serverPlayer, packet);
    }

    public static void broadcastPacketToSpectators(ServerPlayer target, ClientboundSyncPacket packet) {
        for (final ServerPlayer spectator : getSpectators(target)) {
            ServerPlayNetworking.send(spectator, packet);
        }
    }

    private static Collection<ServerPlayer> getSpectators(ServerPlayer target) {
        return target.serverLevel().getPlayers(spectator -> target.equals(spectator.getCamera()));
    }
}
