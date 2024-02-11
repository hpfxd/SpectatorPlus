package com.hpfxd.spectatorplus.fabric.sync.handler;

import com.hpfxd.spectatorplus.fabric.sync.ServerSyncController;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundHotbarSyncPacket;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HotbarSyncHandler {
    private static final Map<UUID, ItemStack[]> HOTBARS = new HashMap<>();

    public static void init() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> HOTBARS.remove(handler.getPlayer().getUUID()));

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> HOTBARS.clear());

        ServerTickEvents.END_WORLD_TICK.register(HotbarSyncHandler::tick);
    }

    private static void tick(ServerLevel level) {
        for (final ServerPlayer player : level.players()) {
            final ItemStack[] slots = HOTBARS.computeIfAbsent(player.getUUID(), k -> {
                final ItemStack[] arr = new ItemStack[9];
                Arrays.fill(arr, ItemStack.EMPTY);
                return arr;
            });

            final ItemStack[] sendSlots = new ItemStack[slots.length];
            boolean updated = false;

            for (int i = 0; i < 9; i++) {
                final ItemStack item = player.getInventory().getItem(i);

                if (!ItemStack.matches(item, slots[i])) {
                    slots[i] = item.copy();
                    sendSlots[i] = item;
                    updated = true;
                }
            }

            if (updated) {
                ServerSyncController.broadcastPacketToSpectators(player, new ClientboundHotbarSyncPacket(player.getUUID(), sendSlots));
            }
        }
    }
}
