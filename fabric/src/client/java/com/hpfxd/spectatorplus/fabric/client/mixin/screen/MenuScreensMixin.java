package com.hpfxd.spectatorplus.fabric.client.mixin.screen;

import com.hpfxd.spectatorplus.fabric.client.SpectatorClientMod;
import com.hpfxd.spectatorplus.fabric.client.sync.ClientSyncController;
import com.hpfxd.spectatorplus.fabric.client.sync.screen.ScreenSyncController;
import com.hpfxd.spectatorplus.fabric.client.util.SpecUtil;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MenuScreens.class)
public abstract class MenuScreensMixin {
    @Inject(
            method = "create(Lnet/minecraft/world/inventory/MenuType;Lnet/minecraft/client/Minecraft;ILnet/minecraft/network/chat/Component;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/MenuScreens$ScreenConstructor;fromPacket(Lnet/minecraft/network/chat/Component;Lnet/minecraft/world/inventory/MenuType;Lnet/minecraft/client/Minecraft;I)V"
            ),
            cancellable = true
    )
    private static <M extends AbstractContainerMenu, S extends Screen & MenuAccess<M>> void spectatorplus$handleSynced(MenuType<M> type, Minecraft mc, int windowId, Component title, CallbackInfo ci, @Local MenuScreens.ScreenConstructor<M, S> screenConstructor) {
        if (ScreenSyncController.isPendingOpen && ClientSyncController.syncData != null && ClientSyncController.syncData.screen != null) {
            ci.cancel();

            if (SpectatorClientMod.config.openScreens || ClientSyncController.syncData.screen.isClientRequested) {
                final Player spectated = SpecUtil.getCameraPlayer(mc);
                if (spectated != null) {
                    ScreenSyncController.syncedWindowId = windowId;

                    final boolean hasInventory = ScreenSyncController.createInventory(spectated);

                    if (ClientSyncController.syncData.screen.isSurvivalInventory) {
                        if (hasInventory) {
                            ScreenSyncController.openPlayerInventory(mc);
                            return;
                        }

                        // if no inventory could be created, we close the inventory
                    } else {
                        final Inventory inventory;
                        if (hasInventory) {
                            inventory = ScreenSyncController.syncedInventory;
                        } else {
                            inventory = mc.player.getInventory();
                        }

                        final M menu = type.create(windowId, inventory);
                        final S screen = screenConstructor.create(menu, inventory, title);

                        ScreenSyncController.handleNewSyncedScreen(mc, screen);
                        return;
                    }
                }
            }

            // Unable to open, immediately tell the server we've closed this screen.
            mc.getConnection().send(new ServerboundContainerClosePacket(windowId));
            ClientSyncController.syncData.screen = null;
            ScreenSyncController.isPendingOpen = false;
        }
    }
}
