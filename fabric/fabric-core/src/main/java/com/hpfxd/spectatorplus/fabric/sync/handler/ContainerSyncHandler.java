package com.hpfxd.spectatorplus.fabric.sync.handler;

import com.hpfxd.spectatorplus.fabric.sync.ServerSyncController;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundContainerSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundScreenSyncPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ContainerSyncHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerSyncHandler.class);

    // Map to track container listeners for each spectator
    // Key: spectator UUID, Value: listener instance
    private static final Map<UUID, ContainerListener> containerListeners = new HashMap<>();

    public static void init() {
        // Clean up listeners when a player disconnects
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            containerListeners.remove(handler.getPlayer().getUUID());
        });
    }

    /**
     * Subscribe a spectator to the target player's open container.
     * The spectator will receive automatic updates when the container changes.
     * This is like the spectator opening the same container themselves.
     */
    public static void subscribeToContainer(ServerPlayer spectator, ServerPlayer target) {
        try {
            var containerMenu = target.containerMenu;
            if (containerMenu == target.inventoryMenu) {
                return; // No container open, just player inventory
            }

            // Remove any existing listener for this spectator
            unsubscribeFromContainer(spectator, target);

            // IMPORTANT: Send data in the correct order to prevent the client from clearing it
            // 1. First, send the target's inventory data (player's backpack)
            InventorySyncHandler.sendPacket(spectator, target);

            // 2. Prepare container data
            int containerSize = containerMenu.slots.size() - 36; // Subtract player inventory slots
            ItemStack[] containerItems = new ItemStack[containerSize];

            for (int i = 0; i < containerSize; i++) {
                var slot = containerMenu.getSlot(i);
                containerItems[i] = slot.getItem().copy();
            }

            // 3. Send container sync packet (this populates container items before screen opens)
            ServerSyncController.sendPacket(spectator, new ClientboundContainerSyncPacket(
                target.getUUID(),
                containerMenu.getType(),
                containerSize,
                containerItems
            ));

            // 4. Finally, send screen sync packet to open the GUI (this must be last)
            int flags = 0; // Container screen (not survival inventory)
            flags |= (1 << 2); // Has dummy slots flag for container screens
            ServerSyncController.sendPacket(spectator, new ClientboundScreenSyncPacket(target.getUUID(), flags));

            // Create and store the listener for this spectator
            ContainerListener listener = new ContainerListener() {
                @Override
                public void slotChanged(@NotNull net.minecraft.world.inventory.AbstractContainerMenu menu, int slotIndex, @NotNull ItemStack stack) {
                    // Only sync container slots, not player inventory slots
                    if (slotIndex < containerSize && ServerSyncController.getSpectators(target).contains(spectator)) {
                        ItemStack[] update = new ItemStack[containerSize];
                        for (int i = 0; i < containerSize; i++) {
                            if (i == slotIndex) {
                                update[i] = stack.copy();
                            } else {
                                // For efficiency, only send changed slot
                                update[i] = null;
                            }
                        }
                        ServerSyncController.sendPacket(spectator, new ClientboundContainerSyncPacket(
                            target.getUUID(),
                            menu.getType(),
                            containerSize,
                            update
                        ));
                    }
                }

                @Override
                public void dataChanged(@NotNull net.minecraft.world.inventory.AbstractContainerMenu menu, int dataSlot, int value) {
                    // Handle furnace progress, brewing stand progress, etc.
                    // For now, we don't need to sync this separately
                }
            };

            // Add spectator as a listener to the container menu
            containerMenu.addSlotListener(listener);

            // Store the listener so we can remove it later
            containerListeners.put(spectator.getUUID(), listener);

            // Try to open the actual screen on the spectator's client
            try {
                spectator.connection.send(new ClientboundOpenScreenPacket(
                    containerMenu.containerId,
                    containerMenu.getType(),
                    target.getDisplayName()
                ));
            } catch (Exception e) {
                LOGGER.debug("Could not send ClientboundOpenScreenPacket to spectator {}: {}",
                    spectator.getGameProfile().name(), e.getMessage());
                // Continue without the packet - the client will handle it through sync packets
            }
        } catch (Exception e) {
            LOGGER.error("Error subscribing spectator {} to container of {}",
                spectator.getGameProfile().name(), target.getGameProfile().name(), e);
        }
    }

    /**
     * Unsubscribe a spectator from a target player's container.
     * This removes the container listener to prevent memory leaks.
     */
    public static void unsubscribeFromContainer(ServerPlayer spectator, ServerPlayer target) {
        try {
            ContainerListener oldListener = containerListeners.remove(spectator.getUUID());
            if (oldListener != null && target.containerMenu != null) {
                target.containerMenu.removeSlotListener(oldListener);
                LOGGER.debug("Removed container listener for spectator {}", spectator.getGameProfile().name());
            }
        } catch (Exception e) {
            LOGGER.error("Error unsubscribing spectator {} from container", spectator.getGameProfile().name(), e);
        }
    }

    /**
     * Clean up all listeners for a spectator when they stop spectating a target.
     * Called from ServerPlayerMixin when camera changes.
     */
    public static void cleanupSpectatorListeners(ServerPlayer spectator, ServerPlayer target) {
        unsubscribeFromContainer(spectator, target);
    }
}
