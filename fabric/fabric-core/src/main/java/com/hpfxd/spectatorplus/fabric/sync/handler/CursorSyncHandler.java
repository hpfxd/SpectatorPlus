package com.hpfxd.spectatorplus.fabric.sync.handler;

import com.hpfxd.spectatorplus.fabric.sync.ServerSyncController;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundScreenCursorSyncPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles synchronization of cursor items (items being dragged) to spectators
 */
public class CursorSyncHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CursorSyncHandler.class);
    private static final Map<UUID, ItemStack> playerCursors = new HashMap<>();

    public static void init() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
            playerCursors.remove(handler.getPlayer().getUUID()));
    }

    /**
     * Called when a player's cursor item changes
     */
    public static void onCursorChanged(ServerPlayer player, ItemStack newCursor) {
        onCursorChanged(player, newCursor, -1);
    }

    /**
     * Called when a player's cursor item changes
     */
    public static void onCursorChanged(ServerPlayer player, ItemStack newCursor, int originSlot) {
        try {
            ItemStack oldCursor = playerCursors.get(player.getUUID());
            if  (oldCursor == null) {
                oldCursor = ItemStack.EMPTY;
            }
            if (newCursor == null) {
                newCursor = ItemStack.EMPTY;
            }

            // Only sync if the cursor actually changed
            if (!ItemStack.isSameItem(oldCursor, newCursor)) {
                playerCursors.put(player.getUUID(), newCursor.copy());

                // Broadcast to spectators
                ClientboundScreenCursorSyncPacket packet = new ClientboundScreenCursorSyncPacket(
                    player.getUUID(),
                    newCursor,
                    originSlot
                );

                ServerSyncController.broadcastPacketToSpectators(player, packet);
            }
        } catch (Exception e) {
            LOGGER.error("Error syncing cursor change for player {}", player.getGameProfile().name(), e);
        }
    }

    /**
     * Send initial cursor data to a spectator
     */
    public static void sendPacket(ServerPlayer spectator, ServerPlayer target) {
        ItemStack cursor = playerCursors.getOrDefault(target.getUUID(), ItemStack.EMPTY);

        if (!cursor.isEmpty()) {
            ClientboundScreenCursorSyncPacket packet = new ClientboundScreenCursorSyncPacket(
                target.getUUID(),
                cursor,
                -1
            );

            ServerSyncController.sendPacket(spectator, packet);
        }
    }
}
