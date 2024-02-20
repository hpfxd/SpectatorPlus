package com.hpfxd.spectatorplus.fabric.client.sync.screen;

import com.hpfxd.spectatorplus.fabric.client.gui.screens.SyncedInventoryScreen;
import com.hpfxd.spectatorplus.fabric.client.util.SpecUtil;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundInventorySyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundScreenCursorSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundScreenSyncPacket;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import static com.hpfxd.spectatorplus.fabric.client.sync.ClientSyncController.setSyncData;
import static com.hpfxd.spectatorplus.fabric.client.sync.ClientSyncController.syncData;

public class ScreenSyncController {
    public static boolean isPendingOpen = false;

    public static int syncedWindowId = -1;
    public static Inventory syncedInventory;
    public static Screen syncedScreen;

    public static void init() {
        ClientEntityEvents.ENTITY_UNLOAD.register((entity, level) -> {
            if (entity instanceof final Player player && syncData != null && syncData.playerId.equals(player.getUUID())) {
                closeSyncedInventory();
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(ClientboundScreenSyncPacket.TYPE, ScreenSyncController::handle);
        ClientPlayNetworking.registerGlobalReceiver(ClientboundInventorySyncPacket.TYPE, ScreenSyncController::handle);
        ClientPlayNetworking.registerGlobalReceiver(ClientboundScreenCursorSyncPacket.TYPE, ScreenSyncController::handle);
    }

    private static void handle(ClientboundScreenSyncPacket packet, LocalPlayer player, PacketSender sender) {
        setSyncData(packet.playerId());
        syncData.setScreen();

        isPendingOpen = true;
        syncData.screen.isSurvivalInventory = packet.isSurvivalInventory();
        syncData.screen.isClientRequested = packet.isClientRequested();
    }

    private static void handle(ClientboundInventorySyncPacket packet, LocalPlayer player, PacketSender sender) {
        setSyncData(packet.playerId());
        syncData.setScreen();

        if (syncData.screen.inventoryItems == null) {
            syncData.screen.inventoryItems = NonNullList.withSize(ClientboundInventorySyncPacket.ITEMS_LENGTH, ItemStack.EMPTY);
        }

        final ItemStack[] items = packet.items();
        for (int slot = 0; slot < items.length; slot++) {
            final ItemStack item = items[slot];

            if (item != null) {
                syncData.screen.inventoryItems.set(slot, item);

                if (syncedInventory != null) {
                    syncedInventory.setItem(slot, item);
                }
            }
        }
    }

    private static void handle(ClientboundScreenCursorSyncPacket packet, LocalPlayer player, PacketSender sender) {
        setSyncData(packet.playerId());
        syncData.setScreen();

        syncData.screen.cursorItem = packet.cursor();
        syncData.screen.cursorItemSlot = packet.originSlot();
    }

    public static void closeSyncedInventory() {
        if (syncedScreen != null) {
            syncedScreen.onClose();
            syncedInventory = null;
        }
    }

    public static void openPlayerInventory(Minecraft mc) {
        final Player player = SpecUtil.getCameraPlayer(mc);
        final SyncedInventoryScreen screen = new SyncedInventoryScreen(player);

        handleNewSyncedScreen(mc, screen);
    }

    public static <S extends Screen & MenuAccess<?>> void handleNewSyncedScreen(Minecraft mc, S screen) {
        mc.player.containerMenu = screen.getMenu();
        mc.setScreen(screen);

        if (mc.screen != screen) {
            syncedInventory = null;
            syncData.screen = null;
            return;
        }

        syncedScreen = screen;

        ScreenEvents.remove(screen).register(s -> {
            syncedScreen = null;
            syncedInventory = null;
            syncedWindowId = -1;
            syncData.screen = null;
        });
    }

    public static boolean createInventory(Player spectated) {
        if (syncData.screen.inventoryItems == null) {
            return false;
        }

        syncedInventory = new Inventory(spectated);

        for (int i = 0; i < syncData.screen.inventoryItems.size(); i++) {
            syncedInventory.items.set(i, syncData.screen.inventoryItems.get(i));
        }
        return true;
    }
}
