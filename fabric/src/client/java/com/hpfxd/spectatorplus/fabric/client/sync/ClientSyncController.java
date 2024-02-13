package com.hpfxd.spectatorplus.fabric.client.sync;

import com.hpfxd.spectatorplus.fabric.client.mixin.MinecraftAccessor;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundExperienceSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundFoodSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundHotbarSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundPositionsSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundSelectedSlotSyncPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class ClientSyncController {
    public static ClientSyncData syncData;

    public static void init() {
        ClientPositionSyncTransmitter.init();

        ClientPlayNetworking.registerGlobalReceiver(ClientboundExperienceSyncPacket.TYPE, ClientSyncController::handle);
        ClientPlayNetworking.registerGlobalReceiver(ClientboundFoodSyncPacket.TYPE, ClientSyncController::handle);
        ClientPlayNetworking.registerGlobalReceiver(ClientboundHotbarSyncPacket.TYPE, ClientSyncController::handle);
        ClientPlayNetworking.registerGlobalReceiver(ClientboundSelectedSlotSyncPacket.TYPE, ClientSyncController::handle);
        ClientPlayNetworking.registerGlobalReceiver(ClientboundPositionsSyncPacket.TYPE, ClientSyncController::handle);

        ClientLoginConnectionEvents.INIT.register((handler, client) -> syncData = null);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> syncData = null);
    }

    private static void handle(ClientboundExperienceSyncPacket packet, LocalPlayer player, PacketSender sender) {
        setSyncData(packet.playerId());

        syncData.experienceProgress = packet.progress();
        syncData.experienceLevel = packet.level();
        syncData.experienceNeededForNextLevel = packet.neededForNextLevel();
    }

    private static void handle(ClientboundFoodSyncPacket packet, LocalPlayer player, PacketSender sender) {
        setSyncData(packet.playerId());

        if (syncData.foodData == null) {
            syncData.foodData = new FoodData();
        }
        syncData.foodData.setFoodLevel(packet.food());
        syncData.foodData.setSaturation(packet.saturation());
    }

    private static void handle(ClientboundHotbarSyncPacket packet, LocalPlayer player, PacketSender sender) {
        setSyncData(packet.playerId());

        final ItemStack[] items = packet.items();
        for (int slot = 0; slot < items.length; slot++) {
            final ItemStack item = items[slot];

            if (item != null) {
                syncData.hotbarItems.set(slot, item);
            }
        }
    }

    private static void handle(ClientboundSelectedSlotSyncPacket packet, LocalPlayer player, PacketSender sender) {
        setSyncData(packet.playerId());

        syncData.selectedHotbarSlot = packet.selectedSlot();
    }

    private static void handle(ClientboundPositionsSyncPacket packet, LocalPlayer player, PacketSender sender) {
        setSyncData(packet.playerId());

        syncData.positionRecord = new PositionRecord(((MinecraftAccessor) Minecraft.getInstance()).getClientTickCount(), packet.entries());
    }

    private static void setSyncData(UUID playerId) {
        if (syncData == null || !syncData.playerId.equals(playerId)) {
            syncData = new ClientSyncData(playerId);
        }
    }
}
