package com.hpfxd.spectatorplus.fabric.sync.handler;

import com.hpfxd.spectatorplus.fabric.sync.ServerSyncController;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundHotbarSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundInventorySyncPacket;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

public class InventorySyncHandler {
    private static final Map<UUID, ItemStack[]> playerInventories = new HashMap<>();
    private static int tickCounter = 0;
    private static final int SYNC_INTERVAL = 5; // Sync every 5 ticks (4 times per second)

    public static void init() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
            playerInventories.remove(handler.getPlayer().getUUID()));
        ServerTickEvents.END_SERVER_TICK.register(InventorySyncHandler::tick);
    }

    public static void tick(MinecraftServer server) {
        tickCounter++;
        if (tickCounter % SYNC_INTERVAL != 0) {
            return; // Skip this tick
        }

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (ServerSyncController.getSpectators(player).isEmpty()) {
                continue;
            }
            syncPlayerInventory(player);
        }
    }

    private static void syncPlayerInventory(ServerPlayer player) {
        final ItemStack[] slots = playerInventories.computeIfAbsent(player.getUUID(), k -> {
            final ItemStack[] arr = new ItemStack[ClientboundInventorySyncPacket.ITEMS_LENGTH];
            Arrays.fill(arr, ItemStack.EMPTY);
            return arr;
        });

        //as hotbar will change more than rest of inventory, we track it separately to avoid sending full inventory when only hotbar changes
        final ItemStack[] currentSlots = extractPlayerInventory(player);
        final ItemStack[] inventorySendSlots = new ItemStack[ClientboundInventorySyncPacket.ITEMS_LENGTH];
        final ItemStack[] hotbarSendSlots = new ItemStack[ClientboundHotbarSyncPacket.ITEMS_LENGTH];

        boolean updatedHotbar = false;
        boolean updatedInventory = false;

        final Inventory inventory = player.getInventory();

        // Main inventory (0-35) including hotbar (0-8)
        for (int i = 0; i < currentSlots.length; i++) {
            if (!ItemStack.matches(currentSlots[i], slots[i])) {
                slots[i] = currentSlots[i].copy();
                inventorySendSlots[i] = currentSlots[i];
                updatedInventory = true;

                // 热键栏是槽位 0-8
                if (i < ClientboundHotbarSyncPacket.ITEMS_LENGTH) {
                    hotbarSendSlots[i] = currentSlots[i];
                    updatedHotbar = true;
                }
            }
        }

        if (updatedInventory) {
            ScreenSyncHandler.updatePlayerInventory(player, inventorySendSlots);
        }

        if (updatedHotbar) {
            ServerSyncController.broadcastPacketToSpectators(player,
                    new ClientboundHotbarSyncPacket(player.getUUID(), hotbarSendSlots));
        }
    }

    //this for send full inventory packet immediately
    public static void sendPacket(ServerPlayer spectator, ServerPlayer target) {
        final ItemStack[] slots = extractPlayerInventory(target);
        ServerSyncController.sendPacket(spectator,
                new ClientboundInventorySyncPacket(target.getUUID(), slots));
    }

    private static ItemStack[] extractPlayerInventory(ServerPlayer player) {
        final Inventory inventory = player.getInventory();
        final ItemStack[] slots = new ItemStack[ClientboundInventorySyncPacket.ITEMS_LENGTH];

        // Main inventory  (0-35)
        IntStream.range(0, 36).forEach(i -> {
            ItemStack item = inventory.getItem(i);
            slots[i] = item != null ? item : ItemStack.EMPTY;
        });

        // Armor (36-39)
        IntStream.range(36, 40).forEach(i -> {
            ItemStack item = inventory.getItem(i);
            slots[i] = item != null ? item : ItemStack.EMPTY;
        });

        // Offhand slot (40)
        ItemStack offhand = inventory.getItem(40);
        slots[40] = offhand != null ? offhand : ItemStack.EMPTY;

        return slots;
    }

    public static void onPlayerDisconnect(UUID playerId) {
        playerInventories.remove(playerId);
    }

    /**
     * Force an immediate inventory sync for a player, bypassing the tick counter
     * Used for critical inventory changes that need immediate synchronization
     */
    public static void forceSyncPlayer(ServerPlayer player) {
        if (!ServerSyncController.getSpectators(player).isEmpty()) {
            syncPlayerInventory(player);
        }
    }
}
