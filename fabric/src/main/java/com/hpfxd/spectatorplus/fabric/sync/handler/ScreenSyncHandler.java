package com.hpfxd.spectatorplus.fabric.sync.handler;

import com.hpfxd.spectatorplus.fabric.sync.packet.ServerboundOpenedInventorySyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ServerboundRequestInventoryOpenPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public class ScreenSyncHandler {
    public static void init() {
        ServerPlayNetworking.registerGlobalReceiver(ServerboundRequestInventoryOpenPacket.TYPE, ScreenSyncHandler::handle);
        ServerPlayNetworking.registerGlobalReceiver(ServerboundOpenedInventorySyncPacket.TYPE, ScreenSyncHandler::handle);
    }

    private static void handle(ServerboundRequestInventoryOpenPacket packet, ServerPlayer player, PacketSender sender) {
    }

    private static void handle(ServerboundOpenedInventorySyncPacket packet, ServerPlayer player, PacketSender sender) {
    }
}
