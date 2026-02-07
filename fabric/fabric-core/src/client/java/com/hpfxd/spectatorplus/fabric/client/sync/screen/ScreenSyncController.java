package com.hpfxd.spectatorplus.fabric.client.sync.screen;

import com.hpfxd.spectatorplus.fabric.client.gui.screens.SyncedInventoryScreen;
import com.hpfxd.spectatorplus.fabric.client.gui.screens.DummyContainer;
import com.hpfxd.spectatorplus.fabric.client.mixin.InventoryAccessor;
import com.hpfxd.spectatorplus.fabric.client.util.SpecUtil;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundInventorySyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundScreenCursorSyncPacket;
import com.hpfxd.spectatorplus.fabric.sync.packet.ClientboundScreenSyncPacket;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

import static com.hpfxd.spectatorplus.fabric.client.sync.ClientSyncController.setSyncData;
import static com.hpfxd.spectatorplus.fabric.client.sync.ClientSyncController.syncData;

public class ScreenSyncController {
    public static boolean isPendingOpen = false;

    public static int syncedWindowId = Integer.MIN_VALUE;
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

    private static void handle(ClientboundScreenSyncPacket packet, ClientPlayNetworking.Context context) {
        setSyncData(packet.playerId());
        syncData.setScreen();

        // Check if this is a screen close event
        if (packet.isScreenClosed()) {
            // Close the synced inventory screen
            final Minecraft mc = Minecraft.getInstance();
            mc.execute(() -> {
                closeSyncedInventory();
            });
            return;
        }

        isPendingOpen = true;
        syncData.screen.isSurvivalInventory = packet.isSurvivalInventory();
        syncData.screen.isClientRequested = packet.isClientRequested();
        syncData.screen.hasDummySlots = packet.hasDummySlots();

        // If this is a survival inventory screen sync, try to open it immediately
        if (syncData.screen.isSurvivalInventory) {
            final Minecraft mc = Minecraft.getInstance();
            mc.execute(() -> {
                if (isPendingOpen && syncData.screen.isSurvivalInventory) {
                    // Create the synced inventory first, so the mixin can use it
                    final Player player = SpecUtil.getCameraPlayer(mc);
                    if (player != null && createInventory(player)) {
                        openPlayerInventory(mc);
                    }
                }
            });
        }
    }

    private static void handle(ClientboundInventorySyncPacket packet, ClientPlayNetworking.Context context) {
        setSyncData(packet.playerId());
        syncData.setScreen();

        syncData.screen.initializeInventoryItems(ClientboundInventorySyncPacket.ITEMS_LENGTH);

        final ItemStack[] items = packet.items();
        for (int slot = 0; slot < items.length; slot++) {
            final ItemStack item = items[slot];
            if (item != null) {
                syncData.screen.updateInventoryItem(slot, item);
                if (syncedInventory != null) {
                    syncedInventory.setItem(slot, item);
                }
            }
        }
        // Also update global armorItems for convenience
        if (syncData.armorItems != null && items.length >= 40) {
            for (int slot = 36; slot < 40; slot++) {
                final ItemStack item = items[slot];
                if (item != null) {
                    syncData.armorItems.set(slot - 36, item);
                }
            }
        }
    }

    private static void handle(ClientboundScreenCursorSyncPacket packet, ClientPlayNetworking.Context context) {
        setSyncData(packet.playerId());
        syncData.setScreen();

        syncData.screen.cursorItem = packet.cursor();
        syncData.screen.cursorItemSlot = packet.originSlot();
    }

    public static void closeSyncedInventory() {
        final Minecraft mc = Minecraft.getInstance();
        if (syncedScreen != null) {
            // Close the current synced screen
            mc.setScreen(null);
            syncedScreen = null;
        }
        // Clean up synced inventory and related state
        syncedInventory = null;
        syncedWindowId = Integer.MIN_VALUE;
        isPendingOpen = false;

        // Clear screen sync data
        if (syncData != null) {
            syncData.screen = null;
        }
    }

