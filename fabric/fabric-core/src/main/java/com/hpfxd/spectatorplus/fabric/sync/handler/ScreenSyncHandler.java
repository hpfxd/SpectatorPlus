package com.hpfxd.spectatorplus.fabric.sync.handler;

import com.hpfxd.spectatorplus.fabric.sync.ServerSyncController;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundInventorySyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundScreenSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ServerboundOpenedInventorySyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ServerboundRequestInventoryOpenPacket;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScreenSyncHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScreenSyncHandler.class);

    public static void init() {
        ServerPlayNetworking.registerGlobalReceiver(ServerboundRequestInventoryOpenPacket.TYPE, ScreenSyncHandler::handle);
        ServerPlayNetworking.registerGlobalReceiver(ServerboundOpenedInventorySyncPacket.TYPE, ScreenSyncHandler::handle);
    }

    private static void handle(ServerboundRequestInventoryOpenPacket packet, ServerPlayNetworking.Context ctx) {
        try {
            final var server = ctx.server();
            final var player = ctx.player();
            final var target = server.getPlayerList().getPlayer(packet.playerId());

            if (target != null) {
                onRequestOpen(player, target);
            } else {
                LOGGER.warn("Player {} requested to view inventory of non-existent player {}",
                    player.getGameProfile().name(), packet.playerId());
            }
        } catch (Exception e) {
            LOGGER.error("Error handling inventory open request", e);
        }
    }

    private static void handle(ServerboundOpenedInventorySyncPacket packet, ServerPlayNetworking.Context ctx) {
        try {
            final var player = ctx.player();
            if (packet.isOpened()) {
                syncScreenOpened(player);
            } else {
                syncScreenClosed(player);
            }
        } catch (Exception e) {
            LOGGER.error("Error handling player inventory open/close", e);
        }
    }

    private static void onRequestOpen(ServerPlayer spectator, ServerPlayer target) {
        // Check if spectator has permission to view inventory
        if (canSyncInventory(spectator)) {
            openPlayerInventory(spectator, target);
        }
    }

    /**
     * Called when a player opens their inventory or any container screen.
     * This method syncs the appropriate screen to all spectators who are viewing this player.
     *
     * @param target The player who opened their inventory/container
     */
    public static void syncScreenOpened(ServerPlayer target) {
        try {
            // Get all spectators who are currently viewing this player
            var spectators = ServerSyncController.getSpectators(target);
            if (spectators.isEmpty()) {
                return; // No spectators, no need to sync
            }

            for (ServerPlayer spectator : spectators) {
                if (!canSyncInventory(spectator)) {
                    continue;
                }

                // Determine what type of screen the target player has open
                if (target.containerMenu != target.inventoryMenu) {
                    // Player has a container open (chest, crafting table, furnace, etc.)
                    LOGGER.debug("Subscribing spectator {} to container of {}",
                        spectator.getGameProfile().name(), target.getGameProfile().name());
                    ContainerSyncHandler.subscribeToContainer(spectator, target);
                } else {
                    // Player has their regular inventory open (survival/creative inventory)
                    LOGGER.debug("Syncing player inventory screen for spectator {} viewing {}",
                        spectator.getGameProfile().name(), target.getGameProfile().name());
                    syncPlayerInventoryScreen(spectator, target);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error syncing player inventory screen for {}", target.getGameProfile().name(), e);
        }
    }

    /**
     * Called when a player closes their inventory or any container screen.
     * This method closes the synced screen for all spectators who are viewing this player.
     *
     * @param target The player who closed their inventory/container
     */
    public static void syncScreenClosed(ServerPlayer target) {
        try {
            // Get all spectators who are currently viewing this player
            var spectators = ServerSyncController.getSpectators(target);
            if (spectators.isEmpty()) {
                return; // No spectators, no need to sync
            }

            for (ServerPlayer spectator : spectators) {
                if (!canSyncInventory(spectator)) {
                    continue;
                }

                LOGGER.debug("Syncing inventory close for spectator {} viewing {}",
                    spectator.getGameProfile().name(), target.getGameProfile().name());

                // Remove any container listeners for this spectator
                ContainerSyncHandler.unsubscribeFromContainer(spectator, target);

                // Send screen sync packet to indicate inventory/container was closed
                int flags = 0; // No flags means close screen
                flags |= (1 << 3); // Screen closed flag
                ServerSyncController.sendPacket(spectator, new ClientboundScreenSyncPacket(target.getUUID(), flags));
            }
        } catch (Exception e) {
            LOGGER.error("Error syncing player inventory close for {}", target.getGameProfile().name(), e);
        }
    }

    private static void openPlayerInventory(ServerPlayer spectator, ServerPlayer target) {
        // Send screen sync packet to indicate survival inventory
        int flags = 1; // Survival inventory flag
        flags |= (1 << 1); // Client requested flag
        // Send full inventory data
        InventorySyncHandler.sendPacket(spectator, target);

        ServerSyncController.sendPacket(spectator, new ClientboundScreenSyncPacket(target.getUUID(), flags));
    }

    private static boolean canSyncInventory(ServerPlayer player) {
        return Permissions.check(player, "spectatorplus.sync.inventory", true);
    }

    public static void updatePlayerInventory(ServerPlayer player, ItemStack[] inventorySendSlots) {
        try {
            // Only send to spectators who have permission and are actually spectating this player
            for (ServerPlayer spectator : ServerSyncController.getSpectators(player)) {
                if (canSyncInventory(spectator)) {
                    ServerSyncController.sendPacket(spectator,
                        new ClientboundInventorySyncPacket(player.getUUID(), inventorySendSlots));
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error updating inventory for player {}", player.getGameProfile().name(), e);
        }
    }

    /**
     * Clean up all listeners for a spectator when they stop spectating a target.
     * Called from ServerPlayerMixin when camera changes.
     */
    public static void cleanupSpectatorListeners(ServerPlayer spectator, ServerPlayer target) {
        ContainerSyncHandler.cleanupSpectatorListeners(spectator, target);
    }

    /**
     * Syncs a player's regular inventory screen (survival/creative inventory) to a spectator.
     * This is used when the target player has opened their own inventory.
     *
     * @param spectator The spectator to sync the screen to
     * @param target The player whose inventory screen should be synced
     */
    private static void syncPlayerInventoryScreen(ServerPlayer spectator, ServerPlayer target) {
        // Send screen sync packet for player inventory (survival inventory)
        int flags = 1; // Survival inventory flag
        // Send the target's inventory data to the spectator
        InventorySyncHandler.sendPacket(spectator, target);

        ServerSyncController.sendPacket(spectator, new ClientboundScreenSyncPacket(target.getUUID(), flags));
    }
}
