package com.hpfxd.spectatorplus.fabric.sync;

import com.hpfxd.spectatorplus.fabric.sync.handler.HotbarSyncHandler;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundPositionsSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ServerboundPositionsSyncPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.Collection;

public class ServerSyncController {
    public static void init() {
        HotbarSyncHandler.init();

        ServerPlayNetworking.registerGlobalReceiver(ServerboundPositionsSyncPacket.TYPE, ServerSyncController::handlePositionsSyncPacket);
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

    private static Collection<ServerPlayer> getSpectators(Entity target) {
        return ((ServerLevel) target.level()).getPlayers(spectator -> target.equals(spectator.getCamera()));
    }

    private static void handlePositionsSyncPacket(ServerboundPositionsSyncPacket packet, ServerPlayer player, PacketSender responseSender) {
        broadcastPacketToSpectators(player, new ClientboundPositionsSyncPacket(player.getUUID(), packet.entries()));
    }
}