    public static void openPlayerInventory(Minecraft mc) {
        final Player player = SpecUtil.getCameraPlayer(mc);
        if (player != null && mc.player != null) {
            final SyncedInventoryScreen screen = new SyncedInventoryScreen(player);
            handleNewSyncedScreen(mc, screen);
        }
    }

    public static void openContainerScreen(Minecraft mc, MenuType<?> containerType, int containerSize) {
        final Player player = SpecUtil.getCameraPlayer(mc);
        if (player != null && mc.player != null && syncData != null && syncData.screen != null) {
            // Create dummy container for the synced screen
            DummyContainer containerInventory = new DummyContainer(containerSize);

            // Populate the container with synced items using helper method
            syncData.screen.populateContainer(containerInventory);

            Inventory playerInventory = syncedInventory != null ? syncedInventory : mc.player.getInventory();
            Screen screen = createVanillaContainerScreen(containerType, syncedWindowId, playerInventory, containerInventory);

            if (screen instanceof MenuAccess<?>) {
                handleNewSyncedScreen(mc, (Screen & MenuAccess<?>) screen);
            }
        }
    }

    private static Screen createVanillaContainerScreen(MenuType<?> type, int windowId, Inventory playerInventory, DummyContainer containerInventory) {
        Component title = Component.literal("Container");

        // For furnace-like containers
        if (type == MenuType.FURNACE) {
            FurnaceMenu menu = new FurnaceMenu(windowId, playerInventory, containerInventory, new SimpleContainerData(4));
            return new FurnaceScreen(menu, playerInventory, title);
        } else if (type == MenuType.BLAST_FURNACE) {
            BlastFurnaceMenu menu = new BlastFurnaceMenu(windowId, playerInventory, containerInventory, new SimpleContainerData(4));
            return new BlastFurnaceScreen(menu, playerInventory, title);
        } else if (type == MenuType.SMOKER) {
            SmokerMenu menu = new SmokerMenu(windowId, playerInventory, containerInventory, new SimpleContainerData(4));
            return new SmokerScreen(menu, playerInventory, title);
        }

        // For brewing stand
        else if (type == MenuType.BREWING_STAND) {
            BrewingStandMenu menu = new BrewingStandMenu(windowId, playerInventory, containerInventory, new SimpleContainerData(2));
            return new BrewingStandScreen(menu, playerInventory, title);
        }

        // For anvil
        else if (type == MenuType.ANVIL) {
            AnvilMenu menu = new AnvilMenu(windowId, playerInventory, ContainerLevelAccess.NULL);
            return new AnvilScreen(menu, playerInventory, title);
        }

        // For enchanting table
        else if (type == MenuType.ENCHANTMENT) {
            EnchantmentMenu menu = new EnchantmentMenu(windowId, playerInventory, ContainerLevelAccess.NULL);
            return new EnchantmentScreen(menu, playerInventory, title);
        }

        // For crafting table
        else if (type == MenuType.CRAFTING) {
            CraftingMenu menu = new CraftingMenu(windowId, playerInventory, ContainerLevelAccess.NULL);
            return new CraftingScreen(menu, playerInventory, title);
        }

        // For grindstone
        else if (type == MenuType.GRINDSTONE) {
            GrindstoneMenu menu = new GrindstoneMenu(windowId, playerInventory, ContainerLevelAccess.NULL);
            return new GrindstoneScreen(menu, playerInventory, title);
        }

        // For loom
        else if (type == MenuType.LOOM) {
            LoomMenu menu = new LoomMenu(windowId, playerInventory, ContainerLevelAccess.NULL);
            return new LoomScreen(menu, playerInventory, title);
        }

        // For stonecutter
        else if (type == MenuType.STONECUTTER) {
            StonecutterMenu menu = new StonecutterMenu(windowId, playerInventory, ContainerLevelAccess.NULL);
            return new StonecutterScreen(menu, playerInventory, title);
        }

        // For cartography table
        else if (type == MenuType.CARTOGRAPHY_TABLE) {
            CartographyTableMenu menu = new CartographyTableMenu(windowId, playerInventory, ContainerLevelAccess.NULL);
            return new CartographyTableScreen(menu, playerInventory, title);
        }

        // For smithing table
        else if (type == MenuType.SMITHING) {
            SmithingMenu menu = new SmithingMenu(windowId, playerInventory, ContainerLevelAccess.NULL);
            return new SmithingScreen(menu, playerInventory, title);
        }

        // For generic containers (chests, etc.)
        else if (type == MenuType.GENERIC_9x1) {
            ChestMenu menu = new ChestMenu(MenuType.GENERIC_9x1, windowId, playerInventory, containerInventory, 1);
            return new ContainerScreen(menu, playerInventory, title);
        } else if (type == MenuType.GENERIC_9x2) {
            ChestMenu menu = new ChestMenu(MenuType.GENERIC_9x2, windowId, playerInventory, containerInventory, 2);
            return new ContainerScreen(menu, playerInventory, title);
        } else if (type == MenuType.GENERIC_9x3) {
            ChestMenu menu = new ChestMenu(MenuType.GENERIC_9x3, windowId, playerInventory, containerInventory, 3);
            return new ContainerScreen(menu, playerInventory, title);
        } else if (type == MenuType.GENERIC_9x4) {
            ChestMenu menu = new ChestMenu(MenuType.GENERIC_9x4, windowId, playerInventory, containerInventory, 4);
            return new ContainerScreen(menu, playerInventory, title);
        } else if (type == MenuType.GENERIC_9x5) {
            ChestMenu menu = new ChestMenu(MenuType.GENERIC_9x5, windowId, playerInventory, containerInventory, 5);
            return new ContainerScreen(menu, playerInventory, title);
        } else if (type == MenuType.GENERIC_9x6) {
            ChestMenu menu = new ChestMenu(MenuType.GENERIC_9x6, windowId, playerInventory, containerInventory, 6);
            return new ContainerScreen(menu, playerInventory, title);
        }

        // For dispenser and dropper
        else if (type == MenuType.GENERIC_3x3) {
            DispenserMenu menu = new DispenserMenu(windowId, playerInventory, containerInventory);
            return new DispenserScreen(menu, playerInventory, title);
        }

        // For hopper
        else if (type == MenuType.HOPPER) {
            HopperMenu menu = new HopperMenu(windowId, playerInventory, containerInventory);
            return new HopperScreen(menu, playerInventory, title);
        }

        // For shulker box
        else if (type == MenuType.SHULKER_BOX) {
            ShulkerBoxMenu menu = new ShulkerBoxMenu(windowId, playerInventory, containerInventory);
            return new ShulkerBoxScreen(menu, playerInventory, title);
        }

        // For beacon
        else if (type == MenuType.BEACON) {
            BeaconMenu menu = new BeaconMenu(windowId, containerInventory, new SimpleContainerData(3), ContainerLevelAccess.NULL);
            return new BeaconScreen(menu, playerInventory, title);
        }

        // Fallback to generic 3x9 chest for unknown types (including lectern which has no GUI)
        else {
            ChestMenu menu = new ChestMenu(MenuType.GENERIC_9x3, windowId, playerInventory, containerInventory, 3);
            return new ContainerScreen(menu, playerInventory, title);
        }
    }

    public static <S extends Screen & MenuAccess<?>> void handleNewSyncedScreen(Minecraft mc, S screen) {
        isPendingOpen = false;
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
            syncedWindowId = Integer.MIN_VALUE;
            syncData.screen = null;
        });
    }

    public static boolean createInventory(Player spectated) {
        if (!syncData.screen.hasInventoryItems()) {
            return false;
        }

        EntityEquipment equipment = ((InventoryAccessor)spectated.getInventory()).getEquipment();
        syncedInventory = new Inventory(spectated, equipment);
        syncData.screen.populateInventory(syncedInventory);
        return true;
    }
}
